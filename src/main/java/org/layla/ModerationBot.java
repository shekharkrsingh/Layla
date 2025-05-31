
package org.layla;

import org.layla.services.GroqService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.layla.models.BotConfig;
import org.layla.services.GroupService;
import org.layla.services.ModerationService;
import org.layla.handlers.CommandHandler;
import org.layla.handlers.MessageHandler;
import org.layla.utils.ResponseHandler;

public class ModerationBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final GroupService groupService;
    private final ModerationService moderationService;
    private final ResponseHandler responseHandler;
    private final CommandHandler commandHandler;
    private final MessageHandler messageHandler;
    private final GroqService groqService;

    public ModerationBot(String botToken, String botUsername, Long ownerId1, Long ownerId2) {
        this.groqService = new GroqService();
        this.config = new BotConfig(botUsername, botToken, ownerId1, ownerId2);
        this.groupService = new GroupService();
        this.moderationService = new ModerationService();
        this.responseHandler = new ResponseHandler(this, botUsername);
        this.commandHandler = new CommandHandler(groupService, config, responseHandler, this);
        this.messageHandler = new MessageHandler(moderationService, groupService, responseHandler, this , groqService);
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update == null || !update.hasMessage()) return;

            if (commandHandler.handleCommands(update.getMessage())) {
                return;
            }

            Long chatId = update.getMessage().getChatId();
            String chatType= update.getMessage().getChat().getType();
            if (chatId == null || (chatType.equals("group") && !groupService.isGroupAllowed(chatId))) return;

            messageHandler.handleMessage(update.getMessage());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}