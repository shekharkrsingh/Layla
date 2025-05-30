// utils/ResponseHandler.java
package org.layla.utils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ResponseHandler {
    private final AbsSender bot;
    private final String botUsername;

    public ResponseHandler(AbsSender bot, String botUsername) {
        this.bot = bot;
        this.botUsername = botUsername;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public void sendResponse(Long chatId, String text) throws TelegramApiException {
        SendMessage response = new SendMessage();
        response.setChatId(chatId.toString());
        response.setText(text);
        bot.execute(response);
    }

    public void sendWelcomeMessage(String chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        bot.execute(message);
    }

    public void deleteMessage(String chatId, Integer messageId) throws TelegramApiException {
        bot.execute(new DeleteMessage(chatId, messageId));
    }
}