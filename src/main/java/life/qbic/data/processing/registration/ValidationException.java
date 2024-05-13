package life.qbic.data.processing.registration;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class ValidationException extends RuntimeException {

  private final ErrorCode errorCode;

  public ValidationException(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public ValidationException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public ErrorCode errorCode() {
    return errorCode;
  }
}
