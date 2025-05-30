// handlers/MessageHandler.java
package org.layla.handlers;

import org.layla.utils.ResponseHandler;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.layla.services.ModerationService;
import org.layla.services.GroupService;

public class MessageHandler {
    private final ModerationService moderationService;
    private final GroupService groupService;
    private final ResponseHandler responseHandler;

    public MessageHandler(ModerationService moderationService, GroupService groupService, ResponseHandler responseHandler) {
        this.moderationService = moderationService;
        this.groupService = groupService;
        this.responseHandler = responseHandler;
    }

    public void handleMessage(Message message) throws TelegramApiException {
        if (message == null || message.getFrom() == null) return;

        if (message.getNewChatMembers() != null && !message.getNewChatMembers().isEmpty()) {
            handleNewMembers(message);
            return;
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
        // Implement admin check logic here
        return false;
    }

    private boolean shouldDeleteMessage(Message message) {
        return message.hasText() ?
                moderationService.shouldDeleteMessage(message.getText()) :
                message.hasPhoto() || message.hasVideo() ||
                        message.hasDocument() || message.hasAudio() ||
                        message.hasSticker();
    }
}