package rujche.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import rujche.sample.avsc.generated.SampleMessage;

@RestController
public class SampleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleController.class);
    private final KafkaTemplate<Object, Object> template;
    private final EventHubsProperties properties;

    public SampleController (KafkaTemplate<Object, Object> template, EventHubsProperties properties) {
        this.template = template;
        this.properties = properties;
    }

    @PostMapping(path = "/message/{message}")
    public void sendMessage(@PathVariable String message) {
        LOGGER.info("Sending message: {}", message);
        this.template.send(properties.getEventhub(), new SampleMessage(message, "Message from SampleController."));
    }
}
