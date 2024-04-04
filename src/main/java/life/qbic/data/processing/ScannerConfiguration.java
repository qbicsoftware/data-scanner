package life.qbic.data.processing;

import org.springframework.stereotype.Component;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class ScannerConfiguration {

  private final String scannerDirectory;

  public ScannerConfiguration(String scannerDirectory) {
    this.scannerDirectory = scannerDirectory;
  }

  public String scannerDirectory() {
    return scannerDirectory;
  }
}
