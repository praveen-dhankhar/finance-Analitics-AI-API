package com.financeapp.controller;

import com.financeapp.entity.ForecastConfig;
import com.financeapp.entity.ForecastResult;
import com.financeapp.dto.ForecastDtos;
import com.financeapp.service.ForecastService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/forecasts")
public class ForecastController {

    private final ForecastService forecastService;
    private final com.financeapp.service.AiService aiService;

    public ForecastController(ForecastService forecastService, com.financeapp.service.AiService aiService) {
        this.forecastService = forecastService;
        this.aiService = aiService;
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<List<ForecastResult>>> getForecast(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(defaultValue = "7") int horizonDays) {
        ForecastConfig cfg = new ForecastConfig();
        cfg.setAlgorithm(ForecastConfig.AlgorithmType.LINEAR_REGRESSION);
        return forecastService.generateForecast(userId, cfg, LocalDate.now().plusDays(1), horizonDays)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Map<Long, List<ForecastResult>>>> batchGenerate(
            @RequestBody ForecastDtos.BatchGenerateRequest request) {
        Long userId = request.userId;
        int horizonDays = request.horizonDays != null ? request.horizonDays : 7;
        LocalDate startDate = request.startDate != null ? request.startDate : LocalDate.now().plusDays(1);
        List<ForecastConfig> configs = request.configs;
        return forecastService.batchGenerateForecasts(userId, configs, startDate, horizonDays)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/accuracy")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<List<ForecastResult>>> accuracy(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "7") int horizonDays,
            @RequestParam(defaultValue = "60") int lookbackDays) {
        ForecastConfig cfg = new ForecastConfig();
        cfg.setAlgorithm(ForecastConfig.AlgorithmType.SMA);
        cfg.setWindowSize(7);
        return forecastService
                .backtestAndStoreAccuracy(userId, cfg, LocalDate.now().plusDays(1), horizonDays, lookbackDays)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/insights")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForecastDtos.InsightsDto> insights(
            @RequestParam(required = false) Long userId) {
        ForecastDtos.InsightsDto dto = new ForecastDtos.InsightsDto();
        dto.topModels = List.of("LINEAR_REGRESSION", "SMA", "GPT-3.5-TURBO");
        dto.aggregates = Map.of("countConfigs", 5, "countResults", 120);

        String context = "Financial Summary: User has consistent upward trend in savings. " +
                "Expenses spiked last month by 15%. Top expense category: Dining Out.";

        return aiService.getFinancialInsights(userId != null ? userId : 1L, context)
                .thenApply(notes -> {
                    dto.notes = notes;
                    return ResponseEntity.ok(dto);
                }).join();
    }
}
