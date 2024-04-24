package life.qbic.data.processing;

import java.util.LinkedList;
import java.util.List;
import life.qbic.data.processing.config.EvaluationWorkersConfig;
import life.qbic.data.processing.config.ProcessingWorkersConfig;
import life.qbic.data.processing.config.RegistrationWorkersConfig;
import life.qbic.data.processing.evaluation.EvaluationConfiguration;
import life.qbic.data.processing.evaluation.EvaluationRequest;
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
    EvaluationWorkersConfig evaluationWorkersConfig = context.getBean(EvaluationWorkersConfig.class);
    EvaluationConfiguration evaluationConfiguration = context.getBean(EvaluationConfiguration.class);
    GlobalConfig globalConfig = context.getBean(GlobalConfig.class);

    var requestQueue = new ConcurrentRegistrationQueue();
    var scannerThread = new Scanner(scannerConfiguration, requestQueue, globalConfig);

    log.info("Registering {} registration workers...", registrationWorkersConfig.amountOfWorkers());

    List<ProcessRegistrationRequest> registrationWorkers = new LinkedList<>();
    for (int i=0; i<registrationWorkersConfig.amountOfWorkers(); i++) {
      registrationWorkers.add(new ProcessRegistrationRequest(requestQueue, registrationConfiguration));
    }

    log.info("Registering {} processing workers...", processingWorkersConfig.threads());

    List<ProcessingRequest> processingWorkers = new LinkedList<>();
    for (int i=0; i<processingWorkersConfig.threads(); i++) {
      processingWorkers.add(new ProcessingRequest(processingConfiguration));
    }

    log.info("Registering {} evaluation workers...", evaluationWorkersConfig.threads());

    List<EvaluationRequest> evaluationWorkers = new LinkedList<>();
    for (int i=0; i<evaluationWorkersConfig.threads(); i++) {
      evaluationWorkers.add(new EvaluationRequest(evaluationConfiguration));
    }

    scannerThread.start();
    registrationWorkers.forEach(Thread::start);
    processingWorkers.forEach(Thread::start);
    evaluationWorkers.forEach(Thread::start);

    Runtime.getRuntime().addShutdownHook(new Thread(null, () ->
    {
      log.info("Shutting sequence initiated...");
      scannerThread.interrupt();
      registrationWorkers.forEach(Thread::interrupt);
      processingWorkers.forEach(Thread::interrupt);
      evaluationWorkers.forEach(Thread::interrupt);
      // if every worker thread has shut down successfully, the application can exit with status code 0
      Runtime.getRuntime().halt(0);
    }, "Shutdown-thread"));

  }
}
