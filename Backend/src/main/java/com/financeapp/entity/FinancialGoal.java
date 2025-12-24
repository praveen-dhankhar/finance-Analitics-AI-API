package com.financeapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * FinancialGoal entity representing user financial goals
 */
@Entity
@Table(name = "financial_goals", indexes = {
		@Index(name = "idx_financial_goals_user", columnList = "user_id"),
		@Index(name = "idx_financial_goals_target_date", columnList = "target_date"),
		@Index(name = "idx_financial_goals_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
public class FinancialGoal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Goal title is required")
	@Size(min = 2, max = 150, message = "Goal title must be between 2 and 150 characters")
	@Column(nullable = false, length = 150)
	private String title;

	@Size(max = 500, message = "Description must not exceed 500 characters")
	@Column(length = 500)
	private String description;

	@NotNull(message = "Target amount is required")
	@DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal targetAmount;

	@NotNull(message = "Target date is required")
	@Column(nullable = false)
	private LocalDate targetDate;

	@NotNull(message = "Current amount is required")
	@DecimalMin(value = "0.00", message = "Current amount must be non-negative")
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal currentAmount = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private GoalStatus status = GoalStatus.ACTIVE;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private GoalType type = GoalType.SAVINGS;

	@NotNull(message = "User is required")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_financial_goals_user"))
	private User user;

	@CreatedDate
	@Column(updatable = false, nullable = true)
	private OffsetDateTime createdAt;

	@LastModifiedDate
	@Column(nullable = true)
	private OffsetDateTime updatedAt;

	// Constructors
	public FinancialGoal() {}

	public FinancialGoal(String title, String description, BigDecimal targetAmount, 
						LocalDate targetDate, User user) {
		this.title = title;
		this.description = description;
		this.targetAmount = targetAmount;
		this.targetDate = targetDate;
		this.user = user;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getTargetAmount() {
		return targetAmount;
	}

	public void setTargetAmount(BigDecimal targetAmount) {
		this.targetAmount = targetAmount;
	}

	public LocalDate getTargetDate() {
		return targetDate;
	}

	public void setTargetDate(LocalDate targetDate) {
		this.targetDate = targetDate;
	}

	public BigDecimal getCurrentAmount() {
		return currentAmount;
	}

	public void setCurrentAmount(BigDecimal currentAmount) {
		this.currentAmount = currentAmount;
	}

	public GoalStatus getStatus() {
		return status;
	}

	public void setStatus(GoalStatus status) {
		this.status = status;
	}

	public GoalType getType() {
		return type;
	}

	public void setType(GoalType type) {
		this.type = type;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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
	public BigDecimal getProgressPercentage() {
		if (targetAmount.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return currentAmount.divide(targetAmount, 4, BigDecimal.ROUND_HALF_UP)
				.multiply(new BigDecimal("100"));
	}

	public boolean isCompleted() {
		return currentAmount.compareTo(targetAmount) >= 0;
	}

	public boolean isOverdue() {
		return LocalDate.now().isAfter(targetDate) && !isCompleted();
	}

	public boolean isActive() {
		return status == GoalStatus.ACTIVE;
	}

	public boolean isPaused() {
		return status == GoalStatus.PAUSED;
	}

	public boolean isCompletedStatus() {
		return status == GoalStatus.COMPLETED;
	}

	public boolean isCancelled() {
		return status == GoalStatus.CANCELLED;
	}

	// equals, hashCode, and toString
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FinancialGoal that = (FinancialGoal) o;
		return Objects.equals(id, that.id) &&
			   Objects.equals(title, that.title) &&
			   Objects.equals(targetAmount, that.targetAmount) &&
			   Objects.equals(user, that.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, title, targetAmount, user);
	}

	@Override
	public String toString() {
		return "FinancialGoal{" +
				"id=" + id +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", targetAmount=" + targetAmount +
				", currentAmount=" + currentAmount +
				", targetDate=" + targetDate +
				", status=" + status +
				", type=" + type +
				", user=" + (user != null ? user.getUsername() : null) +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				'}';
	}

	// Enums for FinancialGoal
	public enum GoalStatus {
		ACTIVE("Active"),
		PAUSED("Paused"),
		COMPLETED("Completed"),
		CANCELLED("Cancelled");

		private final String displayName;

		GoalStatus(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	public enum GoalType {
		SAVINGS("Savings"),
		DEBT_PAYOFF("Debt Payoff"),
		INVESTMENT("Investment"),
		EMERGENCY_FUND("Emergency Fund"),
		MAJOR_PURCHASE("Major Purchase"),
		RETIREMENT("Retirement"),
		EDUCATION("Education"),
		OTHER("Other");

		private final String displayName;

		GoalType(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}
}


