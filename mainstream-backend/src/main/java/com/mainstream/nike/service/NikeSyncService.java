package com.mainstream.nike.service;

import com.mainstream.nike.dto.NikeActivity;
import com.mainstream.nike.dto.NikeUserProfile;
import com.mainstream.run.entity.Run;
import com.mainstream.run.repository.RunRepository;
import com.mainstream.user.entity.User;
import com.mainstream.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NikeSyncService {

    private final NikeApiService nikeApiService;
    private final UserRepository userRepository;
    private final RunRepository runRepository;

    /**
     * Connects a user to Nike using a manually provided access token
     */
    @Transactional
    public User connectNike(Long userId, String accessToken) {
        log.info("Connecting Nike for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            // Validate token and get user profile
            NikeUserProfile profile = nikeApiService.getUserProfile(accessToken);

            // Update user with Nike token
            user.setNikeUserId(profile.getId());
            user.setNikeAccessToken(accessToken);
            user.setNikeConnectedAt(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            log.info("Successfully connected Nike for user ID: {}, Nike user ID: {}",
                    userId, profile.getId());

            return savedUser;
        } catch (Exception e) {
            log.error("Failed to connect Nike for user ID {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to connect Nike account: " + e.getMessage(), e);
        }
    }

    /**
     * Disconnects Nike from a user account
     */
    @Transactional
    public void disconnectNike(Long userId) {
        log.info("Disconnecting Nike for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setNikeUserId(null);
        user.setNikeAccessToken(null);
        user.setNikeConnectedAt(null);

        userRepository.save(user);
        log.info("Successfully disconnected Nike for user ID: {}", userId);
    }

    /**
     * Synchronizes activities from Nike
     */
    @Transactional
    public List<Run> syncActivities(Long userId) {
        log.info("Syncing Nike activities for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isNikeConnected()) {
            throw new RuntimeException("User is not connected to Nike");
        }

        try {
            // Fetch all activities from Nike
            List<NikeActivity> activities = nikeApiService.getAllActivities(user.getNikeAccessToken());

            List<Run> syncedRuns = new ArrayList<>();

            for (NikeActivity activity : activities) {
                // Only sync running activities
                if (!"run".equalsIgnoreCase(activity.getType())) {
                    log.debug("Skipping non-running activity: {} (type: {})", activity.getId(), activity.getType());
                    continue;
                }

                // Check if activity already exists
                if (runRepository.findByNikeActivityIdAndUserId(activity.getId(), userId).isPresent()) {
                    log.debug("Activity {} already exists, skipping", activity.getId());
                    continue;
                }

                // Convert Nike activity to Run
                Run run = convertNikeActivityToRun(activity, userId);
                Run savedRun = runRepository.save(run);
                syncedRuns.add(savedRun);

                log.info("Synced activity: {} (Nike ID: {})",
                        run.getTitle(), activity.getId());
            }

            log.info("Successfully synced {} new runs for user ID: {}", syncedRuns.size(), userId);
            return syncedRuns;
        } catch (Exception e) {
            log.error("Failed to sync Nike activities for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to sync Nike activities: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a Nike activity to a Run entity
     */
    private Run convertNikeActivityToRun(NikeActivity activity, Long userId) {
        // Extract start and end time
        LocalDateTime startTime = activity.getStartEpochMs() != null ?
                LocalDateTime.ofInstant(Instant.ofEpochMilli(activity.getStartEpochMs()), ZoneId.systemDefault()) :
                LocalDateTime.now();

        LocalDateTime endTime = activity.getEndEpochMs() != null ?
                LocalDateTime.ofInstant(Instant.ofEpochMilli(activity.getEndEpochMs()), ZoneId.systemDefault()) :
                startTime;

        // Duration in seconds
        Integer durationSeconds = activity.getActiveDurationMs() != null ?
                (int) (activity.getActiveDurationMs() / 1000) : 0;

        // Extract metrics from summaries
        Double distanceMeters = null;
        Integer calories = null;
        Double elevationGainMeters = null;

        if (activity.getSummaries() != null) {
            for (NikeActivity.NikeSummary summary : activity.getSummaries()) {
                if ("distance".equals(summary.getMetric()) && summary.getValue() != null) {
                    // Nike distance is in KM, convert to meters
                    distanceMeters = summary.getValue() * 1000.0;
                } else if ("calories".equals(summary.getMetric()) && summary.getValue() != null) {
                    calories = summary.getValue().intValue();
                } else if ("ascent".equals(summary.getMetric()) && summary.getValue() != null) {
                    elevationGainMeters = summary.getValue();
                }
            }
        }

        // Calculate average pace in seconds per km and speeds
        Double averagePaceSecondsPerKm = null;
        BigDecimal averageSpeedKmh = null;
        BigDecimal maxSpeedKmh = null;

        if (distanceMeters != null && distanceMeters > 0 && durationSeconds > 0) {
            double distanceKm = distanceMeters / 1000.0;
            averagePaceSecondsPerKm = durationSeconds / distanceKm;

            // Calculate average speed (km/h)
            double durationHours = durationSeconds / 3600.0;
            averageSpeedKmh = BigDecimal.valueOf(distanceKm / durationHours);
        }

        // Extract pace metrics for max speed calculation
        if (activity.getMetrics() != null) {
            for (NikeActivity.NikeMetric metric : activity.getMetrics()) {
                if ("pace".equals(metric.getType()) && metric.getValues() != null && !metric.getValues().isEmpty()) {
                    // Find minimum pace (fastest) and convert to max speed
                    Double minPace = metric.getValues().stream()
                            .filter(v -> v != null && v > 0)
                            .min(Double::compare)
                            .orElse(null);

                    if (minPace != null) {
                        // Pace is in min/km, convert to km/h
                        maxSpeedKmh = BigDecimal.valueOf(60.0 / minPace);
                    }
                }
            }
        }

        // Extract tags for weather and temperature
        String weatherCondition = null;
        Integer temperatureCelsius = null;

        if (activity.getTags() != null) {
            weatherCondition = activity.getTags().getWeather();
            if (activity.getTags().getTemperature() != null) {
                try {
                    temperatureCelsius = Integer.parseInt(activity.getTags().getTemperature());
                } catch (NumberFormatException e) {
                    log.debug("Failed to parse temperature: {}", activity.getTags().getTemperature());
                }
            }
        }

        // Get activity name
        String title = "Nike Run";
        if (activity.getTags() != null && activity.getTags().getName() != null) {
            title = activity.getTags().getName();
        }

        return Run.builder()
                .userId(userId)
                .title(title)
                .startTime(startTime)
                .endTime(endTime)
                .durationSeconds(durationSeconds)
                .distanceMeters(distanceMeters != null ? BigDecimal.valueOf(distanceMeters) : null)
                .averagePaceSecondsPerKm(averagePaceSecondsPerKm)
                .averageSpeedKmh(averageSpeedKmh)
                .maxSpeedKmh(maxSpeedKmh)
                .caloriesBurned(calories)
                .elevationGainMeters(elevationGainMeters != null ? BigDecimal.valueOf(elevationGainMeters) : null)
                .weatherCondition(weatherCondition)
                .temperatureCelsius(temperatureCelsius)
                .runType(Run.RunType.OUTDOOR)
                .status(Run.RunStatus.COMPLETED)
                .isPublic(true)
                .nikeActivityId(activity.getId())
                .build();
    }
}
