package life.qbic.data.processing.evaluation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import life.qbic.data.processing.GlobalConfig;

/**
 * <b>Evaluation Configuration</b>
 * <p>
 * The configuration class for {@link EvaluationRequest} workers.
 *
 * @since 1.0.0
 */
public class EvaluationConfiguration {

  private final Path workingDirectory;
  private final Path targetDirectory;
  private final Pattern measurementIdPattern;
  private final Path usersErrorDirectory;

  public EvaluationConfiguration(String workingDirectory, String targetDirectory,
      String measurementIdPattern,
      GlobalConfig globalConfig) {
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
    this.usersErrorDirectory = globalConfig.usersErrorDirectory();
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

  public Path usersErrorDirectory() {
    return usersErrorDirectory;
  }

}
