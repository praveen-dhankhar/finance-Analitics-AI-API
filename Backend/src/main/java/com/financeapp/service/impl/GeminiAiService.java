package com.financeapp.service.impl;

import com.financeapp.service.AiService;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Primary
public class GeminiAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Override
    @Async
    public CompletableFuture<String> getFinancialInsights(Long userId, String context) {
        log.info("Requesting Gemini AI insights for user {}", userId);

        try {
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            String retrievedContext = retrieveRelevantContext(userId, context);
            String prompt = buildPrompt(context, retrievedContext);

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

    private String retrieveRelevantContext(Long userId, String context) {
        try {
            var queryEmbedding = embeddingModel.embed(context).content();
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(5)
                    .filter(new IsEqualTo("userId", String.valueOf(userId)))
                    .build();

            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(searchRequest).matches();

            return matches.stream()
                    .map(EmbeddingMatch::embedded)
                    .map(TextSegment::text)
                    .distinct()
                    .map(text -> "- " + text)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.warn("Failed to retrieve RAG context for user {}: {}", userId, e.getMessage(), e);
            return "";
        }
    }

    private String buildPrompt(String context, String retrievedContext) {
        StringBuilder prompt = new StringBuilder(
                "You are a financial analyst assistant. Provide insights based on the financial summary. "
                        + "Use the retrieved historical transactions only when they are relevant.\n\n");

        if (!retrievedContext.isBlank()) {
            prompt.append("Relevant historical transactions:\n")
                    .append(retrievedContext)
                    .append("\n\n");
        }

        prompt.append("Current financial summary:\n")
                .append(context);

        return prompt.toString();
    }
}
