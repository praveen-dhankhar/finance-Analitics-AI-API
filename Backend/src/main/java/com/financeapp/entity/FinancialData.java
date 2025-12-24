package com.financeapp.entity;

import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;
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
 * FinancialData entity representing financial transactions and data points
 */
@Entity
@Table(name = "financial_data", indexes = {
		@Index(name = "idx_financial_data_user_date", columnList = "user_id, date"),
		@Index(name = "idx_financial_data_category", columnList = "category"),
		@Index(name = "idx_financial_data_type", columnList = "type")
})
@EntityListeners(AuditingEntityListener.class)
public class FinancialData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "User is required")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_financial_data_user"))
	private User user;

	@NotNull(message = "Date is required")
	@Column(nullable = false)
	private LocalDate date;

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.01", message = "Amount must be greater than 0")
	@Digits(integer = 15, fraction = 2, message = "Amount must have at most 15 integer digits and 2 decimal places")
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@NotNull(message = "Category is required")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private Category category;

	@Size(max = 500, message = "Description must not exceed 500 characters")
	@Column(length = 500)
	private String description;

	@NotNull(message = "Transaction type is required")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private TransactionType type;

	@CreatedDate
	@Column(updatable = false, nullable = true)
	private OffsetDateTime createdAt;

	@LastModifiedDate
	@Column(nullable = true)
	private OffsetDateTime updatedAt;

	// Constructors
	public FinancialData() {}

	public FinancialData(User user, LocalDate date, BigDecimal amount, Category category, 
						String description, TransactionType type) {
		this.user = user;
		this.date = date;
		this.amount = amount;
		this.category = category;
		this.description = description;
		this.type = type;
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

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TransactionType getType() {
		return type;
	}

	public void setType(TransactionType type) {
		this.type = type;
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
	public boolean isIncome() {
		return type == TransactionType.INCOME;
	}

	public boolean isExpense() {
		return type == TransactionType.EXPENSE;
	}

	public boolean isTransfer() {
		return type == TransactionType.TRANSFER;
	}

	public boolean isInvestment() {
		return type == TransactionType.INVESTMENT;
	}

	public boolean isSavings() {
		return type == TransactionType.SAVINGS;
	}

	// equals, hashCode, and toString
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FinancialData that = (FinancialData) o;
		return Objects.equals(id, that.id) &&
			   Objects.equals(user, that.user) &&
			   Objects.equals(date, that.date) &&
			   Objects.equals(amount, that.amount) &&
			   category == that.category &&
			   type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, user, date, amount, category, type);
	}

	@Override
	public String toString() {
		return "FinancialData{" +
				"id=" + id +
				", user=" + (user != null ? user.getUsername() : null) +
				", date=" + date +
				", amount=" + amount +
				", category=" + category +
				", description='" + description + '\'' +
				", type=" + type +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				'}';
	}
}
