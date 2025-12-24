package com.financeapp.service;

import com.financeapp.dto.FileUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Service interface for file storage operations
 * Compatible with both H2 and PostgreSQL databases
 */
public interface FileStorageService {

    /**
     * Upload a file and return file information
     */
    FileUploadResponseDto uploadFile(MultipartFile file, String directory) throws IOException;

    /**
     * Upload profile picture
     */
    FileUploadResponseDto uploadProfilePicture(MultipartFile file, Long userId) throws IOException;

    /**
     * Delete a file by path
     */
    boolean deleteFile(String filePath);

    /**
     * Delete profile picture
     */
    boolean deleteProfilePicture(Long userId, String fileName);

    /**
     * Get file path for a given file name
     */
    Path getFilePath(String fileName, String directory);

    /**
     * Get profile picture path
     */
    Path getProfilePicturePath(Long userId, String fileName);

    /**
     * Check if file exists
     */
    boolean fileExists(String filePath);

    /**
     * Get file size
     */
    long getFileSize(String filePath);

    /**
     * Get file content type
     */
    String getFileContentType(String filePath);

    /**
     * Generate unique file name
     */
    String generateUniqueFileName(String originalFileName);

    /**
     * Validate file type
     */
    boolean isValidFileType(String fileName, List<String> allowedTypes);

    /**
     * Validate file size
     */
    boolean isValidFileSize(long fileSize, long maxSize);

    /**
     * Get file hash for integrity checking
     */
    String getFileHash(String filePath) throws IOException;

    /**
     * Clean up old files
     */
    void cleanupOldFiles(String directory, int daysOld);

    /**
     * Get storage statistics
     */
    StorageStatisticsDto getStorageStatistics();

    /**
     * Get file URL for serving
     */
    String getFileUrl(String filePath);

    /**
     * Get profile picture URL
     */
    String getProfilePictureUrl(Long userId, String fileName);

    /**
     * Resize image file
     */
    FileUploadResponseDto resizeImage(MultipartFile file, int width, int height, String directory) throws IOException;

    /**
     * Resize profile picture
     */
    FileUploadResponseDto resizeProfilePicture(MultipartFile file, Long userId, int width, int height) throws IOException;

    /**
     * Storage statistics DTO
     */
    record StorageStatisticsDto(
        long totalFiles,
        long totalSize,
        long profilePicturesCount,
        long profilePicturesSize,
        String storagePath,
        long availableSpace
    ) {}

    /**
     * File validation result
     */
    record FileValidationResult(
        boolean valid,
        String errorMessage
    ) {}
}
