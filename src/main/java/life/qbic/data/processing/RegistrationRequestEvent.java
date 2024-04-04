package life.qbic.data.processing;

import java.nio.file.Path;
import java.time.Instant;

/**
 * <b><record short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public record RegistrationRequestEvent(Instant timestamp, Path origin, Path target) {

}
