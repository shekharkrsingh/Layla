// BotFactory.java
package org.layla;

import java.util.Optional;

public class BotFactory {
    public static Optional<ModerationBot> createBot() {
        try {
            String botToken = System.getenv("BOT_TOKEN");
            String botUsername = System.getenv("BOT_USERNAME");
            Long ownerId = parseOwnerId();

            if (botToken == null || botUsername == null || ownerId == null) {
                throw new IllegalStateException("Missing required environment variables");
            }

            return Optional.of(new ModerationBot(botToken, botUsername, ownerId));
        } catch (Exception e) {
            System.err.println("Failed to create bot: " + e.getMessage());
            return Optional.empty();
        }
    }

    private static Long parseOwnerId() {
        try {
            return Long.parseLong(System.getenv("BOT_OWNER_ID").trim());
        } catch (Exception e) {
            System.err.println("Invalid BOT_OWNER_ID format");
            return null;
        }
    }
}