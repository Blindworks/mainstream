package com.mainstream.user.service;

import com.mainstream.user.dto.UserDeletionResultDto;
import com.mainstream.user.dto.UserDto;
import com.mainstream.user.dto.UserRegistrationDto;
import com.mainstream.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    UserDto registerUser(UserRegistrationDto registrationDto);

    Optional<UserDto> findByEmail(String email);

    Optional<UserDto> findById(Long id);

    UserDto updateUser(Long id, UserDto userDto);

    void deactivateUser(Long id);

    void activateUser(Long id);

    Page<UserDto> findAllUsers(Pageable pageable);

    Page<UserDto> findPublicUsers(Pageable pageable);

    Page<UserDto> searchUsers(String searchTerm, Pageable pageable);

    long countActiveUsers();

    boolean existsByEmail(String email);

    UserDto updateRole(Long id, User.Role role);

    Page<UserDto> findUsersByRole(User.Role role, Pageable pageable);

    UserDto updateAvatar(Long id, String avatarUrl);

    UserDto deleteAvatar(Long id);

    /**
     * GDPR-compliant permanent deletion of user account and all associated data.
     * This operation is irreversible and deletes:
     * - All personal user data
     * - All runs and activities
     * - All fitness data and uploads
     * - All competition participations
     * - All subscriptions, orders, and payments
     * - Third-party integration tokens
     * - Avatar and uploaded files
     *
     * @param userId The ID of the user to delete
     * @param reason The reason for deletion (for audit trail)
     * @param requestedByUserId The ID of the user requesting the deletion (admin or self)
     * @param ipAddress The IP address of the requester
     * @param userAgent The user agent of the requester
     * @return UserDeletionResultDto containing details of what was deleted
     */
    UserDeletionResultDto deleteUserAccountPermanently(
        Long userId,
        String reason,
        Long requestedByUserId,
        String ipAddress,
        String userAgent
    );

    /**
     * Check if a user can be deleted (e.g., not an admin with active responsibilities).
     *
     * @param userId The ID of the user to check
     * @return true if the user can be deleted, false otherwise
     */
    boolean canDeleteUser(Long userId);

    /**
     * Get a preview of what will be deleted for a user (for confirmation dialog).
     *
     * @param userId The ID of the user
     * @return UserDeletionResultDto with counts but no actual deletion
     */
    UserDeletionResultDto previewUserDeletion(Long userId);
}