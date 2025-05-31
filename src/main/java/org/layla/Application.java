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
			String ownerIdStr1 = System.getenv("BOT_OWNER_ID1");
			String ownerIdStr2 = System.getenv("BOT_OWNER_ID2");

			// Validate configuration
			if (botToken == null || botToken.isEmpty()) {
				throw new IllegalArgumentException("BOT_TOKEN environment variable not set");
			}
			if (botUsername == null || botUsername.isEmpty()) {
				throw new IllegalArgumentException("BOT_USERNAME environment variable not set");
			}
			if (ownerIdStr1 == null || ownerIdStr1.isEmpty()) {
				throw new IllegalArgumentException("BOT_OWNER_ID1 environment variable not set");
			}


			if (ownerIdStr2 == null || ownerIdStr2.isEmpty()) {
				throw new IllegalArgumentException("BOT_OWNER_ID2 environment variable not set");
			}

			// Parse owner ID
			Long ownerId1;
			Long ownerId2;
			try {
				ownerId1 = Long.parseLong(ownerIdStr1.trim());
				ownerId2 = Long.parseLong(ownerIdStr2.trim());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid BOT_OWNER_ID format - must be numeric");
			}

			// Create and register bot
			ModerationBot bot = new ModerationBot(botToken, botUsername, ownerId1, ownerId2);
			botsApi.registerBot(bot);

			System.out.println("Bot started successfully with username: " + botUsername);
		} catch (Exception e) {
			System.err.println("Failed to start bot: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}