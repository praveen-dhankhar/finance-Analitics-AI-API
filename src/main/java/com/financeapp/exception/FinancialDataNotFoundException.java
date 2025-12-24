package com.financeapp.exception;

/**
 * Exception thrown when financial data is not found
 */
public class FinancialDataNotFoundException extends RuntimeException {
    
    public FinancialDataNotFoundException(String message) {
        super(message);
    }
    
    public FinancialDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
