package life.qbic.data.processing;

import life.qbic.springminimaltemplate.CodingPrayersMessageService;
import life.qbic.springminimaltemplate.DeveloperNews;
import life.qbic.springminimaltemplate.MessageService;
import life.qbic.springminimaltemplate.NewsMedia;
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

  @Value("${messages.file}")
  public String messagesFile;

  @Bean
  MessageService messageService() {
    return new CodingPrayersMessageService(messagesFile);
  }

  @Bean
  ScannerConfiguration scannerConfiguration(@Value("${scanner.directory}") String scannerDirectory) {
    return new ScannerConfiguration(scannerDirectory);
  }

  @Bean
  NewsMedia newsMedia() {
    return new DeveloperNews(messageService());
  }

}
