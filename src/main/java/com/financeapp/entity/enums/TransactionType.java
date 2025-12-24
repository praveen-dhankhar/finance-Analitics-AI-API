package com.financeapp.entity.enums;

/**
 * Enum representing different types of financial transactions
 */
public enum TransactionType {
    INCOME("Income"),
    EXPENSE("Expense"),
    TRANSFER("Transfer"),
    INVESTMENT("Investment"),
    SAVINGS("Savings");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return name(); // Return the enum name instead of display name for API consistency
    }
}