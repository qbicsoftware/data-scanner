package life.qbic.data.processing.scanner;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import life.qbic.data.processing.ConcurrentRegistrationQueue;
import life.qbic.data.processing.GlobalConfig;
import life.qbic.data.processing.registration.RegistrationRequest;
import org.apache.logging.log4j.Logger;

/**
 * <b>Scanner</b>
 * <p>
 * The scanner thread iterates over the specified directory and records every directory present as
 * "user" directory.
 * <p>
 * In the current configuration, the scanner detects activity in a directory named "registration"
 * within each user directory. Every event outside this directory is ignored, since data that is
 * currently uploaded must not be processed, which will lead to corrupt data.
 *
 * @since 1.0.0
 */
public class Scanner extends Thread {

  private static final Logger log = getLogger(Scanner.class);
  private static final Path REGISTRATION_PATH = Paths.get("registration");

  private final Path scannerPath;
  private final int scanInterval;
  private final HashSet<Path> userProcessDirectories = new HashSet<>();
  private final ConcurrentRegistrationQueue registrationQueue;
  private final HashSet<RegistrationRequest> submittedRequests = new HashSet<>();
  private final Set<String> ignoredDirectories = new HashSet<>();

  public Scanner(ScannerConfiguration scannerConfiguration,
      ConcurrentRegistrationQueue registrationQueue, GlobalConfig globalConfig) {
    this.setName("Scanner-Thread");
    Objects.requireNonNull(scannerConfiguration, "scannerConfiguration must not be null");
    scannerPath = Path.of(scannerConfiguration.scannerDirectory());
    if (!scannerPath.toFile().exists()) {
      throw new RuntimeException("Could not find scanner directory: " + scannerPath);
    }
    this.scanInterval = scannerConfiguration.scanInterval();
    this.registrationQueue = Objects.requireNonNull(registrationQueue,
        "registrationQueue must not be null");
    this.ignoredDirectories.addAll(scannerConfiguration.ignore());
    if (!this.ignoredDirectories.isEmpty()) {
      log.info("Ignoring {} directories", ignoredDirectories.size());
    }
  }

  private boolean notToIgnore(String filename) {
    return !ignoredDirectories.contains(filename);
  }

  @Override
  public void run() {
    log.info("Started scanning '{}'", scannerPath);
    while (!Thread.interrupted()) {
      try {
        var userFolderIterator = Arrays.stream(
                Objects.requireNonNull(scannerPath.toFile().listFiles())).filter(File::isDirectory)
            .filter(file -> notToIgnore(file.getName()))
            .toList().iterator();

        while (userFolderIterator.hasNext()) {
          fetchRegistrationDirectory(userFolderIterator.next().toPath()).ifPresent(
              this::addRegistrationDirectory);
        }

        List<RegistrationRequest> requests = detectDataForRegistration();
        for (RegistrationRequest request : requests) {
          if (submittedRequests.contains(request)) {
            log.info("Skipping registration request '{}'", request);
            continue;
          }
          registrationQueue.add(request);
          submittedRequests.add(request);
          log.info("New registration requested: {}", request);
        }
        removePathZombies();
        Thread.sleep(scanInterval);
      } catch (InterruptedException e) {
        interrupt();
      }
    }
    log.info("Stopped scanning '{}'", scannerPath);
  }

  private List<RegistrationRequest> detectDataForRegistration() {
    return userProcessDirectories.parallelStream()
        .map(Path::toFile)
        .filter(this::matchesAccessRightsCriteria)
        .filter(this::matchesRegistrationCriteria)
        .map(processDir -> createRequests(
            applyFilterForProcessDir(processDir.listFiles()),
            processDir.toPath()))
        .flatMap(Collection::stream).toList();
  }

  private File[] applyFilterForProcessDir(File[] processDirContent) {
    return Arrays.stream(Objects.requireNonNull(processDirContent))
        .filter(this::matchesAccessRightsCriteria)
        .filter(this::matchesRegistrationCriteria).toArray(File[]::new);
  }

  private boolean matchesAccessRightsCriteria(File file) {
    if (!file.canWrite()) {
      log.error("Cannot write to file '{}'", file);
      return false;
    }
    if (!file.canExecute()) {
      log.error("Cannot execute file '{}'", file);
      return false;
    }
    return true;
  }

  private boolean matchesRegistrationCriteria(File file) {
    if (file.isHidden()) {
      return false;
    }
    return file.isDirectory();
  }

  private List<RegistrationRequest> createRequests(File[] files, Path userDirectory) {
    if (files == null || files.length == 0) {
      return new ArrayList<>();
    }
    return Arrays.stream(files).map(file -> createRequest(file, userDirectory)).toList();
  }

  private RegistrationRequest createRequest(File file, Path userDirectory) {
    return new RegistrationRequest(Instant.now(), file.lastModified(),
        file.getParentFile().toPath(), file.toPath(), userDirectory.getParent());
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
      log.info("New user process directory found: '{}'", path.toString());
    }
  }

  public Optional<Path> fetchRegistrationDirectory(Path userDirectory) {
    Path resolvedPath = userDirectory.resolve(REGISTRATION_PATH);
    return Optional.ofNullable(resolvedPath.toFile().exists() ? resolvedPath : null);
  }

  @Override
  public void interrupt() {
    log.info("Interrupted scanning '{}'", scannerPath);
  }
}
