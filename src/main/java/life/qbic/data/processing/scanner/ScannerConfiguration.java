package life.qbic.data.processing.scanner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class ScannerConfiguration {

  private final String scannerDirectory;
  private final int scanInterval;
  private final String[] ignore;

  public ScannerConfiguration(String scannerDirectory, int interval, String[] ignore) {
    this.scannerDirectory = scannerDirectory;
    if (interval <= 0) {
      throw new IllegalArgumentException("Interval must be greater than 0");
    }
    this.scanInterval = interval;
    this.ignore = Arrays.copyOf(Objects.requireNonNull(ignore), ignore.length);
  }

  public String scannerDirectory() {
    return scannerDirectory;
  }

  public int scanInterval() {
    return scanInterval;
  }

  public Collection<String> ignore () {
    return Arrays.stream(ignore).toList();
  }
}
