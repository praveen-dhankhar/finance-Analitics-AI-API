package com.financeapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table(name = "forecast_configs")
@EntityListeners(AuditingEntityListener.class)
public class ForecastConfig {

    public enum AlgorithmType {
        SMA,
        EWMA,
        LINEAR_REGRESSION,
        SEASONAL_DECOMPOSITION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm", nullable = false, length = 64)
    private AlgorithmType algorithm;

    @Column(name = "window_size")
    private Integer windowSize;

    @Column(name = "smoothing_factor")
    private Double smoothingFactor; // for EWMA (alpha)

    @Column(name = "season_length")
    private Integer seasonLength; // for seasonal decomposition

    @Column(name = "category", length = 128)
    private String category; // optional filter

    @Column(name = "transaction_type", length = 64)
    private String transactionType; // optional filter

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public ForecastConfig() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public AlgorithmType getAlgorithm() { return algorithm; }
    public void setAlgorithm(AlgorithmType algorithm) { this.algorithm = algorithm; }
    public Integer getWindowSize() { return windowSize; }
    public void setWindowSize(Integer windowSize) { this.windowSize = windowSize; }
    public Double getSmoothingFactor() { return smoothingFactor; }
    public void setSmoothingFactor(Double smoothingFactor) { this.smoothingFactor = smoothingFactor; }
    public Integer getSeasonLength() { return seasonLength; }
    public void setSeasonLength(Integer seasonLength) { this.seasonLength = seasonLength; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}


