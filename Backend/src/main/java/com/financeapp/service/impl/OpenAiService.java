package com.financeapp.service.impl;

import com.financeapp.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

//@Service
public class OpenAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);

    private final RestClient restClient;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    public OpenAiService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    @Async
    public CompletableFuture<String> getFinancialInsights(Long userId, String context) {
        log.info("Requesting AI insights for user {} with API key ending in: {}", userId,
                apiKey.substring(Math.max(0, apiKey.length() - 8)));

        try {
            // Construct the request body for Chat Completions
            // Model can be configurable, hardcoded for now
            // For OpenRouter, we often use the 'openai/gpt-3.5-turbo' model ID or similar
            // Using free model for testing - change to "openai/gpt-4o" when credits are
            // available
            String model = "meta-llama/llama-3.2-3b-instruct:free";

            var requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "You are a financial analyst assistant. Provide insights based on the given summary."),
                            Map.of("role", "user", "content", context)));

            Map response = restClient.post()
                    .uri(apiUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "http://localhost:8080") // Site URL
                    .header("X-Title", "FinFlow AI") // Site Title
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("choices")) {
                List choices = (List) response.get("choices");
                if (!choices.isEmpty()) {
                    Map firstChoice = (Map) choices.get(0);
                    Map message = (Map) firstChoice.get("message");
                    return CompletableFuture.completedFuture((String) message.get("content"));
                }
            }

            return CompletableFuture.completedFuture("No insights generated.");

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("AI Service HTTP Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 402 || e.getStatusCode().value() == 401) {
                return CompletableFuture.completedFuture(
                        "AI Service Unavailable: Insufficient credits or invalid key (" + e.getStatusCode() + ").");
            }
            return CompletableFuture.completedFuture("AI Service Error: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Error calling AI service", e);
            return CompletableFuture.completedFuture("Failed to generate insights: " + e.getMessage());
        }
    }
}
