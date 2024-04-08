package life.qbic.data.processing;

import java.util.LinkedList;
import java.util.List;
import life.qbic.data.processing.config.ProcessingWorkersConfig;
import life.qbic.data.processing.config.RegistrationWorkersConfig;
import life.qbic.data.processing.processing.ProcessingConfiguration;
import life.qbic.data.processing.processing.ProcessingRequest;
import life.qbic.data.processing.registration.ProcessRegistrationRequest;
import life.qbic.data.processing.registration.RegistrationConfiguration;
import life.qbic.data.processing.scanner.Scanner;
import life.qbic.data.processing.scanner.ScannerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class Application {

  private static final Logger log = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
        AppConfig.class);

    ScannerConfiguration scannerConfiguration = context.getBean(ScannerConfiguration.class);
    RegistrationWorkersConfig registrationWorkersConfig = context.getBean(RegistrationWorkersConfig.class);
    RegistrationConfiguration registrationConfiguration = context.getBean(RegistrationConfiguration.class);
    ProcessingWorkersConfig processingWorkersConfig = context.getBean(ProcessingWorkersConfig.class);
    ProcessingConfiguration processingConfiguration = context.getBean(ProcessingConfiguration.class);

    var requestQueue = new ConcurrentRegistrationQueue();
    var scannerThread = new Scanner(scannerConfiguration, requestQueue);

    log.info("Registering %s registration workers...".formatted(registrationWorkersConfig.amountOfWorkers()));

    List<ProcessRegistrationRequest> registrationWorkers = new LinkedList<>();
    for (int i=0; i<registrationWorkersConfig.amountOfWorkers(); i++) {
      registrationWorkers.add(new ProcessRegistrationRequest(requestQueue, registrationConfiguration));
    }

    log.info("Registering %s processing workers...".formatted(processingWorkersConfig.getThreads()));

    List<ProcessingRequest> processingWorkers = new LinkedList<>();
    for (int i=0; i<processingWorkersConfig.getThreads(); i++) {
      processingWorkers.add(new ProcessingRequest(processingConfiguration));
    }


    scannerThread.start();
    registrationWorkers.forEach(Thread::start);
    processingWorkers.forEach(Thread::start);


    Runtime.getRuntime().addShutdownHook(new Thread(null, () ->
    {
      log.info("Shutting sequence initiated...");
      scannerThread.interrupt();
      registrationWorkers.forEach(Thread::interrupt);
      processingWorkers.forEach(Thread::interrupt);
    }, "Shutdown-thread"));

  }
}
