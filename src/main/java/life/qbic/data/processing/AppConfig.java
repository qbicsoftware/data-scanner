package life.qbic.data.processing;

import life.qbic.data.processing.config.RegistrationWorkersConfig;
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


}
