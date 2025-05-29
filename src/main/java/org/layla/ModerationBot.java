package org.layla;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModerationBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(ModerationBot.class);
    private final String botToken;
    private final Set<String> forbiddenKeywords = new HashSet<>();

    {
        List<String> forbiddenList = Arrays.asList(
                // English offensive words
                "asshole", "bastard", "bitch", "bloody", "bollocks", "butt", "c*nt", "cocksucker", "cunt",
                "damn", "dick", "dildo", "dyke", "fag", "faggot", "fap", "felch", "flick", "fuck", "gagging",
                "gook", "gyp", "hell", "homo", "hooker", "idiot", "incest", "jackass", "jizz", "kike", "klutz",
                "knee grow", "kys", "lame", "lesbo", "maggot", "mick", "milf", "mong", "moron", "muff", "nazi",
                "negro", "nigga", "nigger", "numbnuts", "paki", "piss", "piss off", "porn", "prick", "queer",
                "rape", "retard", "screw you", "shit", "shithead", "slut", "spastic", "spic", "suck my",
                "swallow", "tard", "thot", "twat", "vagina", "whore", "wop", "zoophile", "zoophilia", "anal",
                "bimbo", "blowjob", "boob", "boobs", "breasts", "butt plug", "clit", "cock", "condom", "cum",
                "deepthroat", "dickhead", "ejaculate", "fellatio", "fisting", "fornicate", "handjob", "hardcore",
                "hentai", "intercourse", "kama sutra", "kinky", "lesbian", "masturbate", "orgasm", "orgy",
                "panties", "pornography", "prostitute", "pussy", "sexting", "shemale", "sodomy", "suck",
                "threesome", "tits", "vibrator", "voyeur",

                // Indian abusive and suspicious words
                "bhosdi", "bhosdike", "chutiya", "chutiye", "lund", "gaand", "gandu", "madarchod", "behnchod",
                "bsdk", "mc", "bc", "randi", "chod", "chudai", "suar", "kutte", "harami", "nalayak",
                "kutta kamina", "sale", "kamina", "jhatu", "jhant", "gand marna", "gand faad", "chinal",
                "lulli", "loda", "bhen ke lode", "maa ke lode", "chodna", "jaat", "chamar", "bhangi", "neech",
                "hijra", "mehnat ki aulad", "launda", "chhakka"
        );

        forbiddenKeywords.addAll(forbiddenList);
    }


    public ModerationBot(String botToken) {
        this.botToken = botToken;
    }

    @Override
    public String getBotUsername() {
        return "laylaCore_bot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update == null || !update.hasMessage()) return;

            Message message = update.getMessage();
            if (message == null || message.getFrom() == null) return;

            String chatId = message.getChatId().toString();
            Integer messageId = message.getMessageId();
            User sender = message.getFrom();

            // Skip if sender is admin or the message is from the bot itself
            if (isUserAdmin(chatId, sender.getId()) || sender.getIsBot()) {
                logger.debug("Skipping admin/bot message from {}", sender.getId());
                return;
            }

            // Check content for violations
            if (shouldDeleteMessage(message)) {
                deleteMessage(chatId, messageId);
            }
        } catch (Exception e) {
            logger.error("Error processing update: {}", e.getMessage());
        }
    }

    private boolean shouldDeleteMessage(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            return containsLink(text) || containsForbiddenKeyword(text);
        }
        return message.hasPhoto() || message.hasVideo() ||
                message.hasDocument() || message.hasAudio() ||
                message.hasSticker();
    }

    private boolean isUserAdmin(String chatId, Long userId) {
        try {
            ChatMember member = execute(new GetChatMember(chatId, userId));
            String status = member.getStatus();
            return "administrator".equals(status) || "creator".equals(status);
        } catch (TelegramApiException e) {
            logger.error("Failed to check admin status: {}", e.getMessage());
            return false;
        }
    }

    private boolean containsLink(String text) {
        return text != null &&
                text.matches("(?i).*((https?://|www\\.)\\S+|t\\.me/\\S+|@\\w+).*");
    }

    private boolean containsForbiddenKeyword(String text) {
        return text != null &&
                forbiddenKeywords.stream().anyMatch(text.toLowerCase()::contains);
    }

    private void deleteMessage(String chatId, Integer messageId) {
        try {
            execute(new DeleteMessage(chatId, messageId));
            logger.info("Deleted message {} in chat {}", messageId, chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to delete message {} in chat {}: {}",
                    messageId, chatId, e.getMessage());
        }
    }

    public void addForbiddenKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            forbiddenKeywords.add(keyword.toLowerCase().trim());
        }
    }

    public void removeForbiddenKeyword(String keyword) {
        if (keyword != null) {
            forbiddenKeywords.remove(keyword.toLowerCase());
        }
    }
}