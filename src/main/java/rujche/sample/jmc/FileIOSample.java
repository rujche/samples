package rujche.sample.jmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileIOSample {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileIOSample.class);
    private static final String FILE_NAME = "src/main/java/rujche/sample/jmc/FileIOSample.java";

    public static void main(String[] args) throws IOException {
        LOGGER.info("main started");
        for (int i = 0; i < 1_000; i++) {
            byte[] data = Files.readAllBytes(Paths.get(FILE_NAME));
            File file = File.createTempFile("FileIOSample-", ".text");
            Files.write(file.toPath(), data);
            file.deleteOnExit();
        }
        LOGGER.info("main ended.");
    }
}
