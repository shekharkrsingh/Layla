package org.layla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@EnableScheduling
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

			// Get configuration from environment variables
			String botToken = System.getenv("BOT_TOKEN");
			String botUsername = System.getenv("BOT_USERNAME");
			String ownerIdStr = System.getenv("BOT_OWNER_ID");

			// Validate configuration
			if (botToken == null || botToken.isEmpty()) {
				throw new IllegalArgumentException("BOT_TOKEN environment variable not set");
			}
			if (botUsername == null || botUsername.isEmpty()) {
				throw new IllegalArgumentException("BOT_USERNAME environment variable not set");
			}
			if (ownerIdStr == null || ownerIdStr.isEmpty()) {
				throw new IllegalArgumentException("BOT_OWNER_ID environment variable not set");
			}

			// Parse owner ID
			Long ownerId;
			try {
				ownerId = Long.parseLong(ownerIdStr.trim());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid BOT_OWNER_ID format - must be numeric");
			}

			// Create and register bot
			ModerationBot bot = new ModerationBot(botToken, botUsername, ownerId);
			botsApi.registerBot(bot);

			System.out.println("Bot started successfully with username: " + botUsername);
		} catch (Exception e) {
			System.err.println("Failed to start bot: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}