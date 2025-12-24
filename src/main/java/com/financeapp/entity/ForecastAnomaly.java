package com.financeapp.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "forecast_anomalies")
@EntityListeners(AuditingEntityListener.class)
public class ForecastAnomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id")
    private ForecastConfig config; // optional

    @Column(name = "event_date", nullable = false)
    private LocalDate date;

    @Column(name = "anomaly_value", nullable = false)
    private Double value;

    @Column(name = "zscore")
    private Double zscore;

    @Column(name = "params_json", columnDefinition = "TEXT")
    private String paramsJson;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ForecastConfig getConfig() { return config; }
    public void setConfig(ForecastConfig config) { this.config = config; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public Double getZscore() { return zscore; }
    public void setZscore(Double zscore) { this.zscore = zscore; }
    public String getParamsJson() { return paramsJson; }
    public void setParamsJson(String paramsJson) { this.paramsJson = paramsJson; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}


