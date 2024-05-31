package life.qbic.data.processing.processing;

import java.io.IOException;
import java.nio.file.Path;
import life.qbic.data.processing.AccessRightsEvaluation;

/**
 * <b>Processing Configuration</b>
 *
 * <p>Holds processing worker configuration settings, such as the working directory of the process
 * and the next target directory the dataset will be moved to, after a successful task
 * performance.</p>
 *
 * @since 1.0.0
 */
public class ProcessingConfiguration {

  private final Path workingDirectory;

  private final Path targetDirectory;

  public ProcessingConfiguration(Path workingDirectory, Path targetDirectory) throws IOException {
    this.workingDirectory = workingDirectory;
    AccessRightsEvaluation.evaluateExistenceAndDirectory(this.workingDirectory);
    AccessRightsEvaluation.evaluateWriteAndExecutablePermission(this.workingDirectory);
    this.targetDirectory = targetDirectory;
    AccessRightsEvaluation.evaluateExistenceAndDirectory(this.targetDirectory);
    AccessRightsEvaluation.evaluateWriteAndExecutablePermission(this.workingDirectory);
  }

  public Path getWorkingDirectory() {
    return workingDirectory;
  }

  public Path getTargetDirectory() {
    return targetDirectory;
  }
}
