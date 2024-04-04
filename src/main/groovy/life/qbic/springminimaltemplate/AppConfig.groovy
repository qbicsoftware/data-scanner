package life.qbic.springminimaltemplate

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

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

    @Value('${messages.file}')
    public String messagesFile

    @Bean
    MessageService messageService() {
        return new CodingPrayersMessageService(messagesFile)
    }

    @Bean
    NewsMedia newsMedia() {
        return new DeveloperNews(messageService())
    }

}
