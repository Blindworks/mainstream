package com.mainstream.activity.service;

import com.mainstream.activity.dto.CreateTrophyRequest;
import com.mainstream.activity.dto.UpdateTrophyRequest;
import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.entity.UserTrophy;
import com.mainstream.activity.repository.TrophyRepository;
import com.mainstream.activity.repository.UserActivityRepository;
import com.mainstream.activity.repository.UserTrophyRepository;
import com.mainstream.activity.service.trophy.TrophyChecker;
import com.mainstream.activity.service.trophy.TrophyProgress;
import com.mainstream.fitfile.entity.FitTrackPoint;
import com.mainstream.fitfile.repository.FitTrackPointRepository;
import com.mainstream.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service for managing trophies and checking trophy achievements.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrophyService {

    private final TrophyRepository trophyRepository;
    private final UserTrophyRepository userTrophyRepository;
    private final UserActivityRepository userActivityRepository;
    private final FitTrackPointRepository fitTrackPointRepository;
    private final ObjectMapper objectMapper;

    // Auto-inject all TrophyChecker implementations
    private final List<TrophyChecker> trophyCheckers;

    /**
     * Check and award trophies for a user based on their latest activity.
     * Uses pluggable TrophyChecker implementations for flexible, configurable trophy logic.
     *
     * @param user The user
     * @param activity The latest activity
     * @return List of newly awarded trophies
     */
    @Transactional
    public List<Trophy> checkAndAwardTrophies(User user, UserActivity activity) {
        List<Trophy> newTrophies = new ArrayList<>();

        log.info("=== Starting Trophy Check for User {} and Activity {} ===", user.getId(), activity.getId());

        // Get all active trophies
        List<Trophy> allTrophies = trophyRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        log.info("Found {} active trophies to check", allTrophies.size());

        for (Trophy trophy : allTrophies) {
            log.debug("Checking trophy: {} (type: {}, ID: {})", trophy.getCode(), trophy.getType(), trophy.getId());

            // Skip if user already has this trophy
            if (userTrophyRepository.existsByUserIdAndTrophyId(user.getId(), trophy.getId())) {
                log.debug("User already has trophy {}, skipping", trophy.getCode());
                continue;
            }

            // Skip if trophy has no criteriaConfig (legacy trophies without config)
            // Exception: LOCATION_BASED trophies can use entity fields instead of criteriaConfig
            if ((trophy.getCriteriaConfig() == null || trophy.getCriteriaConfig().trim().isEmpty())
                && trophy.getType() != Trophy.TrophyType.LOCATION_BASED) {
                log.debug("Trophy {} has no criteriaConfig, skipping", trophy.getCode());
                continue;
            }

            // LOCATION_BASED special handling
            if (trophy.getType() == Trophy.TrophyType.LOCATION_BASED) {
                log.info("Processing LOCATION_BASED trophy: {} (criteriaConfig: {})",
                    trophy.getCode(), trophy.getCriteriaConfig() != null ? "present" : "null");
            }

            // Find appropriate checker for this trophy type
            TrophyChecker checker = findChecker(trophy.getType());
            if (checker == null) {
                log.warn("No checker found for trophy type: {} (trophy: {})", trophy.getType(), trophy.getCode());
                continue;
            }

            // Check if criteria is met
            try {
                log.debug("Running checker for trophy {}", trophy.getCode());
                if (checker.checkCriteria(user, activity, trophy)) {
                    // Calculate progress before awarding
                    TrophyProgress progress = checker.calculateProgress(user, trophy);
                    awardTrophy(user, trophy, activity, progress);
                    newTrophies.add(trophy);
                    log.info("Awarded trophy '{}' (type: {}) to user {} with progress {}/{}",
                        trophy.getName(), trophy.getType(), user.getId(),
                        progress.getCurrentValue(), progress.getTargetValue());
                }
            } catch (Exception e) {
                log.error("Error checking trophy {} for user {}: {}",
                    trophy.getCode(), user.getId(), e.getMessage(), e);
            }
        }

        return newTrophies;
    }

    /**
     * Find the appropriate checker for a given trophy type.
     */
    private TrophyChecker findChecker(Trophy.TrophyType type) {
        return trophyCheckers.stream()
            .filter(checker -> checker.supports(type))
            .findFirst()
            .orElse(null);
    }

    /**
     * Check distance milestone trophies.
     * @deprecated Use generic TrophyChecker system instead
     */
    @Deprecated
    private List<Trophy> checkDistanceMilestones(User user, UserActivity activity) {
        List<Trophy> newTrophies = new ArrayList<>();

        // Get total distance for user
        Long totalDistanceMeters = userActivityRepository.getTotalDistanceForUser(user.getId());
        if (totalDistanceMeters == null) {
            totalDistanceMeters = 0L;
        }

        double totalDistanceKm = totalDistanceMeters / 1000.0;

        // Distance milestones: 1km, 5km, 10km, 21km (half marathon), 42km (marathon)
        int[] milestones = {1, 5, 10, 21, 42};

        for (int milestone : milestones) {
            if (totalDistanceKm >= milestone) {
                String trophyCode = "DISTANCE_" + milestone + "KM";
                Trophy trophy = trophyRepository.findByCode(trophyCode).orElse(null);

                if (trophy != null && !userTrophyRepository.existsByUserIdAndTrophyId(user.getId(), trophy.getId())) {
                    awardTrophy(user, trophy, activity);
                    newTrophies.add(trophy);
                    log.info("Awarded trophy '{}' to user {}", trophy.getName(), user.getId());
                }
            }
        }

        return newTrophies;
    }

    /**
     * Check streak milestone trophies.
     * @deprecated Use generic TrophyChecker system instead
     */
    @Deprecated
    private List<Trophy> checkStreakMilestones(User user) {
        List<Trophy> newTrophies = new ArrayList<>();

        int currentStreak = calculateCurrentStreak(user);

        // Streak milestones: 7 days
        int[] streakMilestones = {7, 30, 100};

        for (int milestone : streakMilestones) {
            if (currentStreak >= milestone) {
                String trophyCode = "STREAK_" + milestone + "_DAYS";
                Trophy trophy = trophyRepository.findByCode(trophyCode).orElse(null);

                if (trophy != null && !userTrophyRepository.existsByUserIdAndTrophyId(user.getId(), trophy.getId())) {
                    awardTrophy(user, trophy, null);
                    newTrophies.add(trophy);
                    log.info("Awarded trophy '{}' to user {} for {} day streak", trophy.getName(), user.getId(), milestone);
                }
            }
        }

        return newTrophies;
    }

    /**
     * Calculate current consecutive day streak for a user.
     * @deprecated Use StreakChecker instead
     */
    @Deprecated
    private int calculateCurrentStreak(User user) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<UserActivity> recentActivities = userActivityRepository.findUserActivitiesSince(user.getId(), thirtyDaysAgo);

        if (recentActivities.isEmpty()) {
            return 0;
        }

        // Sort by date and count consecutive days
        Set<String> activityDates = new HashSet<>();
        for (UserActivity activity : recentActivities) {
            activityDates.add(activity.getActivityStartTime().toLocalDate().toString());
        }

        int streak = 0;
        LocalDateTime date = LocalDateTime.now();

        // Check backwards from today
        for (int i = 0; i < 30; i++) {
            String dateStr = date.toLocalDate().toString();
            if (activityDates.contains(dateStr)) {
                streak++;
            } else if (i > 0) {
                // Break on first missing day (after checking today)
                break;
            }
            date = date.minusDays(1);
        }

        return streak;
    }

    /**
     * Check location-based trophies.
     * @deprecated Use generic TrophyChecker system instead
     */
    @Deprecated
    private List<Trophy> checkLocationBasedTrophies(User user, UserActivity activity) {
        List<Trophy> newTrophies = new ArrayList<>();

        // Only check if activity has a FIT file with GPS data
        if (activity.getFitFileUpload() == null) {
            return newTrophies;
        }

        // Get all active location-based trophies
        List<Trophy> locationTrophies = trophyRepository.findActiveLocationBasedTrophies(LocalDateTime.now());

        if (locationTrophies.isEmpty()) {
            return newTrophies;
        }

        // Get GPS track points for the activity
        List<FitTrackPoint> trackPoints = fitTrackPointRepository.findByFitFileUploadIdWithGpsData(
                activity.getFitFileUpload().getId()
        );

        if (trackPoints.isEmpty()) {
            log.debug("No GPS track points found for activity {}", activity.getId());
            return newTrophies;
        }

        // Check each location trophy
        for (Trophy trophy : locationTrophies) {
            // Skip if user already has this trophy
            if (userTrophyRepository.existsByUserIdAndTrophyId(user.getId(), trophy.getId())) {
                continue;
            }

            // Check if trophy is still valid (within validFrom and validUntil)
            LocalDateTime now = activity.getActivityStartTime();
            if (trophy.getValidFrom() != null && now.isBefore(trophy.getValidFrom())) {
                continue;
            }
            if (trophy.getValidUntil() != null && now.isAfter(trophy.getValidUntil())) {
                continue;
            }

            // Check if any track point is within collection radius
            boolean collected = false;
            for (FitTrackPoint trackPoint : trackPoints) {
                if (trackPoint.hasValidGpsPosition()) {
                    double distance = calculateDistance(
                            trophy.getLatitude(),
                            trophy.getLongitude(),
                            trackPoint.getPositionLat().doubleValue(),
                            trackPoint.getPositionLong().doubleValue()
                    );

                    // Check if within collection radius
                    if (distance <= trophy.getCollectionRadiusMeters()) {
                        collected = true;
                        break;
                    }
                }
            }

            if (collected) {
                awardTrophy(user, trophy, activity);
                newTrophies.add(trophy);
                log.info("Awarded location-based trophy '{}' to user {} at activity {}",
                        trophy.getName(), user.getId(), activity.getId());
            }
        }

        return newTrophies;
    }

    /**
     * Calculate distance between two GPS coordinates using Haversine formula.
     *
     * @param lat1 Latitude of point 1 (degrees)
     * @param lon1 Longitude of point 1 (degrees)
     * @param lat2 Latitude of point 2 (degrees)
     * @param lon2 Longitude of point 2 (degrees)
     * @return Distance in meters
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // meters

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * Award a trophy to a user.
     */
    private void awardTrophy(User user, Trophy trophy, UserActivity activity, TrophyProgress progress) {
        UserTrophy userTrophy = new UserTrophy();
        userTrophy.setUser(user);
        userTrophy.setTrophy(trophy);
        userTrophy.setActivity(activity);

        // Store progress in metadata
        if (progress != null) {
            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("currentValue", progress.getCurrentValue());
                metadata.put("targetValue", progress.getTargetValue());
                metadata.put("percentage", progress.getPercentage());
                metadata.put("isComplete", progress.isComplete());

                String metadataJson = objectMapper.writeValueAsString(metadata);
                userTrophy.setMetadata(metadataJson);
            } catch (Exception e) {
                log.error("Error serializing trophy progress to metadata: {}", e.getMessage(), e);
            }
        }

        userTrophyRepository.save(userTrophy);
    }

    /**
     * Award a trophy to a user (legacy method without progress tracking).
     * @deprecated Use awardTrophy(User, Trophy, UserActivity, TrophyProgress) instead
     */
    @Deprecated
    private void awardTrophy(User user, Trophy trophy, UserActivity activity) {
        awardTrophy(user, trophy, activity, null);
    }

    /**
     * Get all trophies for a user.
     */
    public List<UserTrophy> getUserTrophies(Long userId) {
        return userTrophyRepository.findByUserIdWithUserOrderByEarnedAtDesc(userId);
    }

    /**
     * Calculate progress for all active trophies for a user.
     * Returns progress for both earned and unearned trophies.
     *
     * @param userId The user ID
     * @return Map of trophy ID to TrophyProgress
     */
    public Map<Long, TrophyProgress> calculateAllTrophyProgress(Long userId) {
        Map<Long, TrophyProgress> progressMap = new HashMap<>();

        // Get user
        User user = new User();
        user.setId(userId);

        // Get all active trophies
        List<Trophy> allTrophies = trophyRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        for (Trophy trophy : allTrophies) {
            // Skip if trophy has no criteriaConfig
            // Exception: LOCATION_BASED trophies can use entity fields instead of criteriaConfig
            if ((trophy.getCriteriaConfig() == null || trophy.getCriteriaConfig().trim().isEmpty())
                && trophy.getType() != Trophy.TrophyType.LOCATION_BASED) {
                continue;
            }

            // Find appropriate checker for this trophy type
            TrophyChecker checker = findChecker(trophy.getType());
            if (checker == null) {
                continue;
            }

            // Calculate progress
            try {
                TrophyProgress progress = checker.calculateProgress(user, trophy);
                progressMap.put(trophy.getId(), progress);
            } catch (Exception e) {
                log.error("Error calculating progress for trophy {} and user {}: {}",
                    trophy.getCode(), userId, e.getMessage(), e);
            }
        }

        return progressMap;
    }

    /**
     * Get trophies earned for a specific activity.
     */
    public List<UserTrophy> getTrophiesForActivity(Long activityId, Long userId) {
        log.debug("Fetching trophies for activity {} and user {}", activityId, userId);
        return userTrophyRepository.findByActivityIdAndUserIdWithUserOrderByEarnedAtDesc(activityId, userId);
    }

    /**
     * Get all available trophies.
     */
    public List<Trophy> getAllTrophies() {
        return trophyRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Initialize default trophies in the database.
     */
    @Transactional
    public void initializeDefaultTrophies() {
        log.info("Initializing default trophies...");

        // Distance milestones with criteriaConfig
        createTrophyIfNotExists("DISTANCE_1KM", "First Kilometer", "Complete your first 1km",
                                Trophy.TrophyType.DISTANCE_MILESTONE, Trophy.TrophyCategory.BEGINNER,
                                "{\"distanceMeters\": 1000, \"scope\": \"TOTAL\"}", 1);
        createTrophyIfNotExists("DISTANCE_5KM", "5K Milestone", "Complete 5km total distance",
                                Trophy.TrophyType.DISTANCE_MILESTONE, Trophy.TrophyCategory.BEGINNER,
                                "{\"distanceMeters\": 5000, \"scope\": \"TOTAL\"}", 2);
        createTrophyIfNotExists("DISTANCE_10KM", "10K Milestone", "Complete 10km total distance",
                                Trophy.TrophyType.DISTANCE_MILESTONE, Trophy.TrophyCategory.INTERMEDIATE,
                                "{\"distanceMeters\": 10000, \"scope\": \"TOTAL\"}", 3);
        createTrophyIfNotExists("DISTANCE_21KM", "Half Marathon", "Complete 21km total distance",
                                Trophy.TrophyType.DISTANCE_MILESTONE, Trophy.TrophyCategory.ADVANCED,
                                "{\"distanceMeters\": 21000, \"scope\": \"TOTAL\"}", 4);
        createTrophyIfNotExists("DISTANCE_42KM", "Marathon", "Complete 42km total distance",
                                Trophy.TrophyType.DISTANCE_MILESTONE, Trophy.TrophyCategory.ELITE,
                                "{\"distanceMeters\": 42000, \"scope\": \"TOTAL\"}", 5);
        createTrophyIfNotExists("DISTANCE_100KM", "Ultra Distance", "Complete 100km total distance",
                                Trophy.TrophyType.DISTANCE_MILESTONE, Trophy.TrophyCategory.ELITE,
                                "{\"distanceMeters\": 100000, \"scope\": \"TOTAL\"}", 6);

        // Streak milestones with criteriaConfig
        createTrophyIfNotExists("STREAK_7_DAYS", "Week Warrior", "Run for 7 consecutive days",
                                Trophy.TrophyType.STREAK, Trophy.TrophyCategory.INTERMEDIATE,
                                "{\"consecutiveDays\": 7, \"minimumDistancePerDay\": 1000}", 10);
        createTrophyIfNotExists("STREAK_30_DAYS", "Monthly Master", "Run for 30 consecutive days",
                                Trophy.TrophyType.STREAK, Trophy.TrophyCategory.ADVANCED,
                                "{\"consecutiveDays\": 30, \"minimumDistancePerDay\": 1000}", 11);
        createTrophyIfNotExists("STREAK_100_DAYS", "Century Champion", "Run for 100 consecutive days",
                                Trophy.TrophyType.STREAK, Trophy.TrophyCategory.ELITE,
                                "{\"consecutiveDays\": 100, \"minimumDistancePerDay\": 1000}", 12);

        log.info("Default trophies initialized");

        // Update existing trophies that don't have criteriaConfig
        updateExistingTrophiesWithCriteriaConfig();
    }

    /**
     * Update existing trophies that don't have criteriaConfig set.
     * This migrates old trophies to the new config-based system.
     */
    @Transactional
    public void updateExistingTrophiesWithCriteriaConfig() {
        log.info("Updating existing trophies with criteriaConfig...");

        List<Trophy> allTrophies = trophyRepository.findAll();
        int updatedCount = 0;

        for (Trophy trophy : allTrophies) {
            // Skip trophies that already have criteriaConfig
            if (trophy.getCriteriaConfig() != null && !trophy.getCriteriaConfig().trim().isEmpty()) {
                continue;
            }

            // Generate criteriaConfig based on trophy code and type
            String criteriaConfig = null;

            if (trophy.getType() == Trophy.TrophyType.DISTANCE_MILESTONE && trophy.getCriteriaValue() != null) {
                criteriaConfig = String.format("{\"distanceMeters\": %d, \"scope\": \"TOTAL\"}",
                    trophy.getCriteriaValue());
            } else if (trophy.getType() == Trophy.TrophyType.STREAK && trophy.getCriteriaValue() != null) {
                criteriaConfig = String.format("{\"consecutiveDays\": %d, \"minimumDistancePerDay\": 1000}",
                    trophy.getCriteriaValue());
            }

            if (criteriaConfig != null) {
                trophy.setCriteriaConfig(criteriaConfig);
                trophyRepository.save(trophy);
                updatedCount++;
                log.info("Updated trophy {} with criteriaConfig: {}", trophy.getCode(), criteriaConfig);
            }
        }

        log.info("Updated {} trophies with criteriaConfig", updatedCount);
    }

    private void createTrophyIfNotExists(String code, String name, String description,
                                         Trophy.TrophyType type, Trophy.TrophyCategory category,
                                         String criteriaConfig, Integer displayOrder) {
        if (!trophyRepository.findByCode(code).isPresent()) {
            Trophy trophy = new Trophy();
            trophy.setCode(code);
            trophy.setName(name);
            trophy.setDescription(description);
            trophy.setType(type);
            trophy.setCategory(category);
            trophy.setCriteriaConfig(criteriaConfig);
            trophy.setDisplayOrder(displayOrder);
            trophy.setIsActive(true);
            trophyRepository.save(trophy);
            log.info("Created trophy: {}", code);
        }
    }

    /**
     * Get all trophies (including inactive) for admin.
     */
    public List<Trophy> getAllTrophiesForAdmin() {
        return trophyRepository.findAll();
    }

    /**
     * Get trophy by ID.
     */
    public Optional<Trophy> getTrophyById(Long id) {
        return trophyRepository.findById(id);
    }

    /**
     * Create a new trophy.
     */
    @Transactional
    public Trophy createTrophy(CreateTrophyRequest request) {
        // Check if trophy with same code already exists
        if (trophyRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Trophy with code '" + request.getCode() + "' already exists");
        }

        Trophy trophy = new Trophy();
        trophy.setCode(request.getCode());
        trophy.setName(request.getName());
        trophy.setDescription(request.getDescription());
        trophy.setType(request.getType());
        trophy.setCategory(request.getCategory());
        trophy.setIconUrl(request.getIconUrl());
        trophy.setCriteriaValue(request.getCriteriaValue());
        trophy.setIsActive(request.getIsActive());
        trophy.setDisplayOrder(request.getDisplayOrder());

        // Set location-based fields
        trophy.setLatitude(request.getLatitude());
        trophy.setLongitude(request.getLongitude());
        trophy.setCollectionRadiusMeters(request.getCollectionRadiusMeters());
        trophy.setValidFrom(request.getValidFrom());
        trophy.setValidUntil(request.getValidUntil());
        trophy.setImageUrl(request.getImageUrl());

        // Set configurable criteria fields
        trophy.setCriteriaConfig(request.getCriteriaConfig());
        trophy.setCheckScope(request.getCheckScope());

        Trophy savedTrophy = trophyRepository.save(trophy);
        log.info("Created trophy: {} (ID: {})", savedTrophy.getCode(), savedTrophy.getId());

        return savedTrophy;
    }

    /**
     * Update an existing trophy.
     */
    @Transactional
    public Trophy updateTrophy(Long id, UpdateTrophyRequest request) {
        Trophy trophy = trophyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trophy not found with id: " + id));

        // Update only provided fields
        if (request.getName() != null) {
            trophy.setName(request.getName());
        }
        if (request.getDescription() != null) {
            trophy.setDescription(request.getDescription());
        }
        if (request.getIconUrl() != null) {
            trophy.setIconUrl(request.getIconUrl());
        }
        if (request.getCriteriaValue() != null) {
            trophy.setCriteriaValue(request.getCriteriaValue());
        }
        if (request.getIsActive() != null) {
            trophy.setIsActive(request.getIsActive());
        }
        if (request.getDisplayOrder() != null) {
            trophy.setDisplayOrder(request.getDisplayOrder());
        }

        // Update location-based fields
        if (request.getLatitude() != null) {
            trophy.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            trophy.setLongitude(request.getLongitude());
        }
        if (request.getCollectionRadiusMeters() != null) {
            trophy.setCollectionRadiusMeters(request.getCollectionRadiusMeters());
        }
        if (request.getValidFrom() != null) {
            trophy.setValidFrom(request.getValidFrom());
        }
        if (request.getValidUntil() != null) {
            trophy.setValidUntil(request.getValidUntil());
        }
        if (request.getImageUrl() != null) {
            trophy.setImageUrl(request.getImageUrl());
        }

        // Update configurable criteria fields
        if (request.getCriteriaConfig() != null) {
            trophy.setCriteriaConfig(request.getCriteriaConfig());
        }
        if (request.getCheckScope() != null) {
            trophy.setCheckScope(request.getCheckScope());
        }

        Trophy updatedTrophy = trophyRepository.save(trophy);
        log.info("Updated trophy: {} (ID: {})", updatedTrophy.getCode(), updatedTrophy.getId());

        return updatedTrophy;
    }

    /**
     * Delete a trophy.
     */
    @Transactional
    public void deleteTrophy(Long id) {
        Trophy trophy = trophyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trophy not found with id: " + id));

        // Check if any users have earned this trophy
        long count = userTrophyRepository.countByTrophyId(id);
        if (count > 0) {
            throw new IllegalStateException("Cannot delete trophy that has been awarded to " + count + " users");
        }

        trophyRepository.delete(trophy);
        log.info("Deleted trophy: {} (ID: {})", trophy.getCode(), id);
    }

    /**
     * Activate a trophy.
     */
    @Transactional
    public Trophy activateTrophy(Long id) {
        Trophy trophy = trophyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trophy not found with id: " + id));

        trophy.setIsActive(true);
        Trophy updatedTrophy = trophyRepository.save(trophy);
        log.info("Activated trophy: {} (ID: {})", updatedTrophy.getCode(), updatedTrophy.getId());

        return updatedTrophy;
    }

    /**
     * Deactivate a trophy.
     */
    @Transactional
    public Trophy deactivateTrophy(Long id) {
        Trophy trophy = trophyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trophy not found with id: " + id));

        trophy.setIsActive(false);
        Trophy updatedTrophy = trophyRepository.save(trophy);
        log.info("Deactivated trophy: {} (ID: {})", updatedTrophy.getCode(), updatedTrophy.getId());

        return updatedTrophy;
    }

    /**
     * Get today's trophy - a trophy that is only available on this specific day.
     * Finds a trophy where today's date falls between validFrom and validUntil.
     *
     * @return Optional containing today's trophy if one exists
     */
    public Optional<Trophy> getTodaysTrophy() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

        log.info("Searching for today's trophy (date: {})", now.toLocalDate());

        List<Trophy> allTrophies = trophyRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        return allTrophies.stream()
                .filter(trophy -> {
                    if (trophy.getValidFrom() == null || trophy.getValidUntil() == null) {
                        return false;
                    }

                    // Check if today is within the valid range
                    boolean isValid = !now.isBefore(trophy.getValidFrom()) && !now.isAfter(trophy.getValidUntil());

                    if (isValid) {
                        log.info("Found today's trophy: {} (valid from {} to {})",
                                trophy.getCode(), trophy.getValidFrom(), trophy.getValidUntil());
                    }

                    return isValid;
                })
                .findFirst();
    }

    /**
     * Get users who have earned a specific trophy today.
     * Returns a list of UserTrophy records where earnedAt is today.
     *
     * @param trophyId The ID of the trophy
     * @return List of UserTrophy records earned today
     */
    public List<UserTrophy> getTodaysTrophyWinners(Long trophyId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);

        log.info("Fetching winners of trophy {} for today", trophyId);

        return userTrophyRepository.findByTrophyIdAndEarnedAtBetween(trophyId, startOfDay, endOfDay);
    }

    /**
     * Get all trophies earned this week (Monday to current time).
     * Returns a list of UserTrophy records where earnedAt is within the current week.
     *
     * @return List of UserTrophy records earned this week
     */
    public List<UserTrophy> getWeeklyTrophies() {
        LocalDateTime now = LocalDateTime.now();
        // Get the start of the current week (Monday at 00:00)
        LocalDateTime startOfWeek = now.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        log.info("Fetching weekly trophies from {} to {}", startOfWeek, now);

        return userTrophyRepository.findByEarnedAtBetweenOrderByEarnedAtDesc(startOfWeek, now);
    }
}
