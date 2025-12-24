package com.financeapp.entity.enums;

/**
 * Enum representing different categories for financial transactions
 */
public enum Category {
    // Income Categories
    SALARY("Salary"),
    BONUS("Bonus"),
    FREELANCE("Freelance"),
    INVESTMENT_RETURN("Investment Return"),
    RENTAL_INCOME("Rental Income"),
    OTHER_INCOME("Other Income"),

    // Expense Categories
    HOUSING("Housing"),
    FOOD("Food & Dining"),
    TRANSPORTATION("Transportation"),
    TRANSPORT("Transport"), // Alias for TRANSPORTATION
    HEALTHCARE("Healthcare"),
    ENTERTAINMENT("Entertainment"),
    EDUCATION("Education"),
    SHOPPING("Shopping"),
    UTILITIES("Utilities"),
    INSURANCE("Insurance"),
    TAXES("Taxes"),
    DEBT_PAYMENT("Debt Payment"),
    SAVINGS("Savings"),
    INVESTMENT("Investment"),
    OTHER_EXPENSE("Other Expense");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return name(); // Return the enum name instead of display name for API consistency
    }

    /**
     * Check if this category is typically associated with income
     */
    public boolean isIncomeCategory() {
        return this == SALARY || this == BONUS || this == FREELANCE || 
               this == INVESTMENT_RETURN || this == RENTAL_INCOME || this == OTHER_INCOME;
    }

    /**
     * Check if this category is typically associated with expenses
     */
    public boolean isExpenseCategory() {
        return !isIncomeCategory();
    }
}
