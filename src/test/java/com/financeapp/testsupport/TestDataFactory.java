package com.financeapp.testsupport;

import com.financeapp.entity.FinancialData;
import com.financeapp.entity.User;
import com.financeapp.entity.enums.Category;
import com.financeapp.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public final class TestDataFactory {
    private TestDataFactory() {}

    public static User newUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(username + "@example.com");
        u.setPasswordHash("$2a$10$test");
        u.setCreatedAt(OffsetDateTime.now());
        u.setUpdatedAt(OffsetDateTime.now());
        return u;
    }

    public static FinancialData newFinancialData(User user, TransactionType type, Category category, BigDecimal amount) {
        FinancialData f = new FinancialData();
        f.setUser(user);
        f.setType(type);
        f.setCategory(category);
        f.setAmount(amount);
        f.setDate(LocalDate.now());
        f.setDescription("test " + type + " " + category);
        f.setCreatedAt(OffsetDateTime.now());
        f.setUpdatedAt(OffsetDateTime.now());
        return f;
    }
}


