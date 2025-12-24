package com.financeapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

/**
 * DTO for file upload responses
 * Compatible with both H2 and PostgreSQL databases
 */
public record FileUploadResponseDto(
    String fileName,
    String originalFileName,
    String fileUrl,
    String contentType,
    Long fileSize,
    String fileHash,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    OffsetDateTime uploadedAt,
    
    String status,
    String message
) {
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }
    
    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }
    
    public String getFileSizeFormatted() {
        if (fileSize == null) return "Unknown";
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
}
