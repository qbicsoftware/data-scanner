package life.qbic.data.processing.registration;

import static org.apache.logging.log4j.LogManager.getLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import life.qbic.data.processing.ConcurrentRegistrationQueue;
import life.qbic.data.processing.Provenance;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class ProcessRegistrationRequest extends Thread {

  private static final Logger log = getLogger(ProcessRegistrationRequest.class);
  private static final String threadName = "Registration-%s";
  private static int threadNumber = 1;
  private final ConcurrentRegistrationQueue registrationQueue;
  private final Path workingDirectory;
  private final Path targetDirectory;
  private AtomicBoolean active = new AtomicBoolean(false);

  public ProcessRegistrationRequest(@NonNull ConcurrentRegistrationQueue registrationQueue,
      @NonNull RegistrationConfiguration configuration) {
    this.setName(threadName.formatted(nextThreadNumber()));
    this.registrationQueue = registrationQueue;
    this.workingDirectory = configuration.workingDirectory();
    this.targetDirectory = configuration.targetDirectory();
  }

  private static int nextThreadNumber() {
    return threadNumber++;
  }

  @Override
  public void run() {
    while (true) {
      var request = registrationQueue.poll();
      active.set(true);
      log.info("Processing request: {}", request);
      try {
        Path taskDir = createTaskDirectory();
        Path newLocation = taskDir.resolve(request.target().getFileName());
        Files.move(request.target(), newLocation);
        writeProvenanceInformation(taskDir, newLocation, request);
        Files.move(taskDir, targetDirectory.resolve(taskDir.getFileName()));
      } catch (RuntimeException e) {
        log.error("Error moving task directory", e);
        // TODO move back to user folder
      } catch (IOException e) {
        log.error("Error while processing registration request", e);
        // TODO move back to user folder
      } finally {
        active.set(false);
        log.info("Processing completed: {}", request);
      }
    }
  }

  private void writeProvenanceInformation(Path taskDir, Path newLocation, RegistrationRequest request)
      throws IOException {
    Provenance provenance = new Provenance();
    provenance.originPath = request.origin().toString();
    provenance.history = new ArrayList<>();
    provenance.history.add(newLocation.toString());
    ObjectMapper mapper = new ObjectMapper();
    mapper.writerWithDefaultPrettyPrinter()
        .writeValue(taskDir.resolve("provenance.json").toFile(), provenance);
  }

  private Path createTaskDirectory() {
    UUID taskId = UUID.randomUUID();
    var taskDir = workingDirectory.resolve(taskId.toString());
    taskDir.toFile().mkdirs();
    return workingDirectory.resolve(taskId.toString());
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
