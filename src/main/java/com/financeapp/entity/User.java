package com.financeapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * User entity representing application users
 */
@Entity
@Table(name = "users", uniqueConstraints = {
		@UniqueConstraint(name = "uk_users_email", columnNames = {"email"}),
		@UniqueConstraint(name = "uk_users_username", columnNames = {"username"})
})
@EntityListeners(AuditingEntityListener.class)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Username is required")
	@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
	@Column(nullable = false, length = 50, unique = true)
	private String username;

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	@Size(max = 255, message = "Email must not exceed 255 characters")
	@Column(nullable = false, length = 255, unique = true)
	private String email;

	@NotBlank(message = "Password hash is required")
	@Size(min = 8, max = 255, message = "Password hash must be between 8 and 255 characters")
	@Column(nullable = false, length = 255)
	private String passwordHash;

	@CreatedDate
	@Column(updatable = false, nullable = true)
	private OffsetDateTime createdAt;

	@LastModifiedDate
	@Column(nullable = true)
	private OffsetDateTime updatedAt;

	// Relationships
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<FinancialData> financialData = new HashSet<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Forecast> forecasts = new HashSet<>();

	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Account> accounts = new HashSet<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Budget> budgets = new HashSet<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<FinancialGoal> financialGoals = new HashSet<>();

	// Constructors
	public User() {}

	public User(String username, String email, String passwordHash) {
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
	}

	// Getters and Setters
	public Long getId() { 
		return id; 
	}

	public String getUsername() { 
		return username; 
	}

	public void setUsername(String username) { 
		this.username = username; 
	}

	public String getEmail() { 
		return email; 
	}

	public void setEmail(String email) { 
		this.email = email; 
	}

	public String getPasswordHash() { 
		return passwordHash; 
	}

	public void setPasswordHash(String passwordHash) { 
		this.passwordHash = passwordHash; 
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

	public Set<FinancialData> getFinancialData() { 
		return financialData; 
	}

	public void setFinancialData(Set<FinancialData> financialData) { 
		this.financialData = financialData; 
	}

	public Set<Forecast> getForecasts() { 
		return forecasts; 
	}

	public void setForecasts(Set<Forecast> forecasts) { 
		this.forecasts = forecasts; 
	}

	public Set<Account> getAccounts() { 
		return accounts; 
	}

	public void setAccounts(Set<Account> accounts) { 
		this.accounts = accounts; 
	}

	public Set<Budget> getBudgets() { 
		return budgets; 
	}

	public void setBudgets(Set<Budget> budgets) { 
		this.budgets = budgets; 
	}

	public Set<FinancialGoal> getFinancialGoals() { 
		return financialGoals; 
	}

	public void setFinancialGoals(Set<FinancialGoal> financialGoals) { 
		this.financialGoals = financialGoals; 
	}

	// Helper methods
	public void addFinancialData(FinancialData data) {
		financialData.add(data);
		data.setUser(this);
	}

	public void removeFinancialData(FinancialData data) {
		financialData.remove(data);
		data.setUser(null);
	}

	public void addForecast(Forecast forecast) {
		forecasts.add(forecast);
		forecast.setUser(this);
	}

	public void removeForecast(Forecast forecast) {
		forecasts.remove(forecast);
		forecast.setUser(null);
	}

	// equals, hashCode, and toString
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return Objects.equals(id, user.id) && 
			   Objects.equals(username, user.username) && 
			   Objects.equals(email, user.email);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, username, email);
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", username='" + username + '\'' +
				", email='" + email + '\'' +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				'}';
	}
}


