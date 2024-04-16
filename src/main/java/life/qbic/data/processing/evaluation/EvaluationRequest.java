package life.qbic.data.processing.evaluation;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import life.qbic.data.processing.Provenance;
import life.qbic.data.processing.Provenance.ProvenanceException;
import org.apache.logging.log4j.Logger;

/**
 * <b>Evaluation Request</b>
 *
 * <p>Currently only validates the presence of a QBiC measurement ID in the dataset root
 * folder.</p>
 * <p>
 * If none is present, or the identifier does not match the requirements, it is moved back to the
 * users error folder.
 *
 * @since 1.0.0
 */
public class EvaluationRequest extends Thread {

  private static final String THREAD_NAME = "Evaluation-%s";
  private static final String INTERVENTION_DIRECTORY = "interventions";
  private static final Logger LOG = getLogger(EvaluationRequest.class);
  private static final Set<String> ACTIVE_TASKS = new HashSet<>();
  private static final ReentrantLock LOCK = new ReentrantLock();
  private static int threadNumber = 1;
  private final Path interventionDirectory;
  private final AtomicBoolean active = new AtomicBoolean(false);
  private final AtomicBoolean terminated = new AtomicBoolean(false);
  private final Path workingDirectory;
  private final Path targetDirectory;
  private final Pattern measurementIdPattern;
  private final Path usersErrorDirectory;

  public EvaluationRequest(Path workingDirectory, Path targetDirectory,
      Pattern measurementIdPattern, Path usersErrorDirectory) {
    this.setName(THREAD_NAME.formatted(nextThreadNumber()));
    this.workingDirectory = workingDirectory;
    this.targetDirectory = targetDirectory;
    this.measurementIdPattern = measurementIdPattern;
    if (!workingDirectory.resolve(INTERVENTION_DIRECTORY).toFile().mkdir()
        && !workingDirectory.resolve(
        INTERVENTION_DIRECTORY).toFile().exists()) {
      throw new RuntimeException(
          "Could not create intervention directory for processing request at " + workingDirectory);
    }
    this.usersErrorDirectory = usersErrorDirectory;
    this.interventionDirectory = workingDirectory.resolve(INTERVENTION_DIRECTORY);
  }

  public EvaluationRequest(EvaluationConfiguration evaluationConfiguration) {
    this(evaluationConfiguration.workingDirectory(), evaluationConfiguration.targetDirectory(),
        evaluationConfiguration.measurementIdPattern(),
        evaluationConfiguration.usersErrorDirectory());
  }

  private static int nextThreadNumber() {
    return threadNumber++;
  }

  private static boolean push(String taskId) {
    LOCK.lock();
    boolean notActiveYet;
    try {
      notActiveYet = ACTIVE_TASKS.add(taskId);
    } finally {
      LOCK.unlock();
    }
    return notActiveYet;
  }

  @Override
  public void run() {
    while (true) {
      active.set(true);
      for (File taskDir : tasks()) {
        if (push(taskDir.getAbsolutePath())) {
          evaluateDirectory(taskDir);
          removeTask(taskDir);
        }
      }
      active.set(false);
      if (terminated.get()) {
        LOG.warn("Thread {} terminated", Thread.currentThread().getName());
        break;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // We don't want to interrupt the thread here, only explicit to enable graceful shutdown
        // via its interrupt() method
      }
    }
  }

  private void removeTask(File taskDir) {
    LOCK.lock();
    try {
      ACTIVE_TASKS.remove(taskDir.getAbsolutePath());
    } finally {
      LOCK.unlock();
    }
  }

  private void evaluateDirectory(File taskDir) {
    var provenanceSearch = Provenance.findProvenance(taskDir.toPath());
    if (provenanceSearch.isEmpty()) {
      LOG.error("No provenance file found: %s".formatted(taskDir.getAbsolutePath()));
      moveToSystemIntervention(taskDir, "Provenance file was not found");
      return;
    }

    Provenance provenance = null;
    try {
      provenance = Provenance.parse(provenanceSearch.get().toPath());
    } catch (ProvenanceException e) {
      LOG.error("Could not parse provenance file: %s".formatted(taskDir.getAbsolutePath()), e);
      moveToSystemIntervention(taskDir, e.getMessage());
      return;
    }

    var datasetSearch = findDataset(taskDir);
    if (datasetSearch.isEmpty()) {
      LOG.error("No dataset found: %s".formatted(taskDir.getAbsolutePath()));
      moveBackToOrigin(taskDir, provenance, "No dataset directory found.");
      return;
    }
    var dataset = datasetSearch.get();
    Matcher matcher = measurementIdPattern.matcher(dataset.getName());
    var measurementIdResult = matcher.results().map(MatchResult::group).findFirst();
    if (measurementIdResult.isPresent()) {
      moveToTargetDir(taskDir);
      return;
    }
    String reason = "Missing measurement identifier: no known measurement id was found in the content of directory '%s'".formatted(
        taskDir.getName());
    LOG.error(reason);
    moveBackToOrigin(taskDir, provenance, reason);
  }

  private Optional<File> findDataset(File taskDir) {
    return Arrays.stream(taskDir.listFiles()).filter(File::isDirectory).findFirst();
  }

  private void moveToSystemIntervention(File taskDir, String reason) {
    try {
      var errorFile = taskDir.toPath().resolve("error.txt").toFile();
      errorFile.createNewFile();
      Files.writeString(errorFile.toPath(), reason);
      Files.move(taskDir.toPath(), interventionDirectory.resolve(taskDir.getName()));
    } catch (IOException e) {
      throw new RuntimeException("Cannot move task to intervention: %s".formatted(taskDir), e);
    }
  }

  private void moveBackToOrigin(File taskDir, Provenance provenance, String reason) {
    LOG.info("Moving back to original user directory: " + taskDir.getAbsolutePath());
    try {
      var errorFile = taskDir.toPath().resolve("error.txt").toFile();
      errorFile.createNewFile();
      Files.writeString(errorFile.toPath(), reason);
      Paths.get(provenance.userWorkDirectoryPath).resolve(usersErrorDirectory).toFile().mkdir();
      Files.move(taskDir.toPath(),
          Paths.get(provenance.userWorkDirectoryPath).resolve(usersErrorDirectory).resolve(taskDir.getName()));
    } catch (IOException e) {
      LOG.error("Cannot move task to user intervention: %s".formatted(provenance.originPath), e);
      moveToSystemIntervention(taskDir, e.getMessage());
    }
  }

  private void moveToTargetDir(File taskDir) {
    LOG.info(
        "Moving %s to target directory %s".formatted(taskDir.getAbsolutePath(), targetDirectory));
    try {
      Files.move(taskDir.toPath(), targetDirectory.resolve(taskDir.getName()));
    } catch (IOException e) {
      LOG.error("Cannot move task to target directory: %s".formatted(targetDirectory), e);
      moveToSystemIntervention(taskDir,
          "Cannot move task to target directory: %s".formatted(targetDirectory));
    }

  }

  private List<File> tasks() {
    return Arrays.stream(workingDirectory.toFile().listFiles()).filter(File::isDirectory)
        .filter(file -> !file.getName().equals(INTERVENTION_DIRECTORY)).toList();
  }

  public void interrupt() {
    terminated.set(true);
    while (active.get()) {
      LOG.debug("Thread is still active...");
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // we don't want to interrupt the worker thread before its task is done, since it might
        // render the application in a non-recoverable state
      }
    }
    LOG.debug("Task has been finished");
  }

}
