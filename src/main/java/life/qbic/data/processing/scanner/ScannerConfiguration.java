package life.qbic.data.processing.scanner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class ScannerConfiguration {

  private final Path scannerDirectory;
  private final int scanInterval;
  private final String[] ignore;

  public ScannerConfiguration(String scannerDirectory, int interval, String[] ignore) {
    this.scannerDirectory = Paths.get(scannerDirectory);
    if (interval <= 0) {
      throw new IllegalArgumentException("Interval must be greater than 0");
    }
    if (!this.scannerDirectory.toFile().exists()) {
      throw new IllegalArgumentException("Scanner directory does not exist");
    }
    if (!this.scannerDirectory.toFile().canExecute()) {
      throw new IllegalArgumentException("Scanner directory does not contain executable permissions");
    }
    if (!this.scannerDirectory.toFile().canWrite()) {
      throw new IllegalArgumentException("Scanner directory does not contain write permissions");
    }
    this.scanInterval = interval;
    this.ignore = Arrays.copyOf(Objects.requireNonNull(ignore), ignore.length);
  }

  public String scannerDirectory() {
    return scannerDirectory.toString();
  }

  public int scanInterval() {
    return scanInterval;
  }

  public Collection<String> ignore () {
    return Arrays.stream(ignore).toList();
  }
}
