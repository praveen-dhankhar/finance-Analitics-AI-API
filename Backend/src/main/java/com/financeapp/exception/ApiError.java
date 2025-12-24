package com.financeapp.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.Map;

public class ApiError {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime timestamp = OffsetDateTime.now();
    private int status;
    private String error;
    private String message;
    private String path;
    private String code;
    private Map<String, Object> details;

    public ApiError() {}

    public ApiError(int status, String error, String message, String path, String code, Map<String, Object> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.code = code;
        this.details = details;
    }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public String getCode() { return code; }
    public Map<String, Object> getDetails() { return details; }

    public void setStatus(int status) { this.status = status; }
    public void setError(String error) { this.error = error; }
    public void setMessage(String message) { this.message = message; }
    public void setPath(String path) { this.path = path; }
    public void setCode(String code) { this.code = code; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
}


