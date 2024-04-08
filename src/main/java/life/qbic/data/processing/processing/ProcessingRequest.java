package life.qbic.data.processing.processing;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Logger;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class ProcessingRequest extends Thread {

  private static final Logger log = getLogger(ProcessingRequest.class);
  private final Path workingDirectory;
  private final Path targetDirectory;
  private static final Set<String> activeTasks = new HashSet<>();
  private static final ReentrantLock lock = new ReentrantLock();
  private final AtomicBoolean active = new AtomicBoolean(false);

  public ProcessingRequest(ProcessingConfiguration processingConfiguration) {
    this.workingDirectory = processingConfiguration.getWorkingDirectory();
    this.targetDirectory = processingConfiguration.getTargetDirectory();
  }

  @Override
  public void run() {

  }

  public void interrupt() {
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
