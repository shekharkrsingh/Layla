package org.layla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@EnableScheduling  // Add this annotation
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(new ModerationBot(System.getenv("BOT_TOKEN")));
			System.out.println("Bot started successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
