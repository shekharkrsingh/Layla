package org.layla.services;

import java.util.HashSet;
import java.util.Set;

public class ModerationService {
    private final Set<String> forbiddenKeywords = new HashSet<>();

    public ModerationService() {
        // Add default forbidden keywords
        forbiddenKeywords.add("scam");
        forbiddenKeywords.add("money");
        forbiddenKeywords.add("loan");
        forbiddenKeywords.add("free crypto");
        forbiddenKeywords.add("win");
        forbiddenKeywords.add("prize");
    }

    public boolean shouldDeleteMessage(String text) {
        if (text == null) return false;
        String lowerText = text.toLowerCase();
        return containsLink(lowerText) || forbiddenKeywords.stream().anyMatch(lowerText::contains);
    }

    private boolean containsLink(String text) {
        return text.matches("(?i).*((https?://|www\\.)\\S+|t\\.me/\\S+|@\\w+).*");
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

    public Set<String> getForbiddenKeywords() {
        return new HashSet<>(forbiddenKeywords); // Optional: to safely view them
    }
}
