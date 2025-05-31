// BotFactory.java
package org.layla;

import java.util.Optional;

public class BotFactory {
    public static Optional<ModerationBot> createBot() {
        try {
            String botToken = System.getenv("BOT_TOKEN");
            String botUsername = System.getenv("BOT_USERNAME");
            Long ownerId1 = parseOwnerId1();
            Long ownerId2 = parseOwnerId2();

            if (botToken == null || botUsername == null || ownerId1 == null) {
                throw new IllegalStateException("Missing required environment variables");
            }

            return Optional.of(new ModerationBot(botToken, botUsername, ownerId1,ownerId2));
        } catch (Exception e) {
            System.err.println("Failed to create bot: " + e.getMessage());
            return Optional.empty();
        }
    }

    private static Long parseOwnerId1() {
        try {
            return Long.parseLong(System.getenv("BOT_OWNER_ID1").trim());
        } catch (Exception e) {
            System.err.println("Invalid BOT_OWNER_ID format");
            return null;
        }
    }private static Long parseOwnerId2() {
        try {
            return Long.parseLong(System.getenv("BOT_OWNER_ID2").trim());
        } catch (Exception e) {
            System.err.println("Invalid BOT_OWNER_ID format");
            return null;
        }
    }
}