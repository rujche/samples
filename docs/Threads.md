# Threads

Just following the steps below one by one to learn about the basic usage of related feature in JMC.

1. Read the java code and think about the expected output.
    ```java
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
    ```
2. Diagram about thread and lock:
    ```mermaid
    graph LR
        main-synchronizedSleep01 -->|requires| lock
        main-synchronizedSleep02 -->|requires| lock
        lock --> sleep
        sleep --> quit
    ```
3. Run the code with JVM parameter `-XX:StartFlightRecording=duration=30s,filename=sample.jfr`. More information about 
   `-XX:StartFlightRecording` can be found in [java command doc](https://docs.oracle.com/en/java/javase/22/docs/specs/man/java.html).
4. Check the output log. Does it meet with your expectation? Here is the log in my side:
   ```text
   [0.813s][info][jfr,startup] Started recording 1. The result will be written to:
   [0.813s][info][jfr,startup] 
   [0.813s][info][jfr,startup] C:\Users\rujche\Work\git-workspace\main\samples\sample.jfr
   09:52:17.222 [main] INFO rujche.sample.jmc.ThreadsSample -- main started.
   09:52:17.228 [main-synchronizedSleep01] INFO rujche.sample.jmc.ThreadsSample -- synchronizedSleep started.
   09:52:17.228 [main] INFO rujche.sample.jmc.ThreadsSample -- main ended.
   09:52:20.229 [main-synchronizedSleep01] INFO rujche.sample.jmc.ThreadsSample -- synchronizedSleep ended.
   09:52:20.229 [main-synchronizedSleep02] INFO rujche.sample.jmc.ThreadsSample -- synchronizedSleep started.
   09:52:23.230 [main-synchronizedSleep02] INFO rujche.sample.jmc.ThreadsSample -- synchronizedSleep ended.
   ```
5. JFR file is binary file, it's not readable directly. But we can print jfr file contents to text file by jfr tool in 
   JDK. The jfr tool is part of the JDK installation. 
   ```shell
   $ jfr print sample.jfr > sample.jfr.txt
   ```
6. Open sample.jfr.text, we can find that the content is just a list of events.
   ```text
   jdk.ActiveSetting {
     startTime = 09:52:17.014
     id = 1538
     name = "threshold"
     value = "0 ns"
     eventThread = "main" (javaThreadId = 1)
   }
   
   jdk.ActiveSetting {
     startTime = 09:52:17.014
     duration = 0.539 ms
     id = 1538
     name = "stackTrace"
     value = "false"
     eventThread = "main" (javaThreadId = 1)
   }
   ...
   ```
7. The events and their field layout depends on the JDK version. We can use the `jfr` tool to look at the metadata:
   ```shell
   jfr metadata > jfr-metadata.text
   ```
8. Open `jfr-metadata.text`, and find the event we have seen before: `jdk.ActiveSetting`.
   ```java
   @Name("jdk.ActiveSetting")
   @Label("Recording Setting")
   @Category("Flight Recorder")
   class ActiveSetting extends jdk.jfr.Event {
     @Label("Start Time")
     @Timestamp("TICKS")
     long startTime;
   
     @Label("Duration")
     @Timespan("TICKS")
     long duration;
   
     @Label("Event Thread")
     @Description("Thread in which event was committed in")
     Thread eventThread;
   
     @Label("Stack Trace")
     @Description("Stack Trace starting from the method the event was committed in")
     StackTrace stackTrace;
   
     @Label("Event Id")
     long id;
   
     @Label("Setting Name")
     String name;
   
     @Label("Setting Value")
     String value;
   }
   ```
9. Open `sample.jfr` by JMC.
   > ![Threads-open-jfr-file-by-jmc](../pictures/Threads-open-jfr-file-by-jmc.png)
10. The `Automated Analysis Result` was displayed by default. The given result is accurate: `Lock Instance` / 
    `Java Blocking`.
11. Click `Threads` and analyze by the board.
   > ![Threads-Threads-board](../pictures/Threads-Threads-board.png)
12. Choose one thread and analyze it.
   > ![Threads-thread-analysis](../pictures/Threads-thread-analysis.png)
13. Find related event in sample.jfr. 
    1. For example, to find this event: 
       > ![Threads-find-event-in-jfr-file-1-JMC-board](../pictures/Threads-find-event-in-jfr-file-1-JMC-board.png)
    2. Search `Java Monitor Blocked` in `jfr-metadata.text`, get `JavaMonitorEnter`:
       > ![Threads-find-event-in-jfr-file-2-metadata](../pictures/Threads-find-event-in-jfr-file-2-metadata.png)
    3. Search `JavaMonitorEnter` in `sample.jfr.text`:
       > ![Threads-find-event-in-jfr-file-3-jfr-file](../pictures/Threads-find-event-in-jfr-file-3-jfr-file.png)
14. Find related event in JMC page.
    > ![Threads-find-event-in-JMC](../pictures/Threads-find-event-in-JMC.png)
15. I found that the outline support DIY:
    > ![Threads-Outline-support-DIY](../pictures/Threads-Outline-support-DIY.png)

