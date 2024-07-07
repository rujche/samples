package rujche.sample.integration;


import org.springframework.integration.annotation.MessagingGateway;
import rujche.sample.avsc.generated.SampleMessage;

import static rujche.sample.integration.ChannelNames.ENTRY_CHANNEL;

@MessagingGateway(defaultRequestChannel = ENTRY_CHANNEL)
public interface EntryGateway {
    void sendMessage(SampleMessage message);
}
