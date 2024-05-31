package life.qbic.data.processing.registration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import life.qbic.data.processing.AccessRightsEvaluation;

public class RegistrationConfiguration {


  private final Path workingDirectory;
  private final Path targetDirectory;
  private final String metadataFileName;

  public RegistrationConfiguration(String workingDirectory, String targetDirectory,
      String metadataFileName)
      throws IOException {
    this.workingDirectory = Paths.get(
        Objects.requireNonNull(workingDirectory, "workingDirectory must not be null"));
    AccessRightsEvaluation.evaluateExistenceAndDirectory(this.workingDirectory);
    AccessRightsEvaluation.evaluateWriteAndExecutablePermission(this.workingDirectory);
    this.targetDirectory = Paths.get(
        Objects.requireNonNull(targetDirectory, "targetDirectories must not be null"));
    AccessRightsEvaluation.evaluateExistenceAndDirectory(this.targetDirectory);
    AccessRightsEvaluation.evaluateWriteAndExecutablePermission(this.targetDirectory);

    if (metadataFileName == null || metadataFileName.isEmpty()) {
      throw new IllegalArgumentException("metadataFileName must not be null or empty");
    }

    this.metadataFileName = metadataFileName;
  }

  public Path workingDirectory() {
    return workingDirectory;
  }

  public Path targetDirectory() {
    return targetDirectory;
  }

  public String metadataFileName() {
    return metadataFileName;
  }
}
