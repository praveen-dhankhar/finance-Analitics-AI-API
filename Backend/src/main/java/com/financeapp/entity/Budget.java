package com.financeapp.entity;

import com.financeapp.entity.enums.BudgetPeriod;
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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Budget entity representing user budget plans
 */
@Entity
@Table(name = "budgets", indexes = {
		@Index(name = "idx_budgets_user", columnList = "user_id"),
		@Index(name = "idx_budgets_period", columnList = "period"),
		@Index(name = "idx_budgets_dates", columnList = "start_date, end_date")
})
@EntityListeners(AuditingEntityListener.class)
public class Budget {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Budget name is required")
	@Size(min = 2, max = 100, message = "Budget name must be between 2 and 100 characters")
	@Column(nullable = false, length = 100)
	private String name;

	@NotBlank(message = "Category is required")
	@Size(max = 100, message = "Category must not exceed 100 characters")
	@Column(nullable = false, length = 100)
	private String category;

	@NotNull(message = "Budget period is required")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private BudgetPeriod period;

	@NotNull(message = "Limit amount is required")
	@DecimalMin(value = "0.01", message = "Limit amount must be greater than 0")
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal limitAmount;

	@NotNull(message = "Start date is required")
	@Column(nullable = false)
	private LocalDate startDate;

	@NotNull(message = "End date is required")
	@Column(nullable = false)
	private LocalDate endDate;

	@Size(max = 500, message = "Description must not exceed 500 characters")
	@Column(length = 500)
	private String description;

	@NotNull(message = "User is required")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_budgets_user"))
	private User user;

	@OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Transaction> transactions = new HashSet<>();

	@CreatedDate
	@Column(updatable = false, nullable = true)
	private OffsetDateTime createdAt;

	@LastModifiedDate
	@Column(nullable = true)
	private OffsetDateTime updatedAt;

	// Constructors
	public Budget() {}

	public Budget(String name, String category, BudgetPeriod period, BigDecimal limitAmount, 
				  LocalDate startDate, LocalDate endDate, User user) {
		this.name = name;
		this.category = category;
		this.period = period;
		this.limitAmount = limitAmount;
		this.startDate = startDate;
		this.endDate = endDate;
		this.user = user;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public BudgetPeriod getPeriod() {
		return period;
	}

	public void setPeriod(BudgetPeriod period) {
		this.period = period;
	}

	public BigDecimal getLimitAmount() {
		return limitAmount;
	}

	public void setLimitAmount(BigDecimal limitAmount) {
		this.limitAmount = limitAmount;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Set<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(Set<Transaction> transactions) {
		this.transactions = transactions;
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
	public boolean isActive() {
		LocalDate now = LocalDate.now();
		return !now.isBefore(startDate) && !now.isAfter(endDate);
	}

	public boolean isExpired() {
		return LocalDate.now().isAfter(endDate);
	}

	public boolean isFuture() {
		return LocalDate.now().isBefore(startDate);
	}

	// equals, hashCode, and toString
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Budget budget = (Budget) o;
		return Objects.equals(id, budget.id) &&
			   Objects.equals(name, budget.name) &&
			   Objects.equals(category, budget.category) &&
			   period == budget.period &&
			   Objects.equals(user, budget.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, category, period, user);
	}

	@Override
	public String toString() {
		return "Budget{" +
				"id=" + id +
				", name='" + name + '\'' +
				", category='" + category + '\'' +
				", period=" + period +
				", limitAmount=" + limitAmount +
				", startDate=" + startDate +
				", endDate=" + endDate +
				", user=" + (user != null ? user.getUsername() : null) +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				'}';
	}
}


