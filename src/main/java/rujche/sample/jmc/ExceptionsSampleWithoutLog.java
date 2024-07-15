package rujche.sample.jmc;

import java.util.Set;

public class ExceptionsSampleWithoutLog {
    public static void main(String[] args) {
        sleepOneSecond();
        throwAndCatch(new Throwable("Test throwable"));
        sleepOneSecond();
        throwAndCatch(new Exception("Test exception"));
        sleepOneSecond();
        throwAndCatch(new RuntimeException("Test runtime exception"));
        sleepOneSecond();
        throwAndCatch(new Error("Test error"));
        sleepOneSecond();
    }

    private static void sleepOneSecond() {
        sleepWithoutException(1000);
    }

    private static void sleepWithoutException(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println("sleep failed.");
        }
    }



    private static void throwAndCatch(Throwable throwable) {
        try {
            throw throwable;
        } catch (Throwable t) {
            System.out.println("Caught throwable " + throwable.toString());
        }
    }

    public static class ThreadTest {
        public static void main(String[] args) {
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            for (Thread x : threadSet) {
                System.out.println(x.getName());
            }
        }
    }

    public static class ExceptionStatisticsAnalysis {
        public static void main(String[] args) {
            sleepWithoutException(2_000);
            throwAndCatch(new Throwable("First throwable -------- "));
            sleepWithoutException(2_000);
            throwAndCatch(new Throwable("Test throwable"));
            sleepWithoutException(2_000);
            throwAndCatch(new Exception("Test exception"));
            throwAndCatch(new Exception("Test exception"));
            sleepWithoutException(2_000);
            throwAndCatch(new RuntimeException("Test runtime exception"));
            throwAndCatch(new RuntimeException("Test runtime exception"));
            throwAndCatch(new RuntimeException("Test runtime exception"));
            sleepWithoutException(2_000);
            throwAndCatch(new Error("Test error"));
            throwAndCatch(new Error("Test error"));
            throwAndCatch(new Error("Test error"));
            throwAndCatch(new Error("Test error"));
            sleepWithoutException(2_000);
            throwAndCatch(new Throwable("Last throwable -------- "));
            sleepWithoutException(2_000);
        }
    }

    public static class ExceptionStatisticsAnalysisTwo {
        public static void main(String[] args) {
            sleepWithoutException(2_000);
            throwAndCatch(new Throwable("First throwable -------- "));
            sleepWithoutException(2_000);
            throwAndCatch(new Throwable("Test throwable"));
            throwAndCatch(new Throwable("Test throwable"));
            throwAndCatch(new Throwable("Test throwable"));
            throwAndCatch(new Throwable("Test throwable"));
            sleepWithoutException(2_000);
            throwAndCatch(new Exception("Test exception"));
            throwAndCatch(new Exception("Test exception"));
            throwAndCatch(new Exception("Test exception"));
            sleepWithoutException(2_000);
            throwAndCatch(new RuntimeException("Test runtime exception"));
            throwAndCatch(new RuntimeException("Test runtime exception"));
            sleepWithoutException(2_000);
            throwAndCatch(new Error("Test error"));
            sleepWithoutException(2_000);
            throwAndCatch(new Throwable("Last throwable -------- "));
            sleepWithoutException(2_000);
        }
    }
}
