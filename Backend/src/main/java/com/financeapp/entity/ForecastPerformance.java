package com.financeapp.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table(name = "forecast_performance")
@EntityListeners(AuditingEntityListener.class)
public class ForecastPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id", nullable = false)
    private ForecastConfig config;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "mape", nullable = false)
    private Double mape;

    @Column(name = "horizon_days", nullable = false)
    private Integer horizonDays;

    @Column(name = "lookback_days", nullable = false)
    private Integer lookbackDays;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public ForecastConfig getConfig() { return config; }
    public void setConfig(ForecastConfig config) { this.config = config; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Double getMape() { return mape; }
    public void setMape(Double mape) { this.mape = mape; }
    public Integer getHorizonDays() { return horizonDays; }
    public void setHorizonDays(Integer horizonDays) { this.horizonDays = horizonDays; }
    public Integer getLookbackDays() { return lookbackDays; }
    public void setLookbackDays(Integer lookbackDays) { this.lookbackDays = lookbackDays; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}


