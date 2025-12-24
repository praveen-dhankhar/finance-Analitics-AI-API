package com.financeapp.entity;

import com.financeapp.entity.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Transaction entity representing financial transactions
 */
@Entity
@Table(name = "transactions", indexes = {
		@Index(name = "idx_transactions_account", columnList = "account_id"),
		@Index(name = "idx_transactions_budget", columnList = "budget_id"),
		@Index(name = "idx_transactions_type", columnList = "type"),
		@Index(name = "idx_transactions_timestamp", columnList = "timestamp")
})
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Transaction type is required")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private TransactionType type;

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.01", message = "Amount must be greater than 0")
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Size(max = 255, message = "Description must not exceed 255 characters")
	@Column(length = 255)
	private String description;

	@NotNull(message = "Timestamp is required")
	@Column(nullable = false)
	private OffsetDateTime timestamp;

	@NotNull(message = "Account is required")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transactions_account"))
	private Account account;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "budget_id", foreignKey = @ForeignKey(name = "fk_transactions_budget"))
	private Budget budget;

	@CreatedDate
	@Column(updatable = false, nullable = true)
	private OffsetDateTime createdAt;

	@LastModifiedDate
	@Column(nullable = true)
	private OffsetDateTime updatedAt;

	// Constructors
	public Transaction() {}

	public Transaction(TransactionType type, BigDecimal amount, String description, 
					  OffsetDateTime timestamp, Account account) {
		this.type = type;
		this.amount = amount;
		this.description = description;
		this.timestamp = timestamp;
		this.account = account;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public TransactionType getType() {
		return type;
	}

	public void setType(TransactionType type) {
		this.type = type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public OffsetDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Budget getBudget() {
		return budget;
	}

	public void setBudget(Budget budget) {
		this.budget = budget;
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
		Transaction that = (Transaction) o;
		return Objects.equals(id, that.id) &&
			   type == that.type &&
			   Objects.equals(amount, that.amount) &&
			   Objects.equals(timestamp, that.timestamp) &&
			   Objects.equals(account, that.account);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, amount, timestamp, account);
	}

	@Override
	public String toString() {
		return "Transaction{" +
				"id=" + id +
				", type=" + type +
				", amount=" + amount +
				", description='" + description + '\'' +
				", timestamp=" + timestamp +
				", account=" + (account != null ? account.getName() : null) +
				", budget=" + (budget != null ? budget.getName() : null) +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				'}';
	}
}


