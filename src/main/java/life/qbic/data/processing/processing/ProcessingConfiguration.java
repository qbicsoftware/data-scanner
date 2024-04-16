package life.qbic.data.processing.processing;

import java.nio.file.Path;

/**
 * <b>Processing Configuration</b>
 *
 * <p>Holds processing worker configuration settings, such as the working directory of the process
 * and the next target directory the dataset will be moved to, after a successful task
 * performance.</p>
 *
 * @since 1.0.0
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
