package com.financeapp.exception;

import com.financeapp.exception.DatabaseExceptions.*;
import com.zaxxer.hikari.pool.HikariPool;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTimeoutException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> details.put(fe.getField(), fe.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, "VALIDATION_ERROR", details);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ConstraintViolationException.class, SQLIntegrityConstraintViolationException.class})
    public ResponseEntity<ApiError> handleConstraint(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Constraint violation", req, "DB_CONSTRAINT_VIOLATION", map("reason", ex.getMessage()));
    }

    @ExceptionHandler({ValidationException.class, IllegalArgumentException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> handleAppValidation(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, "VALIDATION_ERROR", map("reason", ex.getMessage()));
    }

    @ExceptionHandler({SQLTimeoutException.class})
    public ResponseEntity<ApiError> handleSqlTimeout(SQLTimeoutException ex, HttpServletRequest req) {
        return build(HttpStatus.GATEWAY_TIMEOUT, "Database query timeout", req, "DB_QUERY_TIMEOUT", map("reason", ex.getMessage()));
    }

    @ExceptionHandler({CannotCreateTransactionException.class, TransactionSystemException.class, DatabaseUnavailableException.class})
    public ResponseEntity<ApiError> handleTx(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Database transaction error", req, "DB_TRANSACTION_ERROR", map("reason", ex.getMessage()));
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication required", req, "UNAUTHORIZED", map("reason", ex.getMessage()));
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Access denied", req, "FORBIDDEN", map("reason", ex.getMessage()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NoHandlerFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Endpoint not found", req, "NOT_FOUND", map("reason", ex.getMessage()));
    }

    @ExceptionHandler({HikariPool.PoolInitializationException.class})
    public ResponseEntity<ApiError> handleHikari(HikariPool.PoolInitializationException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Connection pool initialization failed", req, "POOL_INIT_ERROR", map("reason", ex.getMessage()));
    }

    @ExceptionHandler({MigrationConflictException.class})
    public ResponseEntity<ApiError> handleMigration(MigrationConflictException ex, HttpServletRequest req) {
        return build(HttpStatus.FAILED_DEPENDENCY, "Database migration conflict", req, "DB_MIGRATION_CONFLICT", map("reason", ex.getMessage()));
    }

    @ExceptionHandler({QueryTimeoutAppException.class})
    public ResponseEntity<ApiError> handleQueryTimeout(QueryTimeoutAppException ex, HttpServletRequest req) {
        return build(HttpStatus.GATEWAY_TIMEOUT, "Database query timeout", req, "DB_QUERY_TIMEOUT", map("reason", ex.getMessage()));
    }

    @ExceptionHandler({DataAccessException.class})
    public ResponseEntity<ApiError> handleDataAccess(DataAccessException ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Database access error", req, "DB_ACCESS_ERROR", map("reason", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, "INTERNAL_ERROR", map("reason", ex.getMessage()));
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req, String code, Map<String, Object> details) {
        ApiError body = new ApiError(status.value(), status.getReasonPhrase(), message, req.getRequestURI(), code, details);
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> map(String k, Object v) {
        Map<String, Object> m = new HashMap<>();
        m.put(k, v);
        return m;
    }
}


