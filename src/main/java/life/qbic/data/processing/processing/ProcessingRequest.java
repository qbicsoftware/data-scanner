package life.qbic.data.processing.processing;

import static org.apache.logging.log4j.LogManager.getLogger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import life.qbic.data.processing.Provenance;
import org.apache.logging.log4j.Logger;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class ProcessingRequest extends Thread {

  private static final Logger LOG = getLogger(ProcessingRequest.class);
  private static final String THREAD_NAME = "Processing-%s";
  private static final Set<String> ACTIVE_TASKS = new HashSet<>();
  private static final ReentrantLock LOCK = new ReentrantLock();
  private static int threadNumber = 1;
  private final Path workingDirectory;
  private final Path targetDirectory;
  private final AtomicBoolean active = new AtomicBoolean(false);
  private final AtomicBoolean terminated = new AtomicBoolean(false);
  private final Path interventionDirectory;
  private static final String INTERVENTION_DIRECTORY = "interventions";

  public ProcessingRequest(ProcessingConfiguration processingConfiguration) {
    this.setName(THREAD_NAME.formatted(nextThreadNumber()));
    this.workingDirectory = processingConfiguration.getWorkingDirectory();
    this.targetDirectory = processingConfiguration.getTargetDirectory();
    if (!workingDirectory.resolve(INTERVENTION_DIRECTORY).toFile().mkdir() && !workingDirectory.resolve(
        INTERVENTION_DIRECTORY).toFile().exists()) {
      throw new RuntimeException(
          "Could not create intervention directory for processing request at " + workingDirectory);
    }
    this.interventionDirectory = workingDirectory.resolve(INTERVENTION_DIRECTORY);
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
          LOG.info("Registering task " + taskDir.getAbsolutePath());
          processFile(taskDir);
        }
      }
      try {
        LOG.info("Thread %s is doing sth".formatted(Thread.currentThread().getName()));
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // This thread will only handle interrupted signals via its explicit
        // API method call
      }
      active.set(false);
      if (terminated.get()) {
        LOG.warn("Thread %s terminated".formatted(Thread.currentThread().getName()));
        break;
      }
    }
  }

  private void processFile(File taskDir) {
    var taskDirContent = Arrays.stream(Objects.requireNonNull(taskDir.listFiles())).toList();
    if (taskDirContent.isEmpty()) {
      LOG.error("Task %s has no files", taskDir.getAbsolutePath());
      clearTask(taskDir);
      taskDir.delete();
      LOG.info("Empty task %s deleted", taskDir.getAbsolutePath());
      return;
    }
    Optional<File> provenanceFileSearch = taskDirContent.stream().filter(file -> file.getName().equals(Provenance.FILE_NAME)).findFirst();
    if (provenanceFileSearch.isEmpty()) {
      LOG.error("Task %s has no provenance file".formatted(taskDir.getAbsolutePath()));
      moveToIntervention(taskDir, "No provenance file provided");
      return;
    }
    var provenanceContent = readProvenance(provenanceFileSearch.get());
    if (provenanceContent.isEmpty()) {
      LOG.error("Task %s has no provenance file content".formatted(taskDir.getAbsolutePath()));
      return;
    }
    taskDirContent.stream().filter(file -> !file.getName().equals(Provenance.FILE_NAME)).findFirst().ifPresent(file -> {
      var provenance = provenanceContent.get();
      provenance.addToHistory(taskDir.getAbsolutePath());
      try {
        writeProvenance(provenanceFileSearch.get(), provenance);
      } catch (IOException e) {
        LOG.error("Could not write provenance file " + file.getAbsolutePath(), e);
        moveToIntervention(taskDir, "Writing provenance file failed");
      }
      try {
        moveToTargetFolder(taskDir);
      } catch (IOException e) {
        LOG.error("Could not move task %s to target location".formatted(file.getAbsolutePath()), e);
        moveToIntervention(taskDir, "Writing task directory failed" );
      }
    });
  }

  private void moveToTargetFolder(File taskDir) throws IOException {
    Files.move(taskDir.toPath(), targetDirectory.resolve(taskDir.getName()));
  }

  private void writeProvenance(File provenanceFile, Provenance provenance) throws IOException {
    var mapper = new ObjectMapper();
    mapper.writerWithDefaultPrettyPrinter().writeValue(provenanceFile, provenance);
  }

  private Optional<Provenance> readProvenance(File taskDir) {
    var objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Provenance value = null;
    try {
      value = objectMapper.readValue(taskDir, Provenance.class);
    } catch (IOException e) {
      LOG.error("Error reading provenance file", e);
    }
    return Optional.ofNullable(value);
  }

  private void moveToIntervention(File taskDir, String reason) {
    try {
      var errorFile = taskDir.toPath().resolve("error.txt").toFile();
      errorFile.createNewFile();
      Files.writeString(errorFile.toPath(), reason);
      Files.move(taskDir.toPath(), interventionDirectory.resolve(taskDir.getName()));
    } catch (IOException e) {
      throw new RuntimeException("Cannot move task to intervention: %s".formatted(taskDir), e);
    }

  }

  private void clearTask(File taskDir) {
    LOCK.lock();
    try {
      ACTIVE_TASKS.remove(taskDir.getAbsolutePath());
    } finally {
      LOCK.unlock();
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
