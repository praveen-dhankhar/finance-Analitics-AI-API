package com.financeapp.service;

import java.util.concurrent.CompletableFuture;

public interface AiService {
    CompletableFuture<String> getFinancialInsights(Long userId, String context);
}
