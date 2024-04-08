package life.qbic.data.processing;

import java.nio.file.Path;
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
      @Value("${scanner.interval}") int interval) {
    return new ScannerConfiguration(scannerDirectory, interval);
  }

  @Bean
  RegistrationWorkersConfig registrationWorkersConfig(
      @Value("${registration.threads}") int amountOfWorkers,
      @Value("${registration.working.dir}") String workingDirectory,
      @Value("${registration.target.dir}") String targetDirectory) {
    return new RegistrationWorkersConfig(amountOfWorkers, workingDirectory, targetDirectory);
  }

  @Bean
  RegistrationConfiguration registrationConfiguration(
      RegistrationWorkersConfig registrationWorkersConfig) {
    return new RegistrationConfiguration(registrationWorkersConfig.workingDirectory().toString(),
        registrationWorkersConfig.targetDirectory().toString());
  }

  @Bean
  EvaluationWorkersConfig evaluationWorkersConfig(
      @Value("${evaluations.threads}") int amountOfWorkers,
      @Value("${evaluation.working.dir}") String workingDirectory,
      @Value("${evaluation.target.dir}") String targetDirectory,
      @Value("${evaluation.measurement-id.pattern}") String measurementIdPattern) {
    return new EvaluationWorkersConfig(amountOfWorkers, workingDirectory, targetDirectory,
        measurementIdPattern);
  }

  @Bean
  EvaluationConfiguration evaluationConfiguration(EvaluationWorkersConfig evaluationWorkersConfig) {
    return new EvaluationConfiguration(evaluationWorkersConfig.workingDirectory().toString(),
        evaluationWorkersConfig.targetDirectory().toString(),
        evaluationWorkersConfig.measurementIdPattern().toString());
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
    return new ProcessingConfiguration(processingWorkersConfig.getWorkingDirectory(),
        processingWorkersConfig.getTargetDirectory());
  }

}
