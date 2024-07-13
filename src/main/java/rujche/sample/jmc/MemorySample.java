package rujche.sample.jmc;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rujche.sample.jmc.aa.java.application.ab.memory.TestClassTwo;

import java.util.ArrayList;
import java.util.List;

public class MemorySample {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemorySample.class);
    public static void main(String[] args) {
        LOGGER.info("main started.");
        List<Object> list = new ArrayList<>();
        try {
            while (true) {
                list.add(new Object());
            }
        } catch (OutOfMemoryError e) {
            LOGGER.info("OutOfMemoryError happened as expected. list.size() = {}.", list.size(), e);
        }
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            LOGGER.error("Sleep failed", e);
        }
        LOGGER.info("main ended.");
    }

    private static class MemoryAnalysis {
        public static void main(String[] args) {
            LOGGER.info("main started.");
            List<MemoryAnalysis> list = new ArrayList<>();
            try {
                for (int i = 0; i < 10_000_000; i++) {
                    list.add(new MemoryAnalysis());
                }
            } catch (OutOfMemoryError e) {
                LOGGER.info("OutOfMemoryError happened. list.size() = {}.", list.size(), e);
            }
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                LOGGER.error("Sleep failed", e);
            }
            LOGGER.info("main ended.");
        }
    }

    private static class MemoryAnalysisByHeapDump {
        public static void main(String[] args) {
            LOGGER.info("main started.");
            List<MemoryAnalysisByHeapDump> list = new ArrayList<>();
            try {
                for (int i = 0; i < 100_000; i++) {
                    list.add(new MemoryAnalysisByHeapDump());
                }
            } catch (OutOfMemoryError e) {
                LOGGER.info("OutOfMemoryError happened. list.size() = {}.", list.size(), e);
            }
            LOGGER.info("Start sleep. Please create heap dump now.");
            try {
                Thread.sleep(100_000);
            } catch (InterruptedException e) {
                LOGGER.error("Sleep failed", e);
            }
        }
    }

    private static class MemoryAnalysisTwo {
        public static void main(String[] args) {
            System.out.println("======================================================================");
            System.out.println("--------------------------------------");
            System.out.println(ClassLayout.parseClass(MemoryAnalysisTwo.class).toPrintable());
            System.out.println("--------------------------------------");
            MemoryAnalysisTwo memoryAnalysisTwo = new MemoryAnalysisTwo();
            System.out.println("VM.current().sizeOf(memoryAnalysisTwo):\n" + VM.current().sizeOf(memoryAnalysisTwo));
            System.out.println("--------------------------------------");
            System.out.println("GraphLayout.parseInstance(memoryAnalysisTwo).toFootprint():\n" + GraphLayout.parseInstance(memoryAnalysisTwo).toFootprint());
        }
    }

    private static class MemoryAnalysisThree {
        private Object objectValue;
        private boolean booleanValue;
        private byte byteValue;
        private short shortValue;
        private char charValue;
        private int intValue;
        private float floatValue;
        private long longValue;
        private double doubleValue;
        private Object objectValue2;

        public static void main(String[] args) {
            System.out.println("======================================================================");
            System.out.println(ClassLayout.parseClass(MemoryAnalysis.class).toPrintable());
            System.out.println("--------------------------------------");
            MemoryAnalysisTwo memoryAnalysisTwo = new MemoryAnalysisTwo();
            System.out.println("VM.current().sizeOf(memoryAnalysisTwo):\n" + VM.current().sizeOf(memoryAnalysisTwo));
            System.out.println("--------------------------------------");
            System.out.println("GraphLayout.parseInstance(memoryAnalysisTwo).toFootprint():\n" + GraphLayout.parseInstance(memoryAnalysisTwo).toFootprint());

            System.out.println("======================================================================");
            System.out.println("--------------------------------------");
            System.out.println(ClassLayout.parseClass(MemoryAnalysisThree.class).toPrintable());
            System.out.println("--------------------------------------");
            MemoryAnalysisThree memoryAnalysisThree = new MemoryAnalysisThree();
            System.out.println("VM.current().sizeOf(memoryAnalysisThree):\n" + VM.current().sizeOf(memoryAnalysisThree));
            System.out.println("--------------------------------------");
            System.out.println("GraphLayout.parseInstance(memoryAnalysisThree).toFootprint():\n" + GraphLayout.parseInstance(memoryAnalysisThree).toFootprint());

            System.out.println("======================================================================");
            System.out.println("VM.current().details():\n" + VM.current().details());
        }
    }
}
