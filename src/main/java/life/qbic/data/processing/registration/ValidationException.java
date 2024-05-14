package life.qbic.data.processing.registration;

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
