package rujche.sample.jmc.aa.java.application.aa.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadsSample {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadsSample.class);

    public static void main(String[] args) {
        LOGGER.info("main started.");
        new Thread(ThreadsSample::synchronizedSleep, "main-synchronizedSleep01").start();
        new Thread(ThreadsSample::synchronizedSleep, "main-synchronizedSleep02").start();
        LOGGER.info("main ended.");
    }

    private static synchronized void synchronizedSleep() {
        LOGGER.info("synchronizedSleep started.");
        try {
            Thread.sleep(3_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("synchronizedSleep ended.");
    }
}
