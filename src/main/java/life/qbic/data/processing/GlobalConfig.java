package life.qbic.data.processing;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GlobalConfig {

  private final Path usersErrorDirectoryName;

  private final Path usersDirectoryRegistrationName;

  public GlobalConfig(String usersErrorDirectoryName, String usersRegistrationDirectoryName) {
    if (usersErrorDirectoryName == null || usersErrorDirectoryName.isBlank()) {
      throw new IllegalArgumentException("usersErrorDirectoryName cannot be null or empty");
    }
    if (usersRegistrationDirectoryName == null || usersRegistrationDirectoryName.isBlank()) {
      throw new IllegalArgumentException("usersRegistrationDirectoryName cannot be null or empty");
    }
    this.usersErrorDirectoryName = Paths.get(usersErrorDirectoryName);
    this.usersDirectoryRegistrationName = Paths.get(usersRegistrationDirectoryName);
  }

  public Path usersErrorDirectory() {
    return this.usersErrorDirectoryName;
  }

  public Path usersDirectoryRegistration() {
    return this.usersDirectoryRegistrationName;
  }

}
