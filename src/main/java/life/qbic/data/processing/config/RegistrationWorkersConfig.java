package life.qbic.data.processing.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RegistrationWorkersConfig {

  private final int amountOfWorkers;

  private final Path workingDirectory;

  private final Path targetDirectory;

  private final String metadataFileName;

  public RegistrationWorkersConfig(int threads, String workingDirectory, String targetDirectory, String metadataFileName) {
    if (threads < 1) {
      throw new IllegalArgumentException("Number of threads must be greater than 0");
    }
    Path directory = Paths.get(workingDirectory);
    if (!directory.toFile().exists()) {
      throw new IllegalArgumentException("Directory " + directory + " does not exist");
    }
    Path targetDirectoryPath = Paths.get(targetDirectory);
    if (!targetDirectoryPath.toFile().exists()) {
      throw new IllegalArgumentException("Target directory " + targetDirectory + " does not exist");
    }
    this.workingDirectory = directory;
    this.amountOfWorkers = threads;
    this.targetDirectory = targetDirectoryPath;
    this.metadataFileName = metadataFileName;
  }

  public int amountOfWorkers() {
    return amountOfWorkers;
  }

  public Path workingDirectory() {
    return this.workingDirectory;
  }

  public Path targetDirectory() {
    return this.targetDirectory;
  }

  public String metadataFileName() {
    return this.metadataFileName;
  }
}
