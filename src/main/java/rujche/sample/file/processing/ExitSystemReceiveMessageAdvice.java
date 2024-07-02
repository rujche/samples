package rujche.sample.file.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.integration.aop.ReceiveMessageAdvice;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;


@Component
public class ExitSystemReceiveMessageAdvice implements ReceiveMessageAdvice, ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExitSystemReceiveMessageAdvice.class);

    private ApplicationContext context;
    private boolean lastResultIsNull = true;

    @Override
    public Message<?> afterReceive(Message<?> result, Object source) {
        boolean resultIsNull = (result == null);
        if (resultIsNull != lastResultIsNull) {
            LOGGER.info("Message receiving status changed, received message is {}.", resultIsNull ? "null" : "not null");
            lastResultIsNull = resultIsNull;
            if (resultIsNull) {
                LOGGER.info("Closing context and exiting application.");
                ((ConfigurableApplicationContext) context).close();
            }
        }
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
