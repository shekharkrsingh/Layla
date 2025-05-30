package org.layla.handlers;

import org.layla.utils.ResponseHandler;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.layla.services.GroupService;
import org.layla.models.BotConfig;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class CommandHandler {
    private final GroupService groupService;
    private final BotConfig config;
    private final ResponseHandler responseHandler;
    private final AbsSender absSender;

    public CommandHandler(GroupService groupService, BotConfig config,
                          ResponseHandler responseHandler, AbsSender absSender) {
        this.groupService = groupService;
        this.config = config;
        this.responseHandler = responseHandler;
        this.absSender = absSender;
    }

    /**
     * Handles all bot commands
     * @param message the incoming Telegram message
     * @return true if the command was handled, false otherwise
     * @throws TelegramApiException if Telegram operations fail
     */
    public boolean handleCommands(Message message) throws TelegramApiException {
        if (message == null || !message.hasText()) {
            return false;
        }

        String text = message.getText().trim().toLowerCase();
        Long chatId = message.getChatId();
        User sender = message.getFrom();

        if (text.equals("/allowgroup")) {
            return handleAllowGroupCommand(chatId, sender);
        } else if (text.equals("/removegroup")) {
            return handleRemoveGroupCommand(message);
        }

        return false;
    }

    public boolean handleAllowGroupCommand(Long chatId, User sender) throws TelegramApiException {
        if (sender.getId().equals(config.getOwnerId())) {
            processAllowGroupCommand(chatId);
        } else {
            responseHandler.sendResponse(chatId,
                    "I Love You ❤\uFE0F " + sender.getFirstName() + " \uD83D\uDE18");
        }
        return true;
    }

    private boolean handleRemoveGroupCommand(Message message) throws TelegramApiException {
        Long chatId = message.getChatId();
        User sender = message.getFrom();

        // Check if user is admin in this group
        if (isUserAdmin(chatId.toString(), sender.getId())) {
            processRemoveGroupCommand(chatId);
            return true;
        } else {
            responseHandler.sendResponse(chatId,
                    "❌ You need to be an admin in this group to remove it.");
            return true;
        }
    }

    private void processAllowGroupCommand(Long groupId) throws TelegramApiException {
        if (groupService.isGroupAllowed(groupId)) {
            responseHandler.sendResponse(groupId, "✅ This group is already in the allowed list.");
        } else {
            groupService.addAllowedGroup(groupId);
            responseHandler.sendResponse(groupId, "✅ Group has been added to the allowed list. Bot is now active here!");
        }
    }

    private void processRemoveGroupCommand(Long groupId) throws TelegramApiException {
        if (groupService.isGroupAllowed(groupId)) {
            groupService.removeAllowedGroup(groupId);
            responseHandler.sendResponse(groupId, "✅ This group has been removed from the allowed list. Bot will no longer moderate here.");
        } else {
            responseHandler.sendResponse(groupId, "ℹ️ This group wasn't in the allowed list.");
        }
    }

    private boolean isUserAdmin(String chatId, Long userId) throws TelegramApiException {
        ChatMember member = absSender.execute(new GetChatMember(chatId, userId));
        String status = member.getStatus();
        return "administrator".equals(status) || "creator".equals(status);
    }
}