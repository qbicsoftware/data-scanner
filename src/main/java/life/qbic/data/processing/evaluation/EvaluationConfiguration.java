package life.qbic.data.processing.evaluation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.regex.Pattern;
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
      GlobalConfig globalConfig) {
    this.workingDirectory = Paths.get(workingDirectory);
    if (!this.workingDirectory.toFile().exists()) {
      throw new IllegalArgumentException("Evaluation worker directory does not exist");
    }
    this.targetDirectories = targetDirectories.stream().toList();
    this.targetDirectories.stream().filter(path -> !path.toFile().exists()).forEach(path -> {
      throw new IllegalArgumentException(
          "Evaluation target directory '%s' does not exist".formatted(path));
    });
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
