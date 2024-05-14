package life.qbic.data.processing.registration;

import static org.apache.logging.log4j.LogManager.getLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import life.qbic.data.processing.ConcurrentRegistrationQueue;
import life.qbic.data.processing.GlobalConfig;
import life.qbic.data.processing.Provenance;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;

/**
 * <b>Process Registration Request</b>
 * <p>
 * This must be the first process of handling a new incoming dataset. It will consume a processing
 * request item, that is then used to prepare the dataset for the following downstream processes.
 * <p>
 * The process polls the {@link life.qbic.data.processing.ConcurrentRegistrationQueue} shared with
 * the scanning thread.
 *
 * <p>
 * The process will do the following tasks:
 * <ul>
 *   <li>Wraps a new dataset into a task directory with a random UUID as task name</li>
 *   <li>Creating a provenance JSON file, that is used on downstream processes and holds required provenance data</li>
 *   <li>Moving the dataset to the next processing directory</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class ProcessRegistrationRequest extends Thread {

  private static final Logger log = getLogger(ProcessRegistrationRequest.class);
  private static final String threadName = "Registration-%s";
  private static int threadNumber = 1;
  private final ConcurrentRegistrationQueue registrationQueue;
  private final Path workingDirectory;
  private final Path targetDirectory;
  private final String metadataFileName;
  private final Path usersErrorDirectory;
  private AtomicBoolean active = new AtomicBoolean(false);

  public ProcessRegistrationRequest(@NonNull ConcurrentRegistrationQueue registrationQueue,
      @NonNull RegistrationConfiguration configuration, @NonNull GlobalConfig globalConfig) {
    this.setName(threadName.formatted(nextThreadNumber()));
    this.registrationQueue = registrationQueue;
    this.workingDirectory = configuration.workingDirectory();
    this.targetDirectory = configuration.targetDirectory();
    this.metadataFileName = configuration.metadataFileName();
    this.usersErrorDirectory = globalConfig.usersErrorDirectory();
  }

  private static int nextThreadNumber() {
    return threadNumber++;
  }

  private static void cleanup(Path workingTargetDir) throws IOException {
    try (var content = Files.walk(workingTargetDir)) {
      content.map(Path::toFile).sorted(Comparator.reverseOrder()).forEach(File::delete);
    }
  }

  private static void processMeasurement(String measurementId,
      Map<String, List<RegistrationMetadata>> aggregatedFilesByMeasurementId, Path workingTargetDir,
      Path taskDir) throws IOException {
    for (RegistrationMetadata metadataEntry : aggregatedFilesByMeasurementId.get(
        measurementId)) {
      Files.move(workingTargetDir.resolve(metadataEntry.file()),
          taskDir.resolve(metadataEntry.file()));
    }
  }

  private void moveBackToOrigin(Path target, Path usersHomePath, String reason) {
    log.info("Moving back to original user directory: {}", usersHomePath);
    try {
      Path taskDir = createTaskDirectory();
      Files.move(target, taskDir.resolve(target.getFileName()));
      var errorFile = taskDir.resolve("error.txt").toFile();
      errorFile.createNewFile();
      Files.writeString(errorFile.toPath(), reason);
      usersHomePath.resolve(usersErrorDirectory).toFile().mkdir();
      Files.move(taskDir,
          usersHomePath.resolve(usersErrorDirectory)
              .resolve(taskDir.toFile().getName()));
    } catch (IOException e) {
      log.error("Cannot move task to user intervention: %s".formatted(
          usersHomePath.resolve(usersErrorDirectory)), e);
    }
  }

  private void validateFileEntries(Collection<RegistrationMetadata> metadata, Path request)
      throws ValidationException {
    for (RegistrationMetadata metadataEntry : metadata) {
      if (!request.resolve(Paths.get(metadataEntry.file())).toFile().exists()) {
        throw new ValidationException(
            "Unknown file reference in metadata: %s".formatted(metadataEntry.file()),
            ErrorCode.FILE_NOT_FOUND);
      }
    }
    // To save the user from any trouble, let's also check if all dataset files are described by a metadata entry
    var filesInMetadata = metadata.stream().map(RegistrationMetadata::file).toList();
    var physicalFiles = request.toFile().listFiles();
    for (File file : physicalFiles) {
      // we ignore hidden files
      if (file.isHidden()) {
        continue;
      }
      if (file.getName().endsWith(metadataFileName)) {
        continue;
      }
      if (!filesInMetadata.contains(file.getName())) {
        throw new ValidationException(
            "Found more files than described in the metadata file: %s".formatted(file.getName()),
            ErrorCode.MISSING_FILE_ENTRY);
      }
    }
  }

  private List<RegistrationMetadata> findAndParseMetadata(Path request) throws ValidationException {
    Optional<File> metadataFile = findMetadataFile(request);
    if (metadataFile.isEmpty()) {
      throw new ValidationException("Metadata file does not exist",
          ErrorCode.METADATA_FILE_NOT_FOUND);
    }

    List<String> content;
    try {
      content = new ArrayList<>(Files.readAllLines(Paths.get(metadataFile.get().getPath())));
    } catch (IOException e) {
      log.error("Error reading metadata file", e);
      throw new ValidationException("Cannot read metadata file", ErrorCode.IO_EXCEPTION);
    }

    return content.stream().map(this::parseMetadataRow).toList();
  }

  private RegistrationMetadata parseMetadataRow(String value) throws ValidationException {
    try {
      var splitValues = value.split("\t");
      return new RegistrationMetadata(splitValues[0], splitValues[1]);
    } catch (IndexOutOfBoundsException e) {
      log.error("Error parsing metadata row: %s".formatted(value), e);
      throw new ValidationException("Cannot parse metadata entry", ErrorCode.INCOMPLETE_METADATA);
    }
  }

  private Optional<File> findMetadataFile(Path path) {
    for (File file : Objects.requireNonNull(path.toFile().listFiles())) {
      if (file.isFile() && file.getName().endsWith(metadataFileName)) {
        return Optional.of(file);
      }
    }
    return Optional.empty();
  }

  private void writeProvenanceInformation(Path taskDir, Path newLocation,
      RegistrationRequest request, String measurementId,
      List<String> datasetFiles)
      throws IOException {
    Provenance provenance = new Provenance();
    provenance.originPath = request.origin().toString();
    provenance.history = new ArrayList<>();
    provenance.history.add(newLocation.toString());
    provenance.userWorkDirectoryPath = String.valueOf(request.userPath());
    provenance.qbicMeasurementID = measurementId;
    provenance.addDatasetFiles(datasetFiles);
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

  @Override
  public void run() {
    while (true) {
      var request = registrationQueue.poll();
      active.set(true);
      log.info("Processing request: {}", request);
      var intermediateTaskDir = createTaskDirectory();
      try {
        // Let's first move the registration request content to the working directory of the process

        Files.move(request.target(), intermediateTaskDir.resolve(request.target().getFileName()));
        var workingTargetDir = intermediateTaskDir.resolve(request.target().getFileName());

        var registrationMetadata = findAndParseMetadata(workingTargetDir);
        validateFileEntries(registrationMetadata, workingTargetDir);

        var aggregatedFilesByMeasurementId = registrationMetadata.stream().collect(
            Collectors.groupingBy(RegistrationMetadata::measurementId));

        processAll(aggregatedFilesByMeasurementId, workingTargetDir, request);

        // Finally clean up the task directory, which should only contain the original metadata file
        cleanup(workingTargetDir);
      } catch (ValidationException e) {
        log.error("Failed validation processing request: %s".formatted(request), e);
        moveBackToOrigin(intermediateTaskDir, request.userPath(), e.getMessage());
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

  private void processAll(Map<String, List<RegistrationMetadata>> aggregatedFilesByMeasurementId,
      Path workingTargetDir, RegistrationRequest request) throws IOException {
    for (String measurementId : aggregatedFilesByMeasurementId.keySet()) {
      // We now create individual task directories for every measurement dataset
      Path taskDir = createTaskDirectory();
      processMeasurement(measurementId, aggregatedFilesByMeasurementId, workingTargetDir, taskDir);

      writeProvenanceInformation(taskDir, targetDirectory, request, measurementId,
          aggregatedFilesByMeasurementId.get(measurementId).stream()
              .map(RegistrationMetadata::file).toList());
      Files.move(taskDir, targetDirectory.resolve(taskDir.getFileName()));
    }
  }

}
