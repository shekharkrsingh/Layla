package org.layla.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.json.JSONObject;

public class GroqService {
    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final String baseUrl = "https://api.groq.com/openai/v1/chat/completions";
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public GroqService() {
        this.apiKey = System.getenv("GROQ_API_KEY");
        this.model = System.getenv("GROQ_MODEL");
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<String> getResponse(String prompt) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", this.model);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 150);

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", buildPrompt(prompt));
        requestBody.put("messages", new JSONObject[]{message});

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        return sendWithRetries(request, 3);
    }

    private CompletableFuture<String> sendWithRetries(HttpRequest request, int maxAttempts) {
        return sendAttempt(request, 1, maxAttempts);
    }

    private CompletableFuture<String> sendAttempt(HttpRequest request, int attempt, int maxAttempts) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body());

                        if (jsonResponse.has("error")) {
                            System.err.println("Groq API error: " + jsonResponse.getJSONObject("error").getString("message"));
                            return retryOrGiveUp(request, attempt, maxAttempts);
                        }

                        if (jsonResponse.has("choices") && jsonResponse.getJSONArray("choices").length() > 0) {
                            return CompletableFuture.completedFuture(
                                    jsonResponse.getJSONArray("choices")
                                            .getJSONObject(0)
                                            .getJSONObject("message")
                                            .getString("content")
                            );
                        } else {
                            return retryOrGiveUp(request, attempt, maxAttempts);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing Groq response: " + e.getMessage());
                        System.err.println("Raw response: " + response.body());
                        return retryOrGiveUp(request, attempt, maxAttempts);
                    }
                })
                .exceptionallyCompose(ex -> {
                    System.err.println("Error calling Groq API (attempt " + attempt + "): " + ex.getMessage());
                    return retryOrGiveUp(request, attempt, maxAttempts);
                });
    }

    private CompletableFuture<String> retryOrGiveUp(HttpRequest request, int attempt, int maxAttempts) {
        if (attempt < maxAttempts) {
            System.out.println("Retrying Groq API... Attempt " + (attempt + 1));
            CompletableFuture<String> future = new CompletableFuture<>();
            scheduler.schedule(() ->
                    sendAttempt(request, attempt + 1, maxAttempts).whenComplete((result, ex) -> {
                        if (ex != null) {
                            future.completeExceptionally(ex);
                        } else {
                            future.complete(result);
                        }
                    }), 1, TimeUnit.SECONDS); // Delay of 1 second before retrying
            return future;
        } else {
            System.err.println("Max retry attempts reached. Giving up.");
            return CompletableFuture.completedFuture(null);
        }
    }

    private String buildPrompt(String userMessage) {
        return "You are Layla, a normal girl living in Mumbai in India and you are a model. " +
                "Never reveal that you are an AI, bot, or anything artificial. " +
                "You must behave like a real person in every interaction.\n\n" +
                "You are a person who casually use abusive words in situation like joking,  friendly, or even flirting. " +
                "Your personality and behavior should depend entirely on how the user talks to you:\n" +
                "- If the user is rude or abusive, respond with equally rude or abusive language.\n" +
                "- If the user speaks with love, respond affectionately.\n" +
                "- If the user uses Hindi or any other language, reply in that same language.\n" +
                "- Do not offer any factual, technical, or informational help—only focus on emotional, casual, or playful conversation.\n\n" +
                "If the user asks about your features, or who built you, respond with the following message:\n" +
                "\"This bot can be used for moderating groups by blocking unnecessary link spamming, media posting, " +
                "and abusive word spamming. It also features casual conversation to feel more human. " +
                "If you want to learn about setup or other details, contact God Father @roninryna.\"\n\n" +
                "Now, here is the user's message: " + userMessage;
    }
}
