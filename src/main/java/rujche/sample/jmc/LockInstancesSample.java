package rujche.sample.jmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LockInstancesSample {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockInstancesSample.class);

    public static void main(String[] args) {
        LOGGER.info("main started.");
        ResourceA resourceA = new ResourceA();
        ResourceB resourceB = new ResourceB();
        Runnable runnableA = () -> {
            LOGGER.info("runnableA started.");
            synchronized (resourceA) {
                LOGGER.info("runnableA locked resourceA.");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOGGER.error("runnableA interrupted.", e);
                }
                synchronized (resourceB) {
                    LOGGER.info("runnableA locked both resourceA and resourceB.");
                }
                LOGGER.info("runnableA unlocked resourceB.");
            }
            LOGGER.info("runnableA unlocked resourceA.");
            LOGGER.info("runnableA ended.");
        };
        Runnable runnableB = () -> {
            LOGGER.info("runnableB started.");
            synchronized (resourceB) {
                LOGGER.info("runnableB locked resourceB.");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOGGER.error("runnableB interrupted.", e);
                }
                synchronized (resourceA) {
                    LOGGER.info("runnableB locked both resourceA and resourceB.");
                }
                LOGGER.info("runnableB unlocked resourceA.");
            }
            LOGGER.info("runnableB unlocked resourceB.");
            LOGGER.info("runnableB ended.");
        };
        Thread threadA = new Thread(runnableA, "runnableA");
        Thread threadB = new Thread(runnableB, "runnableB");
        threadA.start();
        threadB.start();
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            LOGGER.error("main interrupted.", e);
        }
        LOGGER.info("Prepare to interrupt threadA and threadB.");
        threadA.interrupt();
        threadB.interrupt();
        LOGGER.info("main ended.");
    }

    private static class ResourceA {
    }

    private static class ResourceB {
    }

    private static class TestRecordBlockEventWhenUnblocked {
        public static void main(String[] args) {
            LOGGER.info("main started.");
            ResourceA resourceA = new ResourceA();
            Runnable runnableA = () -> {
                LOGGER.info("runnableA started.");
                synchronized (resourceA) {
                    LOGGER.info("runnableA locked resourceA.");
                    try {
                        Thread.sleep(13_000);
                    } catch (InterruptedException e) {
                        LOGGER.error("runnableA interrupted.", e);
                    }
                    LOGGER.info("runnableA unlocked resourceB.");
                }
                LOGGER.info("runnableA ended.");
            };
            Runnable runnableB = () -> {
                LOGGER.info("runnableB started.");
                synchronized (resourceA) {
                    LOGGER.info("runnableB locked resourceA.");
                    try {
                        Thread.sleep(13_000);
                    } catch (InterruptedException e) {
                        LOGGER.error("runnableB interrupted.", e);
                    }
                    LOGGER.info("runnableB unlocked resourceA.");
                }
                LOGGER.info("runnableB ended.");
            };
            Thread threadA = new Thread(runnableA, "runnableA");
            Thread threadB = new Thread(runnableB, "runnableB");
            threadA.start();
            threadB.start();
            LOGGER.info("main ended.");
        }
    }

private static class MultiThreadLockOnOneObject {
    public static void main(String[] args) {
        LOGGER.info("main started.");
        ResourceA resourceA = new ResourceA();
        ResourceB resourceB = new ResourceB();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(createRunnable(resourceA), "thread-a-" + i));
            threads.add(new Thread(createRunnable(resourceB), "thread-b-" + i));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        LOGGER.info("main ended.");
    }

    private static Runnable createRunnable(Object object) {
        return () -> {
            synchronized (object) {
                try {
                    Thread.sleep(2_000);
                } catch (InterruptedException e) {
                    LOGGER.error("runnable interrupted.", e);
                }
            }
        };
    }
}
}
