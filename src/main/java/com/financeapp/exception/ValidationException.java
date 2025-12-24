package com.financeapp.exception;

import java.util.List;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends RuntimeException {
    
    private final List<String> validationErrors;
    
    public ValidationException(String message) {
        super(message);
        this.validationErrors = List.of(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.validationErrors = List.of(message);
    }
    
    public ValidationException(List<String> validationErrors) {
        super("Validation failed: " + String.join(", ", validationErrors));
        this.validationErrors = validationErrors;
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
