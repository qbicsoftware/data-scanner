package life.qbic.data.processing.evaluation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class EvaluationConfiguration {

  private final Path workingDirectory;
  private final Path targetDirectory;
  private final Pattern measurementIdPattern;

  public EvaluationConfiguration(String workingDirectory, String targetDirectory, String measurementIdPattern) {
    this.workingDirectory = Paths.get(workingDirectory);
    if (!this.workingDirectory.toFile().exists()) {
      throw new IllegalArgumentException("Evaluation worker directory does not exist");
    }
    this.targetDirectory = Paths.get(targetDirectory);
    if (!this.targetDirectory.toFile().exists()) {
      throw new IllegalArgumentException("Evaluation target directory does not exist");
    }
    if (measurementIdPattern.isBlank()) {
      throw new IllegalArgumentException("Measurement id pattern cannot be blank");
    }
    this.measurementIdPattern = Pattern.compile(measurementIdPattern);
  }

  public Path workingDirectory() {
    return workingDirectory;
  }

  public Path targetDirectory() {
    return targetDirectory;
  }

  public Pattern measurementIdPattern() {
    return measurementIdPattern;
  }

}
