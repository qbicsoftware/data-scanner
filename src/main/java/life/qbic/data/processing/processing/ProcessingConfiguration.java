package life.qbic.data.processing.processing;

import java.nio.file.Path;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class ProcessingConfiguration {

  private final Path workingDirectory;

  private final Path targetDirectory;

  public ProcessingConfiguration(Path workingDirectory, Path targetDirectory) {
    this.workingDirectory = workingDirectory;
    if (!workingDirectory.toFile().exists()) {
      throw new IllegalArgumentException("Working directory does not exist: " + workingDirectory);
    }
    this.targetDirectory = targetDirectory;
    if (!targetDirectory.toFile().exists()) {
      throw new IllegalArgumentException("Target directory does not exist: " + targetDirectory);
    }
  }

  public Path getWorkingDirectory() {
    return workingDirectory;
  }

  public Path getTargetDirectory() {
    return targetDirectory;
  }
}
