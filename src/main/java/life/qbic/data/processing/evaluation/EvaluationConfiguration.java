package life.qbic.data.processing.evaluation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import life.qbic.data.processing.AccessRightsEvaluation;
import life.qbic.data.processing.GlobalConfig;
import life.qbic.data.processing.config.RoundRobinDraw;

/**
 * <b>Evaluation Configuration</b>
 * <p>
 * The configuration class for {@link EvaluationRequest} workers.
 *
 * @since 1.0.0
 */
public class EvaluationConfiguration {

  private final Path workingDirectory;
  private final Collection<Path> targetDirectories;
  private final Path usersErrorDirectory;
  private final RoundRobinDraw<Path> targetDirectoriesRoundRobinDraw;

  public EvaluationConfiguration(String workingDirectory, Collection<Path> targetDirectories,
      GlobalConfig globalConfig) throws IOException {
    this.workingDirectory = Paths.get(workingDirectory);
    AccessRightsEvaluation.evaluateExistenceAndDirectory(this.workingDirectory);
    AccessRightsEvaluation.evaluateWriteAndExecutablePermission(this.workingDirectory);
    this.targetDirectories = targetDirectories.stream().toList();
    for (Path targetDirectory : this.targetDirectories) {
      AccessRightsEvaluation.evaluateExistenceAndDirectory(targetDirectory);
      AccessRightsEvaluation.evaluateWriteAndExecutablePermission(targetDirectory);
    }
    this.targetDirectoriesRoundRobinDraw = RoundRobinDraw.create(targetDirectories);
    this.usersErrorDirectory = globalConfig.usersErrorDirectory();
  }

  public Path workingDirectory() {
    return workingDirectory;
  }

  public RoundRobinDraw<Path> targetDirectories() {
    return targetDirectoriesRoundRobinDraw;
  }

  public Path usersErrorDirectory() {
    return usersErrorDirectory;
  }

}
