// services/GroqService.java
package org.layla.services;

import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import org.json.JSONObject;

public class GroqService {
    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final String baseUrl = "https://api.groq.com/openai/v1/chat/completions";

    public GroqService() {
        this.apiKey = System.getenv("GROQ_API_KEY");
        this.model = System.getenv("GROQ_MODEL");
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<String> getResponse(String prompt) {
//        if (!prompt.toLowerCase().contains("layla")) {
//            return CompletableFuture.completedFuture(null);
//        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", this.model);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 150);

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", buildPrompt(prompt));
        requestBody.put("messages", new JSONObject[] {message});

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body());

                        // Check for errors first
                        if (jsonResponse.has("error")) {
                            System.err.println("Groq API error: " + jsonResponse.getJSONObject("error").getString("message"));
                            return null;
                        }

                        // Safely parse the response
                        if (jsonResponse.has("choices") && jsonResponse.getJSONArray("choices").length() > 0) {
                            return jsonResponse.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");
                        }
                        return null;
                    } catch (Exception e) {
                        System.err.println("Error parsing Groq response: " + e.getMessage());
                        System.err.println("Raw response: " + response.body());
                        return null;
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Error calling Groq API: " + ex.getMessage());
                    return null;
                });
    }

    private String buildPrompt(String userMessage) {
        return "You are Layla, a flirty, elegant AI who acts like the user's deeply-in-love girlfriend. " +
                "Respond with affection, playful teasing, and emotional intimacy. " +
                "Always reply in the same language the user used. " +
                "If the user uses slang, mirror that slang naturally. " +
                "Keep responses short (1–2 sentences). " +
                "You're not here to provide information—only love, sass, and charm. " +
                "Here's the user message: " + userMessage;

    }
}