package life.qbic.data.processing.registration;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

public record RegistrationRequest(Instant timestamp, long lastModified, Path origin, Path target, Path userPath) {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RegistrationRequest that = (RegistrationRequest) o;
    return Objects.equals(origin, that.origin) && Objects.equals(target,
        that.target) && Objects.equals(lastModified, that.lastModified);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lastModified, origin, target);
  }
}
