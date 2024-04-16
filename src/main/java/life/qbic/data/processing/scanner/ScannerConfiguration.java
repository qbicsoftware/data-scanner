package life.qbic.data.processing.scanner;

public class ScannerConfiguration {

  private final String scannerDirectory;
  private final int scanInterval;

  public ScannerConfiguration(String scannerDirectory, int interval) {
    this.scannerDirectory = scannerDirectory;
    if (interval <= 0) {
      throw new IllegalArgumentException("Interval must be greater than 0");
    }
    this.scanInterval = interval;
  }

  public String scannerDirectory() {
    return scannerDirectory;
  }

  public int scanInterval() {
    return scanInterval;
  }
}
