package life.qbic.data.processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
        AppConfig.class);

    ScannerConfiguration scannerConfiguration = context.getBean(ScannerConfiguration.class);
    var scannerThread = new Scanner(scannerConfiguration, new ConcurrentEventQueue());
    scannerThread.start();

    Runtime.getRuntime().addShutdownHook(new Thread(null, scannerThread::interrupt, "Shutdown-thread"));

    context.close();
  }



}
