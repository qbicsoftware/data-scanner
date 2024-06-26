package life.qbic.data.processing.config;

import java.nio.file.Path;

public class ProcessingWorkersConfig {

  private final int threads;

  private final Path workingDirectory;

  private final Path targetDirectory;

  public ProcessingWorkersConfig(int threads, Path workingDirectory, Path targetDirectory) {
    if (threads < 1) {
      throw new IllegalArgumentException("threads must be greater than 0");
    }
    this.threads = threads;

    if (!workingDirectory.toFile().exists()) {
      throw new IllegalArgumentException("working directory does not exist");
    }
    this.workingDirectory = workingDirectory;

    if (!targetDirectory.toFile().exists()) {
      throw new IllegalArgumentException("target directory does not exist");
    }
    this.targetDirectory = targetDirectory;
  }

  public int threads() {
    return threads;
  }

  public Path workingDirectory() {
    return workingDirectory;
  }

  public Path targetDirectory() {
    return targetDirectory;
  }
}
