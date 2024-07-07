package rujche.sample.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.kafka.inbound.KafkaMessageSource;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.integration.router.RecipientListRouter;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerProperties;
import org.springframework.messaging.MessageHandler;
import rujche.sample.EventHubsProperties;

import static rujche.sample.integration.ChannelNames.ENTRY_CHANNEL;
import static rujche.sample.integration.ChannelNames.KAFKA_CONSUMER_CHANNEL;
import static rujche.sample.integration.ChannelNames.KAFKA_PRODUCER_CHANNEL;
import static rujche.sample.integration.ChannelNames.LOG_CHANNEL_1;
import static rujche.sample.integration.ChannelNames.LOG_CHANNEL_2;

@Configuration
public class IntegrationConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationConfiguration.class);
    private final KafkaTemplate<Object, Object> template;
    private final EventHubsProperties properties;

    public IntegrationConfiguration (KafkaTemplate<Object, Object> template, EventHubsProperties properties) {
        this.template = template;
        this.properties = properties;
    }

    @Bean
    @ServiceActivator(inputChannel = ENTRY_CHANNEL)
    public RecipientListRouter entryChannelRouter() {
        RecipientListRouter router = new RecipientListRouter();
        router.addRecipient(KAFKA_PRODUCER_CHANNEL);
        router.addRecipient(LOG_CHANNEL_1);
        return router;
    }

    @Bean
    @ServiceActivator(inputChannel = KAFKA_PRODUCER_CHANNEL)
    public MessageHandler kafkaProducerMessageHandler() throws Exception {
        KafkaProducerMessageHandler<Object, Object> handler = new KafkaProducerMessageHandler<>(template);
        handler.setTopicExpression(new LiteralExpression(properties.getEventhub()));
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = LOG_CHANNEL_1)
    public MessageHandler logMessageHandler1() {
        return message -> LOGGER.info("logMessageHandler1: Handle message: {}.", message);
    }

    @Bean
    @InboundChannelAdapter(channel = KAFKA_CONSUMER_CHANNEL, poller = @Poller(fixedDelay = "5000"))
    public KafkaMessageSource<String, String> source(ConsumerFactory<String, String> consumerFactory)  {
        ConsumerProperties consumerProperties = new ConsumerProperties(properties.getEventhub());
        return new KafkaMessageSource<>(consumerFactory, consumerProperties);
    }

    @Bean
    @ServiceActivator(inputChannel = KAFKA_CONSUMER_CHANNEL)
    public RecipientListRouter kafkaConsumerChannelRouter() {
        RecipientListRouter router = new RecipientListRouter();
        router.addRecipient(LOG_CHANNEL_2);
        return router;
    }

    @Bean
    @ServiceActivator(inputChannel = LOG_CHANNEL_2)
    public MessageHandler logMessageHandler2() {
        return message -> LOGGER.info("logMessageHandler2: Handle message: {}.", message);
    }

}
