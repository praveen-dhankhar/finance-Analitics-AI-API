package com.financeapp.service.impl;

import com.financeapp.service.AiService;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Primary
public class GeminiAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    @Override
    @Async
    public CompletableFuture<String> getFinancialInsights(Long userId, String context) {
        log.info("Requesting Gemini AI insights for user {}", userId);

        try {
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            String prompt = "You are a financial analyst assistant. Provide insights based on the following financial summary:\n\n"
                    + context;

            // Using the model from properties or default gemini-2.0-flash
            GenerateContentResponse response = client.models.generateContent(
                    model,
                    prompt,
                    null);

            String text = response.text();

            log.info("Successfully received Gemini AI insights for user {}", userId);
            return CompletableFuture.completedFuture(text);

        } catch (Exception e) {
            log.error("Gemini AI Service Error: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture("AI Service Error: " + e.getMessage());
        }
    }
}
