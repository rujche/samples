package rujche.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.function.Supplier;

@SpringBootApplication
public class SampleApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(SampleApplication.class);
	private int i = 0;

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

	@Bean
	public Supplier<Message<String>> supply() {
		return () -> {
            LOGGER.info("Sending message, sequence {}", i);
			return MessageBuilder.withPayload("Hello world, " + i++).build();
		};
	}


}
