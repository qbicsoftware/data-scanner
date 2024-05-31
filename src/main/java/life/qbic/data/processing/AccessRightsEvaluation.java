package life.qbic.data.processing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * <b>Access and File Evaluation helper class</b>
 *
 * @since 1.0.0
 */
public class AccessRightsEvaluation {

  /**
   * Convenience method that checks if the application has write and executable permission for the
   * given file.
   *
   * @param file the path of the file of interest to evaluate
   * @throws IOException if one or both of the evaluated conditions are failing
   * @since 1.0.0
   */
  public static void evaluateWriteAndExecutablePermission(Path file) throws IOException {
    evaluateWriteAndExecutablePermission(file.toFile());
  }

  /**
   * Convenience method that checks if the application has write and executable permission for the
   * given file.
   *
   * @param file the file of interest to evaluate
   * @throws IOException if one or both of the evaluated conditions are failing
   * @since 1.0.0
   */
  public static void evaluateWriteAndExecutablePermission(File file) throws IOException {
    if (!file.canWrite()) {
      throw new IOException("Cannot write to file " + file);
    }
    if (!file.canExecute()) {
      throw new IOException("Cannot execute file " + file);
    }
  }

  /**
   * Convenience method that checks if file exists and is a directory.
   *
   * @param file the path of the file of interest to evaluate
   * @throws IOException if neither of the evaluated conditions are failing
   * @since 1.0.0
   */
  public static void evaluateExistenceAndDirectory(Path file) throws IOException {
    evaluateExistenceAndDirectory(file.toFile());
  }

  /**
   * Convenience method that checks if file exists and is a directory.
   *
   * @param file the path of the file of interest to evaluate
   * @throws IOException if neither of the evaluated conditions are failing
   * @since 1.0.0
   */
  public static void evaluateExistenceAndDirectory(File file) throws IOException {
    if (!file.exists()) {
      throw new IOException("File does not exist " + file);
    }
    if (!file.isDirectory()) {
      throw new IOException("File is not a directory " + file);
    }
  }


}
