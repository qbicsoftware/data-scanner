package life.qbic.data.processing.evaluation;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Logger;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class EvaluationRequest extends Thread {

  private static final Logger log = getLogger(EvaluationRequest.class);
  private static final Set<String> activeTasks = new HashSet<>();
  private static final ReentrantLock lock = new ReentrantLock();
  private final AtomicBoolean active = new AtomicBoolean(false);
  private final AtomicBoolean terminated = new AtomicBoolean(false);
  private final Path workingDirectory;
  private final Path targetDirectory;
  private final Pattern measurementIdPattern;

  public EvaluationRequest(Path workingDirectory, Path targetDirectory,
      Pattern measurementIdPattern) {
    this.workingDirectory = workingDirectory;
    this.targetDirectory = targetDirectory;
    this.measurementIdPattern = measurementIdPattern;
  }

  private static boolean push(String taskId) {
    lock.lock();
    boolean notActiveYet;
    try {
      notActiveYet = activeTasks.add(taskId);
    } finally {
      lock.unlock();
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
        log.warn("Thread {} terminated", Thread.currentThread().getName());
        break;
      }
    }
  }

  private void removeTask(File taskDir) {
    lock.lock();
    try {
      activeTasks.remove(taskDir.getAbsolutePath());
    } finally {
      lock.unlock();
    }
  }

  private void evaluateDirectory(File taskDir) {
    Matcher matcher = measurementIdPattern.matcher(taskDir.getName());
    var measurementIdResult = matcher.results().map(MatchResult::group).findFirst();
    if (measurementIdResult.isPresent()) {
      moveToTargetDir(taskDir);
    }
    log.error("Missing measurement identifier: no known measurement id was found in " + taskDir.getAbsolutePath());
    moveBackToOrigin(taskDir);
  }

  private void moveBackToOrigin(File taskDir) {
    log.info("Moving back to original directory: " + taskDir.getAbsolutePath());
  }

  private void moveToTargetDir(File taskDir) {
    log.info("Moving target directory to " + taskDir.getAbsolutePath());
  }

  private List<File> tasks() {
    return Arrays.stream(workingDirectory.toFile().listFiles()).filter(File::isDirectory).toList();
  }

  public void interrupt() {
    terminated.set(true);
    while (active.get()) {
      log.debug("Thread is still active...");
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // we don't want to interrupt the worker thread before its task is done, since it might
        // render the application in a non-recoverable state
      }
    }
    log.debug("Task has been finished");
  }

}
