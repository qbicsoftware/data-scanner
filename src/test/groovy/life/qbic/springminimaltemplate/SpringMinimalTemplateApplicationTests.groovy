package life.qbic.springminimaltemplate

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class SpringMinimalTemplateApplicationTests extends Specification {

    @Test
    void contextLoads() {
    }

    @Autowired
    private MessageService messageService

    def "autowired works"() {
        when:
        String messages = messageService.collectMessage()
        println(messages)
        then:
        messages != null
    }

}
