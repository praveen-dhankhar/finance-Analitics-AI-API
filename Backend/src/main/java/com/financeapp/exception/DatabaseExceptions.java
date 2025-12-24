package com.financeapp.exception;

public class DatabaseExceptions {
    public static class DatabaseUnavailableException extends RuntimeException {
        public DatabaseUnavailableException(String message, Throwable cause) { super(message, cause); }
    }
    public static class MigrationConflictException extends RuntimeException {
        public MigrationConflictException(String message, Throwable cause) { super(message, cause); }
    }
    public static class ConstraintViolationAppException extends RuntimeException {
        public ConstraintViolationAppException(String message, Throwable cause) { super(message, cause); }
    }
    public static class QueryTimeoutAppException extends RuntimeException {
        public QueryTimeoutAppException(String message, Throwable cause) { super(message, cause); }
    }
}


