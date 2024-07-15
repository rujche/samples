package rujche.sample.jmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionsSample {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileIOSample.class);
    public static void main(String[] args) {
        LOGGER.info("main started");
        try {
            throw new Throwable("Test throwable");
        } catch (Throwable throwable) {
            LOGGER.info("Caught throwable");
        }
        try {
            throw new Exception("Test exception");
        } catch (Exception exception) {
            LOGGER.info("Caught exception");
        }
        try {
            throw new RuntimeException("Test runtime exception");
        } catch (RuntimeException exception) {
            LOGGER.info("Caught runtime exception");
        }
        try {
            throw new Error("Test error");
        } catch (Error error) {
            LOGGER.info("Caught error");
        }
        LOGGER.info("main ended");
    }


    private static class ExceptionStatisticsSample {
        public static void main(String[] args) {
            LOGGER.info("main started");
            sleepOneSecond();
            throwAndCatch(new Throwable("Test throwable"));
            sleepOneSecond();
            throwAndCatch(new Exception("Test exception"));
            sleepOneSecond();
            throwAndCatch(new RuntimeException("Test runtime exception"));
            sleepOneSecond();
            throwAndCatch(new Error("Test error"));
            sleepOneSecond();
            LOGGER.info("main ended");
        }

        private static void sleepOneSecond() {
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                LOGGER.info("sleep failed.", e);
            }
        }

        private static void throwAndCatch(Throwable throwable) {
            try {
                throw throwable;
            } catch (Throwable t) {
                LOGGER.info("Caught throwable", t);
            }
        }
    }
}
