package rujche.sample.jmc.aa.java.application.ab.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MemorySample {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemorySample.class);

    public static void main(String[] args) {
        LOGGER.info("main started.");
        List<TestClass> list = new ArrayList<>();
        try {
            for (int i = 0; i < 1_000_000; i++) {
//            while (true) {
                list.add(new TestClass());
            }
        } catch (OutOfMemoryError e) {
            LOGGER.info("OutOfMemoryError happened as expected. list.size() = {}.", list.size(), e);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.error("Sleep failed", e);
        }
        LOGGER.info("main ended.");
    }
}
