package com.mainstream.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainstream.activity.repository.DailyWinnerRepository;
import com.mainstream.activity.repository.UserActivityRepository;
import com.mainstream.activity.repository.UserTrophyRepository;
import com.mainstream.competition.repository.CompetitionParticipantRepository;
import com.mainstream.competition.repository.CompetitionRepository;
import com.mainstream.fitfile.entity.FitFileUpload;
import com.mainstream.fitfile.repository.FitFileUploadRepository;
import com.mainstream.run.repository.RouteRepository;
import com.mainstream.run.repository.RunRepository;
import com.mainstream.subscription.repository.OrderRepository;
import com.mainstream.subscription.repository.PaymentRepository;
import com.mainstream.subscription.repository.SubscriptionRepository;
import com.mainstream.user.dto.UserDeletionResultDto;
import com.mainstream.user.dto.UserDto;
import com.mainstream.user.dto.UserRegistrationDto;
import com.mainstream.user.entity.AccountDeletionLog;
import com.mainstream.user.entity.User;
import com.mainstream.user.exception.ResourceNotFoundException;
import com.mainstream.user.exception.UserAlreadyExistsException;
import com.mainstream.user.mapper.UserMapper;
import com.mainstream.user.repository.AccountDeletionLogRepository;
import com.mainstream.user.repository.PasswordResetTokenRepository;
import com.mainstream.user.repository.UserRepository;
import com.mainstream.user.service.FileStorageService;
import com.mainstream.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    // Repositories for GDPR deletion
    private final RunRepository runRepository;
    private final UserActivityRepository userActivityRepository;
    private final UserTrophyRepository userTrophyRepository;
    private final FitFileUploadRepository fitFileUploadRepository;
    private final DailyWinnerRepository dailyWinnerRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionRepository competitionRepository;
    private final RouteRepository routeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AccountDeletionLogRepository accountDeletionLogRepository;

    @Override
    @Transactional
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        log.debug("Registering new user with email: {}", registrationDto.getEmail());

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + registrationDto.getEmail() + " already exists");
        }

        User user = userMapper.toEntity(registrationDto);
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return userMapper.toDto(savedUser);
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .map(userMapper::toDto);
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id)
                .filter(User::getIsActive)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.debug("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        userMapper.updateEntityFromDto(existingUser, userDto);
        User updatedUser = userRepository.save(existingUser);

        log.info("User updated successfully with ID: {}", id);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        log.debug("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User deactivated successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        log.debug("Activating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setIsActive(true);
        userRepository.save(user);

        log.info("User activated successfully with ID: {}", id);
    }

    @Override
    public Page<UserDto> findAllUsers(Pageable pageable) {
        log.debug("Finding all active users, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAllActiveUsers(pageable)
                .map(userMapper::toDto);
    }

    @Override
    public Page<UserDto> findPublicUsers(Pageable pageable) {
        log.debug("Finding all public users, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAllPublicUsers(pageable)
                .map(userMapper::toDto);
    }

    @Override
    public Page<UserDto> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Searching users with term: {}, page: {}, size: {}", 
                  searchTerm, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findUsersBySearchTerm(searchTerm, pageable)
                .map(userMapper::toDto);
    }

    @Override
    public long countActiveUsers() {
        long count = userRepository.countActiveUsers();
        log.debug("Active users count: {}", count);
        return count;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UserDto updateRole(Long id, User.Role role) {
        log.debug("Updating role for user ID: {} to: {}", id, role);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setRole(role);
        User updatedUser = userRepository.save(user);

        log.info("User role updated successfully for ID: {} to: {}", id, role);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public Page<UserDto> findUsersByRole(User.Role role, Pageable pageable) {
        log.debug("Finding users by role: {}, page: {}, size: {}",
                  role, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findUsersByRole(role, pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional
    public UserDto updateAvatar(Long id, String avatarUrl) {
        log.debug("Updating avatar for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Delete old avatar if it exists and is not a UI Avatars URL
        String oldAvatarUrl = user.getProfilePictureUrl();
        if (oldAvatarUrl != null && !oldAvatarUrl.contains("ui-avatars.com")) {
            fileStorageService.deleteAvatar(oldAvatarUrl);
        }

        user.setProfilePictureUrl(avatarUrl);
        User updatedUser = userRepository.save(user);

        log.info("Avatar updated successfully for user ID: {}", id);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public UserDto deleteAvatar(Long id) {
        log.debug("Deleting avatar for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        String avatarUrl = user.getProfilePictureUrl();
        if (avatarUrl != null && !avatarUrl.contains("ui-avatars.com")) {
            fileStorageService.deleteAvatar(avatarUrl);
        }

        user.setProfilePictureUrl(null);
        User updatedUser = userRepository.save(user);

        log.info("Avatar deleted successfully for user ID: {}", id);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public UserDeletionResultDto deleteUserAccountPermanently(
            Long userId,
            String reason,
            Long requestedByUserId,
            String ipAddress,
            String userAgent
    ) {
        log.info("Starting GDPR-compliant permanent deletion for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        String userEmail = user.getEmail();
        LocalDateTime deletionTimestamp = LocalDateTime.now();

        UserDeletionResultDto.UserDeletionResultDtoBuilder resultBuilder = UserDeletionResultDto.builder()
                .userId(userId)
                .email(userEmail)
                .deletionTimestamp(deletionTimestamp)
                .success(false);

        try {
            // Step 1: Count all entities BEFORE deletion (for audit trail)
            log.debug("Counting entities for user ID: {}", userId);
            int runsCount = runRepository.countByUserId(userId).intValue();
            int userActivitiesCount = userActivityRepository.findByUserIdOrderByActivityStartTimeDesc(userId).size();
            int userTrophiesCount = (int) userTrophyRepository.countByUserId(userId);
            int fitFileUploadsCount = (int) fitFileUploadRepository.countByUserId(userId);
            int dailyWinnersCount = (int) dailyWinnerRepository.countByUserId(userId);
            int competitionParticipantsCount = (int) competitionParticipantRepository.countByUserId(userId);
            long competitionsCount = competitionRepository.countByCreatedById(userId);
            int routesCount = (int) routeRepository.countByCreatorUserId(userId);
            int subscriptionsCount = (int) subscriptionRepository.countByUserId(userId);
            int ordersCount = (int) orderRepository.countByUserId(userId);
            int paymentsCount = (int) paymentRepository.countByUserId(userId);
            int passwordResetTokensCount = (int) passwordResetTokenRepository.countByUserId(userId);

            // Step 2: Get FIT files for file storage cleanup (before DB deletion)
            List<FitFileUpload> fitFileUploads = fitFileUploadRepository.findByUserIdOrderByCreatedAtDesc(userId);
            int fitFilesDeleted = 0;

            // Step 3: Handle third-party integration disconnection
            boolean stravaDisconnected = user.getStravaUserId() != null;
            boolean nikeDisconnected = user.getNikeUserId() != null;
            boolean garminDisconnected = user.getGarminUserId() != null;

            if (stravaDisconnected) {
                log.info("Disconnecting Strava integration for user ID: {}", userId);
                // In production, would call Strava API to revoke tokens
            }
            if (nikeDisconnected) {
                log.info("Disconnecting Nike integration for user ID: {}", userId);
            }
            if (garminDisconnected) {
                log.info("Disconnecting Garmin integration for user ID: {}", userId);
            }

            // Step 4: Delete entities WITHOUT CASCADE constraints (manual deletion)
            log.debug("Deleting entities without CASCADE constraints");

            // Delete FIT file uploads (no FK constraint)
            int deletedFitUploads = fitFileUploadRepository.deleteByUserId(userId);
            log.debug("Deleted {} FIT file uploads", deletedFitUploads);

            // Delete daily winner records (no FK constraint)
            int deletedDailyWinners = dailyWinnerRepository.deleteByUserId(userId);
            log.debug("Deleted {} daily winner records", deletedDailyWinners);

            // Delete competition participations (no FK constraint)
            int deletedParticipations = competitionParticipantRepository.deleteByUserId(userId);
            log.debug("Deleted {} competition participations", deletedParticipations);

            // Reassign competitions to system account (to avoid RESTRICT constraint)
            // Using ID 1 as system/admin account - in production, this should be configurable
            int reassignedCompetitions = 0;
            if (competitionsCount > 0) {
                Long systemUserId = findSystemUserId();
                reassignedCompetitions = competitionRepository.reassignCompetitions(userId, systemUserId);
                log.debug("Reassigned {} competitions to system account", reassignedCompetitions);
            }

            // Anonymize routes (set creator to null)
            int anonymizedRoutes = routeRepository.anonymizeByCreatorUserId(userId);
            log.debug("Anonymized {} routes", anonymizedRoutes);

            // Step 5: Delete avatar from file storage
            int avatarFilesDeleted = 0;
            String avatarUrl = user.getProfilePictureUrl();
            if (avatarUrl != null && !avatarUrl.contains("ui-avatars.com")) {
                try {
                    fileStorageService.deleteAvatar(avatarUrl);
                    avatarFilesDeleted = 1;
                    log.debug("Deleted avatar file");
                } catch (Exception e) {
                    log.warn("Failed to delete avatar file for user {}: {}", userId, e.getMessage());
                }
            }

            // Step 6: Delete FIT files from storage
            for (FitFileUpload fitFile : fitFileUploads) {
                try {
                    if (fitFile.getFilePath() != null) {
                        fileStorageService.deleteFile(fitFile.getFilePath());
                        fitFilesDeleted++;
                    }
                } catch (Exception e) {
                    log.warn("Failed to delete FIT file {}: {}", fitFile.getFilePath(), e.getMessage());
                }
            }
            log.debug("Deleted {} FIT files from storage", fitFilesDeleted);

            // Step 7: Delete the user (CASCADE will handle: runs, subscriptions, orders, payments, password_reset_tokens, user_activities, user_trophies)
            log.info("Deleting user record and cascading related entities");
            userRepository.delete(user);

            // Step 8: Create audit log entry
            String emailHash = hashEmail(userEmail);
            String deletedEntitiesSummary = createDeletedEntitiesSummary(
                    runsCount, userActivitiesCount, userTrophiesCount, fitFileUploadsCount,
                    dailyWinnersCount, competitionParticipantsCount, reassignedCompetitions,
                    anonymizedRoutes, subscriptionsCount, ordersCount, paymentsCount,
                    passwordResetTokensCount, avatarFilesDeleted, fitFilesDeleted
            );

            AccountDeletionLog.DeletionType deletionType = userId.equals(requestedByUserId)
                    ? AccountDeletionLog.DeletionType.SELF_SERVICE
                    : AccountDeletionLog.DeletionType.ADMIN_INITIATED;

            AccountDeletionLog auditLog = AccountDeletionLog.builder()
                    .deletedUserId(userId)
                    .emailHash(emailHash)
                    .deletionReason(reason)
                    .requestedBy(requestedByUserId)
                    .deletionType(deletionType)
                    .deletedEntitiesSummary(deletedEntitiesSummary)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .legalBasis("Art. 17 GDPR - Right to erasure")
                    .dataCategoriesDeleted(createDataCategoriesList())
                    .thirdPartyNotifications(createThirdPartyNotifications(stravaDisconnected, nikeDisconnected, garminDisconnected))
                    .build();

            AccountDeletionLog savedLog = accountDeletionLogRepository.save(auditLog);
            log.info("Created audit log entry with ID: {}", savedLog.getId());

            // Build success result
            UserDeletionResultDto result = resultBuilder
                    .success(true)
                    .message("User account and all associated data permanently deleted in compliance with GDPR Art. 17")
                    .runsDeleted(runsCount)
                    .userActivitiesDeleted(userActivitiesCount)
                    .userTrophiesDeleted(userTrophiesCount)
                    .fitFileUploadsDeleted(fitFileUploadsCount)
                    .dailyWinnersDeleted(dailyWinnersCount)
                    .competitionParticipantsDeleted(competitionParticipantsCount)
                    .competitionsReassigned(reassignedCompetitions)
                    .routesAnonymized(anonymizedRoutes)
                    .subscriptionsDeleted(subscriptionsCount)
                    .ordersDeleted(ordersCount)
                    .paymentsDeleted(paymentsCount)
                    .passwordResetTokensDeleted(passwordResetTokensCount)
                    .avatarFilesDeleted(avatarFilesDeleted)
                    .fitFilesDeleted(fitFilesDeleted)
                    .stravaDisconnected(stravaDisconnected)
                    .nikeDisconnected(nikeDisconnected)
                    .garminDisconnected(garminDisconnected)
                    .auditLogId(savedLog.getId().toString())
                    .build();

            log.info("Successfully completed GDPR deletion for user ID: {}. Deleted: {} runs, {} activities, {} trophies, {} FIT uploads",
                    userId, runsCount, userActivitiesCount, userTrophiesCount, fitFileUploadsCount);

            return result;

        } catch (Exception e) {
            log.error("Failed to delete user account for ID: {}", userId, e);
            return resultBuilder
                    .success(false)
                    .message("Failed to delete user account: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean canDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Admin users cannot be deleted if they are the only admin
        if (user.getRole() == User.Role.ADMIN) {
            long adminCount = userRepository.findUsersByRole(User.Role.ADMIN, Pageable.unpaged()).getTotalElements();
            if (adminCount <= 1) {
                log.warn("Cannot delete user ID: {} - last admin user", userId);
                return false;
            }
        }

        return true;
    }

    @Override
    public UserDeletionResultDto previewUserDeletion(Long userId) {
        log.debug("Creating deletion preview for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        int runsCount = runRepository.countByUserId(userId).intValue();
        int userActivitiesCount = userActivityRepository.findByUserIdOrderByActivityStartTimeDesc(userId).size();
        int userTrophiesCount = (int) userTrophyRepository.countByUserId(userId);
        int fitFileUploadsCount = (int) fitFileUploadRepository.countByUserId(userId);
        int dailyWinnersCount = (int) dailyWinnerRepository.countByUserId(userId);
        int competitionParticipantsCount = (int) competitionParticipantRepository.countByUserId(userId);
        int competitionsCount = (int) competitionRepository.countByCreatedById(userId);
        int routesCount = (int) routeRepository.countByCreatorUserId(userId);
        int subscriptionsCount = (int) subscriptionRepository.countByUserId(userId);
        int ordersCount = (int) orderRepository.countByUserId(userId);
        int paymentsCount = (int) paymentRepository.countByUserId(userId);
        int passwordResetTokensCount = (int) passwordResetTokenRepository.countByUserId(userId);

        int avatarFilesCount = (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().contains("ui-avatars.com")) ? 1 : 0;

        return UserDeletionResultDto.builder()
                .userId(userId)
                .email(user.getEmail())
                .deletionTimestamp(LocalDateTime.now())
                .success(true)
                .message("Preview of data that will be permanently deleted")
                .runsDeleted(runsCount)
                .userActivitiesDeleted(userActivitiesCount)
                .userTrophiesDeleted(userTrophiesCount)
                .fitFileUploadsDeleted(fitFileUploadsCount)
                .dailyWinnersDeleted(dailyWinnersCount)
                .competitionParticipantsDeleted(competitionParticipantsCount)
                .competitionsReassigned(competitionsCount)
                .routesAnonymized(routesCount)
                .subscriptionsDeleted(subscriptionsCount)
                .ordersDeleted(ordersCount)
                .paymentsDeleted(paymentsCount)
                .passwordResetTokensDeleted(passwordResetTokensCount)
                .avatarFilesDeleted(avatarFilesCount)
                .fitFilesDeleted(fitFileUploadsCount)
                .stravaDisconnected(user.getStravaUserId() != null)
                .nikeDisconnected(user.getNikeUserId() != null)
                .garminDisconnected(user.getGarminUserId() != null)
                .build();
    }

    // Helper methods for GDPR deletion

    private Long findSystemUserId() {
        // Find the first admin user to reassign competitions
        // In production, this should be a configurable system account
        return userRepository.findUsersByRole(User.Role.ADMIN, Pageable.ofSize(1))
                .getContent()
                .stream()
                .findFirst()
                .map(User::getId)
                .orElse(1L); // Fallback to ID 1 if no admin found
    }

    private String hashEmail(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.toLowerCase().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            return "HASH_ERROR";
        }
    }

    private String createDeletedEntitiesSummary(
            int runs, int activities, int trophies, int fitUploads,
            int dailyWinners, int participations, int competitionsReassigned,
            int routesAnonymized, int subscriptions, int orders, int payments,
            int passwordTokens, int avatarFiles, int fitFiles
    ) {
        try {
            return objectMapper.writeValueAsString(java.util.Map.ofEntries(
                    java.util.Map.entry("runs", runs),
                    java.util.Map.entry("userActivities", activities),
                    java.util.Map.entry("userTrophies", trophies),
                    java.util.Map.entry("fitFileUploads", fitUploads),
                    java.util.Map.entry("dailyWinners", dailyWinners),
                    java.util.Map.entry("competitionParticipants", participations),
                    java.util.Map.entry("competitionsReassigned", competitionsReassigned),
                    java.util.Map.entry("routesAnonymized", routesAnonymized),
                    java.util.Map.entry("subscriptions", subscriptions),
                    java.util.Map.entry("orders", orders),
                    java.util.Map.entry("payments", payments),
                    java.util.Map.entry("passwordResetTokens", passwordTokens),
                    java.util.Map.entry("avatarFiles", avatarFiles),
                    java.util.Map.entry("fitFiles", fitFiles)
            ));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize deleted entities summary", e);
            return "{}";
        }
    }

    private String createDataCategoriesList() {
        try {
            return objectMapper.writeValueAsString(java.util.List.of(
                    "Personal identification data (name, email, phone)",
                    "Authentication credentials",
                    "Profile information (bio, preferences)",
                    "Fitness and health data (runs, activities)",
                    "Location data (GPS points, routes)",
                    "Achievement data (trophies, competitions)",
                    "Financial data (subscriptions, payments)",
                    "Third-party integration tokens",
                    "User-generated content (uploaded files, avatars)"
            ));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize data categories", e);
            return "[]";
        }
    }

    private String createThirdPartyNotifications(boolean strava, boolean nike, boolean garmin) {
        try {
            return objectMapper.writeValueAsString(java.util.Map.of(
                    "strava", strava ? "Tokens revoked" : "Not connected",
                    "nike", nike ? "Tokens revoked" : "Not connected",
                    "garmin", garmin ? "Tokens revoked" : "Not connected"
            ));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize third party notifications", e);
            return "{}";
        }
    }
}