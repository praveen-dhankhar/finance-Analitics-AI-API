package com.financeapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Forecast entity representing ML-based financial predictions
 */
@Entity
@Table(name = "forecasts", indexes = {
		@Index(name = "idx_forecasts_user_date", columnList = "user_id, forecast_date"),
		@Index(name = "idx_forecasts_confidence", columnList = "confidence_score"),
		@Index(name = "idx_forecasts_model", columnList = "model_name")
})
@EntityListeners(AuditingEntityListener.class)
public class Forecast {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "User is required")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_forecasts_user"))
	private User user;

	@NotNull(message = "Forecast date is required")
	@Future(message = "Forecast date must be in the future")
	@Column(nullable = false, name = "forecast_date")
	private LocalDate forecastDate;

	@NotNull(message = "Predicted amount is required")
	@DecimalMin(value = "0.00", message = "Predicted amount must be non-negative")
	@Digits(integer = 15, fraction = 2, message = "Predicted amount must have at most 15 integer digits and 2 decimal places")
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal predictedAmount;

	@NotNull(message = "Confidence score is required")
	@DecimalMin(value = "0.0", message = "Confidence score must be between 0.0 and 1.0")
	@DecimalMax(value = "1.0", message = "Confidence score must be between 0.0 and 1.0")
	@Digits(integer = 1, fraction = 4, message = "Confidence score must have at most 4 decimal places")
	@Column(nullable = false, precision = 5, scale = 4)
	private BigDecimal confidenceScore;

	@NotBlank(message = "Model name is required")
	@Size(max = 100, message = "Model name must not exceed 100 characters")
	@Column(nullable = false, length = 100)
	private String modelName;

	@Size(max = 50, message = "Model version must not exceed 50 characters")
	@Column(length = 50)
	private String modelVersion;

	@Size(max = 1000, message = "Prediction context must not exceed 1000 characters")
	@Column(length = 1000)
	private String predictionContext;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ForecastType forecastType = ForecastType.INCOME_EXPENSE;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ForecastStatus status = ForecastStatus.ACTIVE;

	@CreatedDate
	@Column(updatable = false, nullable = true)
	private OffsetDateTime createdAt;

	@LastModifiedDate
	@Column(nullable = true)
	private OffsetDateTime updatedAt;

	// Constructors
	public Forecast() {}

	public Forecast(User user, LocalDate forecastDate, BigDecimal predictedAmount, 
					BigDecimal confidenceScore, String modelName) {
		this.user = user;
		this.forecastDate = forecastDate;
		this.predictedAmount = predictedAmount;
		this.confidenceScore = confidenceScore;
		this.modelName = modelName;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDate getForecastDate() {
		return forecastDate;
	}

	public void setForecastDate(LocalDate forecastDate) {
		this.forecastDate = forecastDate;
	}

	public BigDecimal getPredictedAmount() {
		return predictedAmount;
	}

	public void setPredictedAmount(BigDecimal predictedAmount) {
		this.predictedAmount = predictedAmount;
	}

	public BigDecimal getConfidenceScore() {
		return confidenceScore;
	}

	public void setConfidenceScore(BigDecimal confidenceScore) {
		this.confidenceScore = confidenceScore;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

	public String getPredictionContext() {
		return predictionContext;
	}

	public void setPredictionContext(String predictionContext) {
		this.predictionContext = predictionContext;
	}

	public ForecastType getForecastType() {
		return forecastType;
	}

	public void setForecastType(ForecastType forecastType) {
		this.forecastType = forecastType;
	}

	public ForecastStatus getStatus() {
		return status;
	}

	public void setStatus(ForecastStatus status) {
		this.status = status;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	// Business logic methods
	public boolean isHighConfidence() {
		return confidenceScore != null && confidenceScore.compareTo(new BigDecimal("0.8")) >= 0;
	}

	public boolean isMediumConfidence() {
		return confidenceScore != null && 
			   confidenceScore.compareTo(new BigDecimal("0.5")) >= 0 && 
			   confidenceScore.compareTo(new BigDecimal("0.8")) < 0;
	}

	public boolean isLowConfidence() {
		return confidenceScore != null && confidenceScore.compareTo(new BigDecimal("0.5")) < 0;
	}

	public boolean isActive() {
		return status == ForecastStatus.ACTIVE;
	}

	public boolean isExpired() {
		return status == ForecastStatus.EXPIRED;
	}

	public boolean isSuperseded() {
		return status == ForecastStatus.SUPERSEDED;
	}

	// equals, hashCode, and toString
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Forecast forecast = (Forecast) o;
		return Objects.equals(id, forecast.id) &&
			   Objects.equals(user, forecast.user) &&
			   Objects.equals(forecastDate, forecast.forecastDate) &&
			   Objects.equals(predictedAmount, forecast.predictedAmount) &&
			   Objects.equals(modelName, forecast.modelName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, user, forecastDate, predictedAmount, modelName);
	}

	@Override
	public String toString() {
		return "Forecast{" +
				"id=" + id +
				", user=" + (user != null ? user.getUsername() : null) +
				", forecastDate=" + forecastDate +
				", predictedAmount=" + predictedAmount +
				", confidenceScore=" + confidenceScore +
				", modelName='" + modelName + '\'' +
				", modelVersion='" + modelVersion + '\'' +
				", forecastType=" + forecastType +
				", status=" + status +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				'}';
	}

	// Enums for Forecast
	public enum ForecastType {
		INCOME_EXPENSE("Income/Expense"),
		BUDGET_FORECAST("Budget Forecast"),
		INVESTMENT_RETURN("Investment Return"),
		SAVINGS_GOAL("Savings Goal"),
		DEBT_PAYOFF("Debt Payoff");

		private final String displayName;

		ForecastType(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	public enum ForecastStatus {
		ACTIVE("Active"),
		EXPIRED("Expired"),
		SUPERSEDED("Superseded"),
		ARCHIVED("Archived");

		private final String displayName;

		ForecastStatus(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}
}
