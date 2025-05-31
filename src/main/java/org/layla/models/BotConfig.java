
package org.layla.models;

public class BotConfig {
    private final String botUsername;
    private final String botToken;
    private final Long ownerId1;
    private final Long ownerId2;

    public BotConfig(String botUsername, String botToken, Long ownerId1, Long ownerId2) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.ownerId1 = ownerId1;
        this.ownerId2=ownerId2;
    }

    // Getters
    public String getBotUsername() { return botUsername; }
    public String getBotToken() { return botToken; }
    public Long getOwnerId1() { return ownerId1; }
    public Long getOwnerId2(){ return ownerId2;}
}