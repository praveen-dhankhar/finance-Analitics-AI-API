package com.financeapp.exception;

/**
 * Exception thrown when password validation fails.
 */
public class InvalidPasswordException extends RuntimeException {
    
    public InvalidPasswordException(String message) {
        super(message);
    }
    
    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static InvalidPasswordException weakPassword() {
        return new InvalidPasswordException("Password does not meet security requirements");
    }
    
    public static InvalidPasswordException incorrectPassword() {
        return new InvalidPasswordException("Incorrect password provided");
    }
    
    public static InvalidPasswordException sameAsCurrent() {
        return new InvalidPasswordException("New password must be different from current password");
    }
}
