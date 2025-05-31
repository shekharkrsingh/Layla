// handlers/MessageHandler.java
package org.layla.handlers;

import org.layla.models.BotConfig;
import org.layla.services.GroqService;
import org.layla.utils.ResponseHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.layla.services.ModerationService;
import org.layla.services.GroupService;

public class MessageHandler {
    private final ModerationService moderationService;
    private final GroupService groupService;
    private final ResponseHandler responseHandler;
    private  final Integer spamLength=200;
    private final AbsSender absSender;
    private final GroqService groqService;

    public MessageHandler(ModerationService moderationService, GroupService groupService, ResponseHandler responseHandler, AbsSender absSender, GroqService groqService) {
        this.moderationService = moderationService;
        this.groupService = groupService;
        this.responseHandler = responseHandler;
        this.absSender = absSender;
        this.groqService = groqService;
    }

    public void handleMessage(Message message) throws TelegramApiException {
        if (message == null || message.getFrom() == null) return;

        if (message.getNewChatMembers() != null && !message.getNewChatMembers().isEmpty()) {
            handleNewMembers(message);
            return;
        }

        String text=message.getText();
        if (message.hasText() && (text.toLowerCase().contains("layla") || text.toLowerCase().contains("@"+responseHandler.getBotUsername()) || (message.getChat().getType().equals("private") && message.getReplyToMessage()== null))) {
            handleLaylaResponse(message);
        }

        if(message.getReplyToMessage() != null && message.getReplyToMessage().getFrom().getUserName().equals(responseHandler.getBotUsername())){
            String finalMessage= text+"\n Layla this is your previous message: "+message.getReplyToMessage().getText();
            handleLaylaResponse(message, finalMessage);
        }

        if (shouldSkipMessage(message)) return;

        if (shouldDeleteMessage(message)) {
            responseHandler.deleteMessage(message.getChatId().toString(), message.getMessageId());
        }
    }

    private void handleNewMembers(Message message) throws TelegramApiException {
        String chatId = message.getChatId().toString();
        String chatTitle = message.getChat().getTitle() != null ?
                message.getChat().getTitle() : "the group";

        for (User newMember : message.getNewChatMembers()) {
            if (!newMember.getIsBot() || !newMember.getUserName().equals(responseHandler.getBotUsername())) {
                responseHandler.sendWelcomeMessage(chatId, generateWelcomeMessage(newMember, chatTitle));
            }
        }
    }

    private String generateWelcomeMessage(User user, String chatGroup) {
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? " " + user.getLastName() : "";
        String username = user.getUserName() != null ? " (@" + user.getUserName() + ")" : "";

        return String.format(
                "👋 Welcome\n\n%s%s%s to %s! 🎉\n\n✨ We're glad to have you here.\n\n",
                firstName, lastName, username, chatGroup
        );
    }

    private boolean shouldSkipMessage(Message message) {
        return isUserAdmin(message) || message.getFrom().getIsBot();
    }

    private boolean isUserAdmin(Message message) {
        try {
            if (message.getChat().isUserChat()) {
                // Private chats don't have admins
                return false;
            }

            String chatId = message.getChatId().toString();
            Long userId = message.getFrom().getId();

            ChatMember member = absSender.execute(new GetChatMember(chatId, userId));
            String status = member.getStatus();

            // Consider creator and administrators as admins
            return "creator".equals(status) || "administrator".equals(status);
        } catch (TelegramApiException e) {
            // If we can't check the status, assume not admin to be safe
            return false;
        }
    }

    private void handleLaylaResponse(Message message) {
        groqService.getResponse(message.getText())
                .thenAccept(response -> {
                    if (response != null && !response.trim().isEmpty()) {
                        try {
                            responseHandler.sendResponse(message.getChatId(), response);
                        } catch (TelegramApiException e) {
                            System.err.println("Failed to send Layla response: " + e.getMessage());
                        }
                    }
                });
    }
    private void handleLaylaResponse(Message message, String newText) {
        groqService.getResponse(newText)
                .thenAccept(response -> {
                    if (response != null && !response.trim().isEmpty()) {
                        try {
                            responseHandler.sendResponse(message.getChatId(), response);
                        } catch (TelegramApiException e) {
                            System.err.println("Failed to send Layla response: " + e.getMessage());
                        }
                    }
                });
    }

    private boolean shouldDeleteMessage(Message message) {
        return message.hasText() ?
                (moderationService.shouldDeleteMessage(message.getText())|| message.getForwardFrom()!=null) || (!isUserAdmin(message) && message.getText().length()>spamLength) :
                message.hasPhoto() || message.hasVideo() ||
                        message.hasDocument() || message.hasAudio() ||
                        message.hasSticker();
    }
}