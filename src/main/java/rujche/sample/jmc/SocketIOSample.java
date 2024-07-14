package rujche.sample.jmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class SocketIOSample {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketIOSample.class);

    public static void main(String[] args) throws IOException {
        LOGGER.info("main started.");
        List<String> urlList = Arrays.asList(
                "https://www.bing.com/",
                "https://www.google.com/",
                "https://www.baidu.com/",
                "https://spring.io/"
        );
        for (String url : urlList) {
            String htmlPage = downloadFromUrl(url);
            LOGGER.info("Downloaded html page. url = {}, size = {} Bytes.", url, htmlPage.length());
        }
        LOGGER.info("main ended.");
    }

    private static String downloadFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        String result;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            result = builder.toString();
        }
        return result;
    }
}
