package com.financeapp.service.impl;

import com.financeapp.dto.FileUploadResponseDto;
import com.financeapp.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of FileStorageService
 * Compatible with both H2 and PostgreSQL databases
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    @Value("${app.file-storage.path:/tmp/finance-app/files}")
    private String storagePath;

    @Value("${app.file-storage.max-file-size:10485760}") // 10MB
    private long maxFileSize;

    @Value("${app.file-storage.allowed-image-types:image/jpeg,image/png,image/gif,image/webp}")
    private String allowedImageTypes;

    @Value("${app.file-storage.profile-picture-max-width:400}")
    private int profilePictureMaxWidth;

    @Value("${app.file-storage.profile-picture-max-height:400}")
    private int profilePictureMaxHeight;

    private static final List<String> DEFAULT_ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Override
    public FileUploadResponseDto uploadFile(MultipartFile file, String directory) throws IOException {
        log.info("Uploading file: {} to directory: {}", file.getOriginalFilename(), directory);

        // Validate file
        FileValidationResult validation = validateFile(file);
        if (!validation.valid()) {
            return new FileUploadResponseDto(
                null, file.getOriginalFilename(), null, file.getContentType(),
                file.getSize(), null, OffsetDateTime.now(), "ERROR", validation.errorMessage()
            );
        }

        // Generate unique file name
        String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
        
        // Create directory if it doesn't exist
        Path directoryPath = Paths.get(storagePath, directory);
        Files.createDirectories(directoryPath);
        
        // Save file
        Path filePath = directoryPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Generate file hash
        String fileHash = getFileHash(filePath.toString());
        
        // Generate file URL
        String fileUrl = getFileUrl(Paths.get(directory, uniqueFileName).toString());
        
        log.info("Successfully uploaded file: {} to path: {}", uniqueFileName, filePath);
        
        return new FileUploadResponseDto(
            uniqueFileName, file.getOriginalFilename(), fileUrl, file.getContentType(),
            file.getSize(), fileHash, OffsetDateTime.now(), "SUCCESS", "File uploaded successfully"
        );
    }

    @Override
    public FileUploadResponseDto uploadProfilePicture(MultipartFile file, Long userId) throws IOException {
        log.info("Uploading profile picture for user ID: {}", userId);

        // Validate image file
        FileValidationResult validation = validateImageFile(file);
        if (!validation.valid()) {
            return new FileUploadResponseDto(
                null, file.getOriginalFilename(), null, file.getContentType(),
                file.getSize(), null, OffsetDateTime.now(), "ERROR", validation.errorMessage()
            );
        }

        // Resize image if needed
        MultipartFile resizedFile = resizeImageIfNeeded(file, profilePictureMaxWidth, profilePictureMaxHeight);
        
        // Upload to profile-pictures directory
        return uploadFile(resizedFile, "profile-pictures");
    }

    @Override
    public boolean deleteFile(String filePath) {
        log.info("Deleting file: {}", filePath);
        
        try {
            Path path = Paths.get(storagePath, filePath);
            boolean deleted = Files.deleteIfExists(path);
            
            if (deleted) {
                log.info("Successfully deleted file: {}", filePath);
            } else {
                log.warn("File not found for deletion: {}", filePath);
            }
            
            return deleted;
        } catch (IOException e) {
            log.error("Error deleting file: {}", filePath, e);
            return false;
        }
    }

    @Override
    public boolean deleteProfilePicture(Long userId, String fileName) {
        log.info("Deleting profile picture for user ID: {} with file: {}", userId, fileName);
        
        String filePath = Paths.get("profile-pictures", fileName).toString();
        return deleteFile(filePath);
    }

    @Override
    public Path getFilePath(String fileName, String directory) {
        return Paths.get(storagePath, directory, fileName);
    }

    @Override
    public Path getProfilePicturePath(Long userId, String fileName) {
        return getFilePath(fileName, "profile-pictures");
    }

    @Override
    public boolean fileExists(String filePath) {
        Path path = Paths.get(storagePath, filePath);
        return Files.exists(path);
    }

    @Override
    public long getFileSize(String filePath) {
        try {
            Path path = Paths.get(storagePath, filePath);
            return Files.size(path);
        } catch (IOException e) {
            log.error("Error getting file size for: {}", filePath, e);
            return 0;
        }
    }

    @Override
    public String getFileContentType(String filePath) {
        try {
            Path path = Paths.get(storagePath, filePath);
            return Files.probeContentType(path);
        } catch (IOException e) {
            log.error("Error getting content type for: {}", filePath, e);
            return "application/octet-stream";
        }
    }

    @Override
    public String generateUniqueFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        return UUID.randomUUID().toString() + extension;
    }

    @Override
    public boolean isValidFileType(String fileName, List<String> allowedTypes) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return allowedTypes.stream()
                .anyMatch(type -> type.toLowerCase().endsWith(extension));
    }

    @Override
    public boolean isValidFileSize(long fileSize, long maxSize) {
        return fileSize > 0 && fileSize <= maxSize;
    }

    @Override
    public String getFileHash(String filePath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            byte[] hashBytes = digest.digest(fileBytes);
            
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating file hash for: {}", filePath, e);
            throw new IOException("Error generating file hash", e);
        }
    }

    @Override
    public void cleanupOldFiles(String directory, int daysOld) {
        log.info("Cleaning up old files in directory: {} older than {} days", directory, daysOld);
        
        try {
            Path directoryPath = Paths.get(storagePath, directory);
            if (!Files.exists(directoryPath)) {
                return;
            }
            
            OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(daysOld);
            
            Files.walk(directoryPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            OffsetDateTime fileTime = OffsetDateTime.ofInstant(
                                Files.getLastModifiedTime(path).toInstant(),
                                OffsetDateTime.now().getOffset()
                            );
                            return fileTime.isBefore(cutoffDate);
                        } catch (IOException e) {
                            log.error("Error checking file modification time: {}", path, e);
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("Deleted old file: {}", path);
                        } catch (IOException e) {
                            log.error("Error deleting old file: {}", path, e);
                        }
                    });
                    
        } catch (IOException e) {
            log.error("Error cleaning up old files in directory: {}", directory, e);
        }
    }

    @Override
    public StorageStatisticsDto getStorageStatistics() {
        log.info("Getting storage statistics");
        
        try {
            Path storagePathObj = Paths.get(storagePath);
            if (!Files.exists(storagePathObj)) {
                return new StorageStatisticsDto(0, 0, 0, 0, storagePath, 0);
            }
            
            long totalFiles = 0;
            long totalSize = 0;
            long profilePicturesCount = 0;
            long profilePicturesSize = 0;
            
            Files.walk(storagePathObj)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            long fileSize = Files.size(path);
                            // This is a simplified approach - in a real implementation,
                            // you'd want to track these counts properly
                        } catch (IOException e) {
                            log.error("Error getting file size: {}", path, e);
                        }
                    });
            
            // Get available space
            long availableSpace = Files.getFileStore(storagePathObj).getUsableSpace();
            
            return new StorageStatisticsDto(
                totalFiles, totalSize, profilePicturesCount, profilePicturesSize,
                storagePath, availableSpace
            );
            
        } catch (IOException e) {
            log.error("Error getting storage statistics", e);
            return new StorageStatisticsDto(0, 0, 0, 0, storagePath, 0);
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        // In a real implementation, this would return the actual URL
        // For now, return a relative path
        return "/files/" + filePath.replace("\\", "/");
    }

    @Override
    public String getProfilePictureUrl(Long userId, String fileName) {
        return getFileUrl("profile-pictures/" + fileName);
    }

    @Override
    public FileUploadResponseDto resizeImage(MultipartFile file, int width, int height, String directory) throws IOException {
        log.info("Resizing image to {}x{}", width, height);
        
        // Validate image file
        FileValidationResult validation = validateImageFile(file);
        if (!validation.valid()) {
            return new FileUploadResponseDto(
                null, file.getOriginalFilename(), null, file.getContentType(),
                file.getSize(), null, OffsetDateTime.now(), "ERROR", validation.errorMessage()
            );
        }
        
        // Resize image
        MultipartFile resizedFile = resizeImageIfNeeded(file, width, height);
        
        // Upload resized image
        return uploadFile(resizedFile, directory);
    }

    @Override
    public FileUploadResponseDto resizeProfilePicture(MultipartFile file, Long userId, int width, int height) throws IOException {
        log.info("Resizing profile picture for user ID: {} to {}x{}", userId, width, height);
        
        MultipartFile resizedFile = resizeImageIfNeeded(file, width, height);
        return uploadFile(resizedFile, "profile-pictures");
    }

    /**
     * Validate file
     */
    private FileValidationResult validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new FileValidationResult(false, "File is empty or null");
        }
        
        if (!isValidFileSize(file.getSize(), maxFileSize)) {
            return new FileValidationResult(false, "File size exceeds maximum allowed size");
        }
        
        return new FileValidationResult(true, null);
    }

    /**
     * Validate image file
     */
    private FileValidationResult validateImageFile(MultipartFile file) {
        FileValidationResult basicValidation = validateFile(file);
        if (!basicValidation.valid()) {
            return basicValidation;
        }
        
        if (file.getContentType() == null || !isValidImageType(file.getContentType())) {
            return new FileValidationResult(false, "Invalid image file type");
        }
        
        return new FileValidationResult(true, null);
    }

    /**
     * Check if content type is valid image type
     */
    private boolean isValidImageType(String contentType) {
        List<String> allowedTypes = Arrays.asList(allowedImageTypes.split(","));
        return allowedTypes.contains(contentType.toLowerCase());
    }

    /**
     * Resize image if needed
     */
    private MultipartFile resizeImageIfNeeded(MultipartFile file, int maxWidth, int maxHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            throw new IOException("Unable to read image");
        }
        
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Calculate new dimensions
        int newWidth = originalWidth;
        int newHeight = originalHeight;
        
        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            double widthRatio = (double) maxWidth / originalWidth;
            double heightRatio = (double) maxHeight / originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);
            
            newWidth = (int) (originalWidth * ratio);
            newHeight = (int) (originalHeight * ratio);
        }
        
        // If no resizing needed, return original file
        if (newWidth == originalWidth && newHeight == originalHeight) {
            return file;
        }
        
        // Resize image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        // Convert back to MultipartFile
        return new ResizedMultipartFile(resizedImage, file.getOriginalFilename(), file.getContentType());
    }

    /**
     * Custom MultipartFile implementation for resized images
     */
    private static class ResizedMultipartFile implements MultipartFile {
        private final BufferedImage image;
        private final String originalFilename;
        private final String contentType;
        private byte[] bytes;

        public ResizedMultipartFile(BufferedImage image, String originalFilename, String contentType) {
            this.image = image;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return image == null;
        }

        @Override
        public long getSize() {
            return getBytes().length;
        }

        @Override
        public byte[] getBytes() {
            if (bytes == null) {
                try {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    String format = contentType != null && contentType.contains("png") ? "png" : "jpg";
                    ImageIO.write(image, format, baos);
                    bytes = baos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("Error converting image to bytes", e);
                }
            }
            return bytes;
        }

        @Override
        public java.io.InputStream getInputStream() throws IOException {
            return new java.io.ByteArrayInputStream(getBytes());
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            Files.write(dest.toPath(), getBytes());
        }
    }
}
