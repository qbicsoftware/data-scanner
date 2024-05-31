package life.qbic.data.processing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class GlobalConfig {

  private final Path usersErrorDirectoryName;

  private final Path usersDirectoryRegistrationName;

  private final Pattern qbicMeasurementIdPattern;

  public GlobalConfig(String usersErrorDirectoryName, String usersRegistrationDirectoryName,
      String qbicMeasurementIdPattern) {
    if (usersErrorDirectoryName == null || usersErrorDirectoryName.isBlank()) {
      throw new IllegalArgumentException("usersErrorDirectoryName cannot be null or empty");
    }
    if (usersRegistrationDirectoryName == null || usersRegistrationDirectoryName.isBlank()) {
      throw new IllegalArgumentException("usersRegistrationDirectoryName cannot be null or empty");
    }
    if (qbicMeasurementIdPattern == null || qbicMeasurementIdPattern.isBlank()) {
      throw new IllegalArgumentException("qbicMeasurementIdPattern cannot be null or empty");
    }
    this.usersErrorDirectoryName = Paths.get(usersErrorDirectoryName);
    this.usersDirectoryRegistrationName = Paths.get(usersRegistrationDirectoryName);
    this.qbicMeasurementIdPattern = Pattern.compile(qbicMeasurementIdPattern);
  }

  public Path usersErrorDirectory() {
    return this.usersErrorDirectoryName;
  }

  public Path usersDirectoryRegistration() {
    return this.usersDirectoryRegistrationName;
  }

  public Pattern qbicMeasurementIdPattern() {
    return this.qbicMeasurementIdPattern;
  }

}
