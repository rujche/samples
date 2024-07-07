package rujche.sample;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("eventhubs")
public class EventHubsProperties {

    private String namespace;
    private String eventhub;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getEventhub() {
        return eventhub;
    }

    public void setEventhub(String eventhub) {
        this.eventhub = eventhub;
    }
}