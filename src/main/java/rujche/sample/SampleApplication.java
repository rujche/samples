package rujche.sample;

import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.messaging.eventhubs.support.EventHubsHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.azure.spring.messaging.AzureHeaders.CHECKPOINTER;

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
	@Bean
	public Consumer<Message<String>> consume() {
		return message -> {
			Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);
			LOGGER.info("New message received: '{}', partition key: {}, sequence number: {}, offset: {}, enqueued time: {}",
					message.getPayload(),
					message.getHeaders().get(EventHubsHeaders.PARTITION_KEY),
					message.getHeaders().get(EventHubsHeaders.SEQUENCE_NUMBER),
					message.getHeaders().get(EventHubsHeaders.OFFSET),
					message.getHeaders().get(EventHubsHeaders.ENQUEUED_TIME)
			);

			checkpointer.success()
					.doOnSuccess(success -> LOGGER.info("Message '{}' successfully checkpointed", message.getPayload()))
					.doOnError(error -> LOGGER.error("Exception found", error))
					.block();
		};
	}

}
