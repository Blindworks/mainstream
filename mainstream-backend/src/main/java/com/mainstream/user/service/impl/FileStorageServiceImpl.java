package com.mainstream.user.service.impl;

import com.mainstream.user.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${file.upload.avatar-dir:uploads/avatars}")
    private String avatarUploadDir;

    @Value("${file.upload.route-image-dir:uploads/route-images}")
    private String routeImageUploadDir;

    @Override
    public String storeAvatar(MultipartFile file, Long userId) {
        if (!isValidImage(file)) {
            throw new IllegalArgumentException("Invalid image file");
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(avatarUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = "avatar_" + userId + "_" + UUID.randomUUID() + fileExtension;
            Path filePath = uploadPath.resolve(filename);

            // Copy file to the target location
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Avatar stored successfully for user {}: {}", userId, filename);

            // Return relative URL path
            return "/uploads/avatars/" + filename;

        } catch (IOException e) {
            log.error("Failed to store avatar for user {}", userId, e);
            throw new RuntimeException("Failed to store avatar file", e);
        }
    }

    @Override
    public void deleteAvatar(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract filename from URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(avatarUploadDir).resolve(filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Avatar deleted successfully: {}", filename);
            }
        } catch (IOException e) {
            log.error("Failed to delete avatar: {}", fileUrl, e);
            // Don't throw exception, just log it
        }
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("File is null or empty");
            return false;
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File size exceeds maximum allowed size: {} bytes", file.getSize());
            return false;
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Invalid content type: {}", contentType);
            return false;
        }

        return true;
    }

    @Override
    public String storeRouteImage(MultipartFile file, Long routeId) {
        if (!isValidImage(file)) {
            throw new IllegalArgumentException("Invalid image file");
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(routeImageUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = "route_" + routeId + "_" + UUID.randomUUID() + fileExtension;
            Path filePath = uploadPath.resolve(filename);

            // Copy file to the target location
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Route image stored successfully for route {}: {}", routeId, filename);

            // Return relative URL path
            return "/uploads/route-images/" + filename;

        } catch (IOException e) {
            log.error("Failed to store route image for route {}", routeId, e);
            throw new RuntimeException("Failed to store route image file", e);
        }
    }

    @Override
    public void deleteRouteImage(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract filename from URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(routeImageUploadDir).resolve(filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Route image deleted successfully: {}", filename);
            }
        } catch (IOException e) {
            log.error("Failed to delete route image: {}", fileUrl, e);
            // Don't throw exception, just log it
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted successfully: {}", filePath);
            } else {
                log.warn("File not found for deletion: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            // Don't throw exception, just log it
        }
    }
}
