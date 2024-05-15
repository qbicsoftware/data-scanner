package life.qbic.data.processing;

import java.nio.file.Path;
import java.util.Arrays;
import life.qbic.data.processing.config.EvaluationWorkersConfig;
import life.qbic.data.processing.config.ProcessingWorkersConfig;
import life.qbic.data.processing.config.RegistrationWorkersConfig;
import life.qbic.data.processing.evaluation.EvaluationConfiguration;
import life.qbic.data.processing.processing.ProcessingConfiguration;
import life.qbic.data.processing.registration.RegistrationConfiguration;
import life.qbic.data.processing.scanner.ScannerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * <b>Spring configuration class</b>
 *
 * <p>Reads properties from a properties file and creates beans for the application.</p>
 *
 * @since 0.1.0
 */
@Configuration
@PropertySource("application.properties")
class AppConfig {

  @Bean
  ScannerConfiguration scannerConfiguration(
      @Value("${scanner.directory}") String scannerDirectory,
      @Value("${scanner.interval}") int interval, @Value("${scanner.ignore}") String[] ignore) {
    return new ScannerConfiguration(scannerDirectory, interval, ignore);
  }

  @Bean
  RegistrationWorkersConfig registrationWorkersConfig(
      @Value("${registration.threads}") int amountOfWorkers,
      @Value("${registration.working.dir}") String workingDirectory,
      @Value("${registration.target.dir}") String targetDirectory,
      @Value("${registration.metadata.filename}") String metadataFileName) {
    return new RegistrationWorkersConfig(amountOfWorkers, workingDirectory, targetDirectory,
        metadataFileName);
  }

  @Bean
  RegistrationConfiguration registrationConfiguration(
      RegistrationWorkersConfig registrationWorkersConfig) {
    return new RegistrationConfiguration(registrationWorkersConfig.workingDirectory().toString(),
        registrationWorkersConfig.targetDirectory().toString(),
        registrationWorkersConfig.metadataFileName());
  }

  @Bean
  EvaluationWorkersConfig evaluationWorkersConfig(
      @Value("${evaluation.threads}") int amountOfWorkers,
      @Value("${evaluation.working.dir}") String workingDirectory,
      @Value("${evaluation.target.dirs}") String[] targetDirectory) {
    return new EvaluationWorkersConfig(amountOfWorkers, workingDirectory, Arrays.stream(targetDirectory).toList());
  }

  @Bean
  EvaluationConfiguration evaluationConfiguration(EvaluationWorkersConfig evaluationWorkersConfig,
      GlobalConfig globalConfig) {
    return new EvaluationConfiguration(evaluationWorkersConfig.workingDirectory().toString(),
        evaluationWorkersConfig.targetDirectories(), globalConfig);
  }

  @Bean
  ProcessingWorkersConfig processingWorkersConfig(
      @Value("${processing.threads}") int amountOfWorkers,
      @Value("${processing.working.dir}") String workingDirectory,
      @Value("${processing.target.dir}") String targetDirectory) {
    return new ProcessingWorkersConfig(amountOfWorkers, Path.of(workingDirectory),
        Path.of(targetDirectory));
  }

  @Bean
  ProcessingConfiguration processingConfiguration(ProcessingWorkersConfig processingWorkersConfig) {
    return new ProcessingConfiguration(processingWorkersConfig.workingDirectory(),
        processingWorkersConfig.targetDirectory());
  }

  @Bean
  GlobalConfig globalConfig(
      @Value("${users.error.directory.name}") String usersErrorDirectoryName,
      @Value("${users.registration.directory.name}") String usersRegistrationDirectoryName,
      @Value("${qbic.measurement-id.pattern}") String measurementIdPattern) {
    return new GlobalConfig(usersErrorDirectoryName, usersRegistrationDirectoryName, measurementIdPattern);
  }

}
