package life.qbic.data.processing.registration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class RegistrationConfiguration {


  private final Path workingDirectory;
  private final Path targetDirectory;
  private final String metadataFileName;

  public RegistrationConfiguration(String workingDirectory, String targetDirectory, String metadataFileName) {
    this.workingDirectory = Paths.get(Objects.requireNonNull(workingDirectory, "workingDirectory must not be null"));
    if (!workingDirectory().toFile().exists()) {
      throw new IllegalArgumentException(targetDirectory + " does not exist");
    }
    if (!workingDirectory().toFile().isDirectory()) {
      throw new IllegalArgumentException(targetDirectory + " is not a directory");
    }
    this.targetDirectory = Paths.get(Objects.requireNonNull(targetDirectory, "targetDirectories must not be null"));
    if (!targetDirectory().toFile().exists()) {
      throw new IllegalArgumentException(targetDirectory + " does not exist");
    }
    if (!targetDirectory().toFile().isDirectory()) {
      throw new IllegalArgumentException(targetDirectory + " is not a directory");
    }
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

  public String metadataFileName() { return metadataFileName; }
}
