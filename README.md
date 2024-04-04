# Spring Boot Starter (template)

A minimal working starter template for a Spring Boot non-web applications using the
Spring [CommandLineRunner](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/CommandLineRunner.html)
interface with a demonstration of Java
annotation-based [Inversion of Control](https://stackoverflow.com/questions/3058/what-is-inversion-of-control)
via [Dependency Injection](https://stackoverflow.com/questions/130794/what-is-dependency-injection).

## Run the app

Checkout the latest code from `main` and run the Maven goal `spring-boot:run`:

```
mvn spring-boot:run
```

## What the app does

This small app just parses a file with a collection of good coding prayers and creates a singleton
instance of an `CodingPrayersMessageService`. This concrete implementation uses the
interface `MessageService`, that comes with only one public method: `String collectMessage()`.

This service is used to demonstrate the IoC principle. We have defined another interface `NewsMedia`
and provide a concrete implementation `DeveloperNews`, that will call the message service to receive
recent news and forward them to the caller.

In the main app code, we just retrieve this Singleton instance or Bean in Spring lingua from the
loaded context and call the news media `getNEws()` method. The collected message is then printed out
to the command line interface:

```

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.6)

2021-11-18 09:17:37.640  INFO 68052 --- [           main] l.q.s.SpringMinimalTemplateApplication   : Starting SpringMinimalTemplateApplication using Java 17.0.1 on imperator.am10.uni-tuebingen.de with PID 68052 (/Users/sven1103/git/spring-boot-starter-template/target/classes started by sven1103 in /Users/sven1103/git/spring-boot-starter-template)
2021-11-18 09:17:37.641  INFO 68052 --- [           main] l.q.s.SpringMinimalTemplateApplication   : No active profile set, falling back to default profiles: default
2021-11-18 09:17:38.164  INFO 68052 --- [           main] l.q.s.SpringMinimalTemplateApplication   : Started SpringMinimalTemplateApplication in 0.808 seconds (JVM running for 1.489)
####################### Message of the day ##################
Have you written unit tests yet? If not, do it!
##############################################################

```

## Realisation of IoC and DPI

The messages collection is stored in a simple text file `messages.txt`, that is provided with the
apps `resources`. Just go ahead and change the content of the file and run the app!

<img width="308" alt="grafik" src="https://user-images.githubusercontent.com/9976560/142380084-d01081d2-79fb-4ff3-acc5-3140dca38f6a.png">


So how does the app know where to find this file and **load the messages content**?

We have configured it as a **external property** in a file `application.properties` and load the
configuration on application startup! Cool eh?

<img width="381" alt="grafik" src="https://user-images.githubusercontent.com/9976560/142376871-5bee068f-208c-4af0-a35b-9442ee498789.png">

This is how the file content looks like:

```
messages.file=messages.txt
```

So how do we access the value of the `messages.file` property in our application with Spring?

Have a look in the class `AppConfig`, there the magic happens:

```groovy
@Configuration
@PropertySource("application.properties")
class AppConfig {

    @Value('${messages.file}')
    public String messagesFile
```

We define the property source, which is the file `application.properties` that is provided in the
resource folder of the app and available to the classpath. We also tell with the
annotation `@Configuration` Spring, hey this is a class that holds app configuration data!

With the annotation `@Value('${messages.file}')` we tell Spring, which property's value should be
injected. Here we make use of field injection, other types of injection like method and constructor
injection are also possible.

So how is the concrete implementation of the `MessageService` presented to Spring? We can use
the `@Bean` annotation here, to tell Spring: _hey, this is sth you must load on startup and provide
to the context_.

```java
@Configuration
@PropertySource("application.properties")
class AppConfig {

    ....

    @Bean
    MessageService messageService() {
        return new CodingPrayersMessageService(messagesFile)
    }
```

That is all there is, you can now load the bean in your main application code:

```java
@SpringBootApplication
class SpringMinimalTemplateApplication {

    static void main(String[] args) {
	SpringApplication.run(SpringMinimalTemplateApplication, args)
	// load the annotation context
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)
	// get the service bean
	MessageService service = context.getBean("messageService", MessageService.class)
	// collect the message and praise the magic
	println service.collectMessage()

```

### Inversion of Control

<img width="990" alt="grafik" src="https://user-images.githubusercontent.com/9976560/144576610-4dd4aa0a-1b58-4832-aaac-0bb70757a9a1.png">

You might have already spotted the interface `NewsMedia` and its implementing class `DeveloperNews`
in the app's source code. Here you can see an example for the magic of inversion of control.

The `NewsMedia` interface is just an abstraction that we will later use, because we don't care about
the actual implementation details. By this, we also do not create any dependencies to concrete
implementation details but on actual behaviour. Concrete implementations can then later be exchanged
without causing any breaking changes in the client code base.

The interface has only one method: `String getNews()`. Now let's have a closer look into the
class `DeveloperNews` that implements this interface:

```java
class DeveloperNews implements NewsMedia{

    private MessageService service

    DeveloperNews(MessageService service) {
        this.service = service
    }

    @Override
    String getNews() {
        return service.collectMessage()
    }
}
```

When you check the constructor signature, you see that this method has only one argument, which is a
reference to an object of type `MessageService`. And when the `getNews()` method is called by the
client, the class delegates this request to the message service. Since we have stored the reference
in a private field, that is super easy, we known how to call the service.

So why is this inversion of control?

Because the `DeveloperNews` class does not manage the instantiation of a concrete message service.
The configuration happened outside of the class, therefore the DeveloperNews class has no direct
control over the instantiation. If it had, it would look like this:

```java
DeveloperNews(String filePathToMessages) {
        this.service = new CodingPrayersMessageService(filePathToMessages)
}
```

That doesn't look good, does it? In order to create an instance of a message service, we would need
to know the conrete implementation and its required properties (here it is the file path to
the `messages.txt`). So the `DeveloperNews` class has the control over the message service.

Instead, we would like to not take care about these details, so we invert the control and inject the
dependency via the constructor.

Please find more in depth documentation on the
official [Spring website](https://spring.io/projects/spring-framework).





