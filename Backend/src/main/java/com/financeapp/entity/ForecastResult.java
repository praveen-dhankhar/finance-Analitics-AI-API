package com.financeapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "forecast_results")
@EntityListeners(AuditingEntityListener.class)
public class ForecastResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id", nullable = false)
    private ForecastConfig config;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "forecast_value", nullable = false)
    private BigDecimal forecastValue;

    @Column(name = "confidence_low")
    private BigDecimal confidenceLow;

    @Column(name = "confidence_high")
    private BigDecimal confidenceHigh;

    @Column(name = "mape")
    private Double mape; // accuracy for backtests

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public ForecastResult() {}

    public Long getId() { return id; }
    public ForecastConfig getConfig() { return config; }
    public void setConfig(ForecastConfig config) { this.config = config; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public BigDecimal getForecastValue() { return forecastValue; }
    public void setForecastValue(BigDecimal forecastValue) { this.forecastValue = forecastValue; }
    public BigDecimal getConfidenceLow() { return confidenceLow; }
    public void setConfidenceLow(BigDecimal confidenceLow) { this.confidenceLow = confidenceLow; }
    public BigDecimal getConfidenceHigh() { return confidenceHigh; }
    public void setConfidenceHigh(BigDecimal confidenceHigh) { this.confidenceHigh = confidenceHigh; }
    public Double getMape() { return mape; }
    public void setMape(Double mape) { this.mape = mape; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}


