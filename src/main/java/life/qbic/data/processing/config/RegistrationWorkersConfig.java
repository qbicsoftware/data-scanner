package life.qbic.data.processing.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class RegistrationWorkersConfig {

  private final int amountOfWorkers;

  private final Path workingDirectory;

  private final Path targetDirectory;

  public RegistrationWorkersConfig(int amountOfWorkers, String workingDirectory, String targetDirectory) {
    if (amountOfWorkers < 1) {
      throw new IllegalArgumentException("Number of workers must be greater than 0");
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
    this.amountOfWorkers = amountOfWorkers;
    this.targetDirectory = targetDirectoryPath;
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
}
