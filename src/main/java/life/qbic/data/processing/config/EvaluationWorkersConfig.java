package life.qbic.data.processing.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.regex.Pattern;

public class EvaluationWorkersConfig {

  private final int threads;
  private final Path workingDirectory;
  private final Collection<Path> targetDirectories;
  private final Pattern measurementIdPattern;

  public EvaluationWorkersConfig(int threads, String workingDirectory, String measurementIdPattern,
      Collection<String> targetDirectories) {
    if (threads < 1) {
      throw new IllegalArgumentException(
          "Number of evaluation worker threads must be greater than 0");
    }
    if (targetDirectories.isEmpty()) {
      throw new IllegalArgumentException(
          "Target directories cannot be empty, please specify at least one target directory");
    }
    this.threads = threads;
    this.workingDirectory = Paths.get(workingDirectory);
    if (!this.workingDirectory.toFile().exists()) {
      throw new IllegalArgumentException("Evaluation worker directory does not exist");
    }
    this.targetDirectories = targetDirectories.stream().map(Paths::get).toList();
    this.targetDirectories.stream().filter(path -> !path.toFile().exists()).forEach(path -> {
      throw new IllegalArgumentException(
          "Evaluation target directory '%s' does not exist".formatted(path));
    });
    if (measurementIdPattern.isBlank()) {
      throw new IllegalArgumentException("Measurement id pattern cannot be blank");
    }
    this.measurementIdPattern = Pattern.compile(measurementIdPattern);
  }

  public int threads() {
    return threads;
  }

  public Path workingDirectory() {
    return workingDirectory;
  }

  public Collection<Path> targetDirectories() {
    return targetDirectories;
  }

  public Pattern measurementIdPattern() {
    return measurementIdPattern;
  }
}
