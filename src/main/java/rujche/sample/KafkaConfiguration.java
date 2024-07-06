package rujche.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.support.GenericMessage;

@Configuration
public class KafkaConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConfiguration.class);

    private final EventHubsProperties properties;

    public KafkaConfiguration(EventHubsProperties properties) {
        this.properties = properties;
    }

    // Relate issue: https://github.com/spring-projects/spring-kafka/issues/3346
    @KafkaListener(id = "sample-message-group", topics = "${eventhubs.eventhub}")
    public void listen(GenericMessage<Object> message) {
        LOGGER.info("Received message. topic = {}, message = {}", properties.getEventhub(), message);
    }
}
