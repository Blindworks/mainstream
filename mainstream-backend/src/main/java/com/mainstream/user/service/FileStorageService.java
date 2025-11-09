package com.mainstream.user.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Store an avatar file for a user
     * @param file the file to store
     * @param userId the user's ID
     * @return the URL/path where the file is stored
     */
    String storeAvatar(MultipartFile file, Long userId);

    /**
     * Delete an avatar file
     * @param fileUrl the URL/path of the file to delete
     */
    void deleteAvatar(String fileUrl);

    /**
     * Validate if the file is a valid image
     * @param file the file to validate
     * @return true if valid, false otherwise
     */
    boolean isValidImage(MultipartFile file);
}
