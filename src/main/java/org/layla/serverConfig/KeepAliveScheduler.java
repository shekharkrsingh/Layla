package org.layla.serverConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class KeepAliveScheduler {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveScheduler.class);

    // Load the URL from an environment variable
    private static final String SERVER_URL = System.getenv("SERVER_URL");

    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void pingServer() {
        if (SERVER_URL == null || SERVER_URL.isEmpty()) {
            logger.warn("KEEP_ALIVE_URL environment variable not set. Skipping ping.");
            return;
        }

        try {
            URL url = new URL(SERVER_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            logger.info("Pinged server: Response code = {}", responseCode);
        } catch (Exception e) {
            logger.error("Failed to ping server: {}", e.getMessage());
        }
    }
}
