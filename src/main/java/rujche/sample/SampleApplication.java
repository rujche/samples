package rujche.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import rujche.sample.avsc.generated.SampleMessage;
import rujche.sample.integration.EntryGateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

@SpringBootApplication
public class SampleApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleApplication.class);

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(SampleApplication.class).run(args);
        EntryGateway entry = context.getBean(EntryGateway.class);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please input a line to send message. input 'exit' to exit.");
        while (true) {
            String line = reader.readLine();
            if (line.equals("exit")) {
                System.out.println("Closing application context, please wait a few seconds...");
                context.close();
                break;
            }
            SampleMessage message = new SampleMessage(line, "Created in SampleApplication. date = " + new Date());
            LOGGER.info("Sending message by EntryGateway. message = {}.", line);
            entry.sendMessage(message);
        }
    }

}
