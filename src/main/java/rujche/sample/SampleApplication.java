package rujche.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import rujche.sample.avsc.generated.SampleMessage;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SpringBootApplication
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    // I tried to change type parameter from "Object" to "SimpleMessage", but failed.
    // If you can achieve this, welcome to create a PR.
    @Bean
    public Consumer<Object> consumer(){
        return message -> System.out.println("Consumed message. message = " + message);
    }

    @Bean
    public Supplier<SampleMessage> supplier() {
        return () -> new SampleMessage("Sample message", new Date().toString());
    }

}
