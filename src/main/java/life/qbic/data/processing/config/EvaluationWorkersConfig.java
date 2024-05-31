package life.qbic.data.processing.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class EvaluationWorkersConfig {

  private final int threads;
  private final Path workingDirectory;
  private final Collection<Path> targetDirectories;

  public EvaluationWorkersConfig(int threads, String workingDirectory,
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
}
