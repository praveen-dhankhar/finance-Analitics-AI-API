package com.financeapp.entity;

import com.financeapp.entity.enums.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Account entity representing user financial accounts
 */
@Entity
@Table(name = "accounts", indexes = {
		@Index(name = "idx_accounts_owner", columnList = "owner_id"),
		@Index(name = "idx_accounts_type", columnList = "type")
})
@EntityListeners(AuditingEntityListener.class)
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Account name is required")
	@Size(min = 2, max = 100, message = "Account name must be between 2 and 100 characters")
	@Column(nullable = false, length = 100)
	private String name;

	@NotNull(message = "Account type is required")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private AccountType type;

	@NotNull(message = "Balance is required")
	@DecimalMin(value = "0.00", message = "Balance must be non-negative")
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal balance = BigDecimal.ZERO;

	@Size(max = 500, message = "Description must not exceed 500 characters")
	@Column(length = 500)
	private String description;

	@NotNull(message = "Account owner is required")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_accounts_owner"))
	private User owner;

	@OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Transaction> transactions = new HashSet<>();

	@CreatedDate
	@Column(updatable = false, nullable = true)
	private OffsetDateTime createdAt;

	@LastModifiedDate
	@Column(nullable = true)
	private OffsetDateTime updatedAt;

	// Constructors
	public Account() {}

	public Account(String name, AccountType type, User owner) {
		this.name = name;
		this.type = type;
		this.owner = owner;
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

	public AccountType getType() {
		return type;
	}

	public void setType(AccountType type) {
		this.type = type;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
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

	// Helper methods
	public void addTransaction(Transaction transaction) {
		transactions.add(transaction);
		transaction.setAccount(this);
	}

	public void removeTransaction(Transaction transaction) {
		transactions.remove(transaction);
		transaction.setAccount(null);
	}

	// equals, hashCode, and toString
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Account account = (Account) o;
		return Objects.equals(id, account.id) &&
			   Objects.equals(name, account.name) &&
			   type == account.type &&
			   Objects.equals(owner, account.owner);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, type, owner);
	}

	@Override
	public String toString() {
		return "Account{" +
				"id=" + id +
				", name='" + name + '\'' +
				", type=" + type +
				", balance=" + balance +
				", owner=" + (owner != null ? owner.getUsername() : null) +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				'}';
	}
}


