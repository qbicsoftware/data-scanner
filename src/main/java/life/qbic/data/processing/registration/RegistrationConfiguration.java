package life.qbic.data.processing.registration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class RegistrationConfiguration {


  private final Path workingDirectory;
  private final Path targetDirectory;

  public RegistrationConfiguration(String workingDirectory, String targetDirectory) {
    this.workingDirectory = Paths.get(Objects.requireNonNull(workingDirectory, "workingDirectory must not be null"));
    if (!workingDirectory().toFile().exists()) {
      throw new IllegalArgumentException(targetDirectory + " does not exist");
    }
    if (!workingDirectory().toFile().isDirectory()) {
      throw new IllegalArgumentException(targetDirectory + " is not a directory");
    }
    this.targetDirectory = Paths.get(Objects.requireNonNull(targetDirectory, "targetDirectory must not be null"));
    if (!targetDirectory().toFile().exists()) {
      throw new IllegalArgumentException(targetDirectory + " does not exist");
    }
    if (!targetDirectory().toFile().isDirectory()) {
      throw new IllegalArgumentException(targetDirectory + " is not a directory");
    }
  }

  public Path workingDirectory() {
    return workingDirectory;
  }

  public Path targetDirectory() {
    return targetDirectory;
  }
}
