package life.qbic.data.processing;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.logging.log4j.Logger;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class Scanner extends Thread {

  private static final Logger log = getLogger(Scanner.class);
  private static final Path REGISTRATION_PATH = Paths.get("registration");
  private final ScannerConfiguration configuration;
  private final HashSet<Path> userProcessDirectories = new HashSet<>();
  private final ConcurrentEventQueue eventQueue;

  public Scanner(ScannerConfiguration scannerConfiguration, ConcurrentEventQueue eventQueue) {
    this.setName("Scanner-Thread");
    this.configuration = Objects.requireNonNull(scannerConfiguration,
        "scannerConfiguration must not be null");
    this.eventQueue = Objects.requireNonNull(eventQueue, "eventQueue must not be null");
  }

  @Override
  public void run() {
    log.info("Started scanning '%s'".formatted(configuration.scannerDirectory()));
    var scannerPath = Paths.get(configuration.scannerDirectory());
    while (!Thread.interrupted()) {
      try {
        var userFolderIterator = Arrays.stream(
                Objects.requireNonNull(scannerPath.toFile().listFiles())).filter(File::isDirectory)
            .toList().iterator();

        while (userFolderIterator.hasNext()) {
          fetchRegistrationDirectory(userFolderIterator.next().toPath()).ifPresent(
              this::addRegistrationDirectory);
        }

        removePathZombies();

        Thread.sleep(100);
      } catch (InterruptedException e) {
        interrupt();
      }
    }
  }

  private void removePathZombies() {
    List<Path> zombies = new LinkedList<>();
    for (Path processFolder : userProcessDirectories) {
      if (!processFolder.toFile().exists()) {
        zombies.add(processFolder);
      }
    }

    zombies.forEach(zombie -> {
      userProcessDirectories.remove(zombie);
      log.warn("Removing orphaned process directory: '%s'".formatted(zombie));
    });

  }

  private void addRegistrationDirectory(Path path) {
    if (userProcessDirectories.add(path)) {
      log.info("New user process directory found: '%s'".formatted(path.toString()));
    }
  }

  public Optional<Path> fetchRegistrationDirectory(Path userDirectory) {
    Path resolvedPath = userDirectory.resolve(REGISTRATION_PATH);
    return Optional.ofNullable(resolvedPath.toFile().exists() ? resolvedPath : null);
  }

  @Override
  public void interrupt() {
    log.warn("Received interrupt signal, cleaning up and shutting down.");
  }


}
