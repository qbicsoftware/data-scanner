package life.qbic.data.processing.scanner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import life.qbic.data.processing.AccessRightsEvaluation;

public class ScannerConfiguration {

  private final Path scannerDirectory;
  private final int scanInterval;
  private final String[] ignore;

  public ScannerConfiguration(String scannerDirectory, int interval, String[] ignore)
      throws IOException {
    this.scannerDirectory = Paths.get(scannerDirectory);
    if (interval <= 0) {
      throw new IllegalArgumentException("Interval must be greater than 0");
    }
    AccessRightsEvaluation.evaluateExistenceAndDirectory(this.scannerDirectory);
    AccessRightsEvaluation.evaluateWriteAndExecutablePermission(this.scannerDirectory);
    this.scanInterval = interval;
    this.ignore = Arrays.copyOf(Objects.requireNonNull(ignore), ignore.length);
  }

  public String scannerDirectory() {
    return scannerDirectory.toString();
  }

  public int scanInterval() {
    return scanInterval;
  }

  public Collection<String> ignore() {
    return Arrays.stream(ignore).toList();
  }
}
