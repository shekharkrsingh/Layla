
package org.layla.models;

public class BotConfig {
    private final String botUsername;
    private final String botToken;
    private final Long ownerId;

    public BotConfig(String botUsername, String botToken, Long ownerId) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.ownerId = ownerId;
    }

    // Getters
    public String getBotUsername() { return botUsername; }
    public String getBotToken() { return botToken; }
    public Long getOwnerId() { return ownerId; }
}