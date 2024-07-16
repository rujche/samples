package rujche.sample.jmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadDumpsSample {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadDumpsSample.class);

    public static void main(String[] args) {
        LOGGER.info("main started.");
        new Thread(ThreadDumpsSample::synchronizedSleep, "main-synchronizedSleep01").start();
        new Thread(ThreadDumpsSample::synchronizedSleep, "main-synchronizedSleep02").start();
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
