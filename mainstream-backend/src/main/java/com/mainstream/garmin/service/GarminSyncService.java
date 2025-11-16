package com.mainstream.garmin.service;

import com.mainstream.activity.service.UserActivityService;
import com.mainstream.garmin.dto.GarminActivity;
import com.mainstream.garmin.dto.GarminActivityDetails;
import com.mainstream.garmin.dto.GarminTokenResponse;
import com.mainstream.run.entity.GpsPoint;
import com.mainstream.run.entity.Run;
import com.mainstream.run.repository.GpsPointRepository;
import com.mainstream.run.repository.RunRepository;
import com.mainstream.user.entity.User;
import com.mainstream.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GarminSyncService {

    private final GarminApiService garminApiService;
    private final UserRepository userRepository;
    private final RunRepository runRepository;
    private final GpsPointRepository gpsPointRepository;
    private final UserActivityService userActivityService;

    private static final DateTimeFormatter GARMIN_DATE_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Connects a user to Garmin using the authorization code
     */
    @Transactional
    public User connectGarmin(Long userId, String authorizationCode) {
        log.info("Connecting Garmin for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Exchange authorization code for tokens
        GarminTokenResponse tokenResponse = garminApiService.exchangeToken(authorizationCode);

        // Calculate token expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn());

        // Update user with Garmin tokens
        user.setGarminUserId(tokenResponse.getUserId());
        user.setGarminAccessToken(tokenResponse.getAccessToken());
        user.setGarminRefreshToken(tokenResponse.getRefreshToken());
        user.setGarminTokenExpiresAt(expiresAt);
        user.setGarminConnectedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("Successfully connected Garmin for user ID: {}, Garmin user ID: {}",
                userId, tokenResponse.getUserId());

        return savedUser;
    }

    /**
     * Disconnects Garmin from a user account
     */
    @Transactional
    public void disconnectGarmin(Long userId) {
        log.info("Disconnecting Garmin for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setGarminUserId(null);
        user.setGarminAccessToken(null);
        user.setGarminRefreshToken(null);
        user.setGarminTokenExpiresAt(null);
        user.setGarminConnectedAt(null);

        userRepository.save(user);
        log.info("Successfully disconnected Garmin for user ID: {}", userId);
    }

    /**
     * Synchronizes activities from Garmin
     */
    @Transactional
    public List<Run> syncActivities(Long userId, LocalDateTime since) {
        log.info("Syncing Garmin activities for user ID: {} since: {}", userId, since);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isGarminConnected()) {
            throw new RuntimeException("User is not connected to Garmin");
        }

        // Check if token needs refresh
        String accessToken = getValidAccessToken(user);

        // Fetch activities from Garmin
        List<GarminActivity> activities = garminApiService.getActivities(
                accessToken,
                since,
                100 // fetch up to 100 activities
        );

        List<Run> syncedRuns = new ArrayList<>();

        for (GarminActivity activity : activities) {
            // Only sync running activities
            if (!isRunningActivity(activity)) {
                log.debug("Skipping non-running activity: {} (type: {})",
                        activity.getActivityName(),
                        activity.getActivityType() != null ? activity.getActivityType().getTypeKey() : "unknown");
                continue;
            }

            // Check if activity already exists
            if (runRepository.findByGarminActivityIdAndUserId(activity.getActivityId(), userId).isPresent()) {
                log.debug("Activity {} already exists, skipping", activity.getActivityId());
                continue;
            }

            // Convert Garmin activity to Run
            Run run = convertGarminActivityToRun(activity, userId);
            Run savedRun = runRepository.save(run);
            syncedRuns.add(savedRun);

            // Fetch and save GPS points from activity details
            try {
                log.debug("Fetching GPS data for activity {} (run {})", activity.getActivityId(), savedRun.getId());
                GarminActivityDetails details = garminApiService.getActivityDetails(accessToken, activity.getActivityId());

                int gpsPointCount = 0;
                if (details != null && details.getGeoPolylineDTO() != null) {
                    gpsPointCount = createGpsPointsFromDetails(savedRun, details, activity.getStartTimeLocal());
                }

                if (gpsPointCount > 0) {
                    log.info("Synced activity: {} (Garmin ID: {}) with {} kcal and {} GPS points",
                            activity.getActivityName(), activity.getActivityId(),
                            activity.getCalories() != null ? activity.getCalories().intValue() : 0,
                            gpsPointCount);

                    // Process activity for route matching and trophy checking
                    try {
                        log.info("Processing activity for route matching and trophy checking for run {}", savedRun.getId());
                        userActivityService.processAndCreateActivityFromRun(user, savedRun);
                    } catch (Exception ex) {
                        log.error("Error processing activity for route matching and trophies for run {}: {}",
                                savedRun.getId(), ex.getMessage(), ex);
                    }
                } else {
                    log.warn("Synced activity: {} (Garmin ID: {}) with {} kcal but NO GPS points",
                            activity.getActivityName(), activity.getActivityId(),
                            activity.getCalories() != null ? activity.getCalories().intValue() : 0);
                }
            } catch (Exception e) {
                log.error("Error fetching GPS data for activity {} ({}): {}",
                        activity.getActivityId(), e.getClass().getSimpleName(), e.getMessage(), e);
                log.info("Synced activity: {} (Garmin ID: {}) with {} kcal (no GPS data due to error)",
                        activity.getActivityName(), activity.getActivityId(),
                        activity.getCalories() != null ? activity.getCalories().intValue() : 0);
            }
        }

        log.info("Successfully synced {} new runs for user ID: {}", syncedRuns.size(), userId);
        return syncedRuns;
    }

    /**
     * Retroactively fetches and saves GPS points for an existing Garmin run
     */
    @Transactional
    public int backfillGpsPointsForRun(Long userId, Long runId) {
        log.info("Backfilling GPS points for run ID: {} and user ID: {}", runId, userId);

        Run run = runRepository.findByIdAndUserId(runId, userId)
                .orElseThrow(() -> new RuntimeException("Run not found for user"));

        if (run.getGarminActivityId() == null) {
            throw new RuntimeException("Run is not from Garmin - cannot backfill GPS points");
        }

        long existingGpsPointCount = gpsPointRepository.countByRunId(runId);
        if (existingGpsPointCount > 0) {
            log.info("Run {} already has {} GPS points - no backfill needed", runId, existingGpsPointCount);
            return (int) existingGpsPointCount;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isGarminConnected()) {
            throw new RuntimeException("User is not connected to Garmin");
        }

        String accessToken = getValidAccessToken(user);

        try {
            GarminActivityDetails details = garminApiService.getActivityDetails(accessToken, run.getGarminActivityId());
            if (details == null || details.getGeoPolylineDTO() == null) {
                throw new RuntimeException("No GPS data available for this activity");
            }

            // We need the start time, but we can use the run's start time
            int gpsPointCount = createGpsPointsFromDetails(run, details, run.getStartTime().toString());
            log.info("Backfilled {} GPS points for run {}", gpsPointCount, runId);
            return gpsPointCount;
        } catch (Exception e) {
            log.error("Error backfilling GPS points for run {}: {}", runId, e.getMessage());
            throw new RuntimeException("Failed to backfill GPS points: " + e.getMessage(), e);
        }
    }

    /**
     * Backfills GPS points for all Garmin runs that don't have GPS data
     */
    @Transactional
    public Map<String, Object> backfillAllMissingGpsPoints(Long userId) {
        log.info("Backfilling GPS points for all Garmin runs without GPS data for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isGarminConnected()) {
            throw new RuntimeException("User is not connected to Garmin");
        }

        List<Run> garminRuns = runRepository.findByUserIdAndGarminActivityIdIsNotNull(userId);
        log.info("Found {} Garmin runs for user {}", garminRuns.size(), userId);

        int processedCount = 0;
        int successCount = 0;
        int alreadyHadGpsCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();

        for (Run run : garminRuns) {
            processedCount++;

            long existingGpsPointCount = gpsPointRepository.countByRunId(run.getId());
            if (existingGpsPointCount > 0) {
                log.debug("Run {} already has {} GPS points - skipping", run.getId(), existingGpsPointCount);
                alreadyHadGpsCount++;
                continue;
            }

            try {
                int gpsPointCount = backfillGpsPointsForRun(userId, run.getId());
                if (gpsPointCount > 0) {
                    successCount++;
                    log.info("Successfully backfilled {} GPS points for run {}", gpsPointCount, run.getId());
                }
            } catch (Exception e) {
                failedCount++;
                String errorMsg = String.format("Run %d: %s", run.getId(), e.getMessage());
                errors.add(errorMsg);
                log.warn("Failed to backfill GPS points for run {}: {}", run.getId(), e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalGarminRuns", garminRuns.size());
        result.put("processedCount", processedCount);
        result.put("successCount", successCount);
        result.put("alreadyHadGpsCount", alreadyHadGpsCount);
        result.put("failedCount", failedCount);
        result.put("errors", errors);

        log.info("Backfill complete: {} success, {} already had GPS, {} failed out of {} Garmin runs",
                successCount, alreadyHadGpsCount, failedCount, garminRuns.size());

        return result;
    }

    /**
     * Gets a valid access token, refreshing if necessary
     */
    private String getValidAccessToken(User user) {
        LocalDateTime now = LocalDateTime.now();

        // Check if token is expired or about to expire (within 5 minutes)
        if (user.getGarminTokenExpiresAt() == null ||
                user.getGarminTokenExpiresAt().isBefore(now.plusMinutes(5))) {

            log.info("Refreshing expired token for user ID: {}", user.getId());

            GarminTokenResponse tokenResponse = garminApiService.refreshToken(user.getGarminRefreshToken());

            user.setGarminAccessToken(tokenResponse.getAccessToken());
            user.setGarminRefreshToken(tokenResponse.getRefreshToken());
            user.setGarminTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));

            userRepository.save(user);
        }

        return user.getGarminAccessToken();
    }

    /**
     * Checks if the activity is a running activity
     */
    private boolean isRunningActivity(GarminActivity activity) {
        if (activity.getActivityType() == null) {
            return false;
        }
        String typeKey = activity.getActivityType().getTypeKey();
        // Garmin uses typeKey like "running", "trail_running", "treadmill_running"
        return typeKey != null && typeKey.toLowerCase().contains("running");
    }

    /**
     * Converts a Garmin activity to a Run entity
     */
    private Run convertGarminActivityToRun(GarminActivity activity, Long userId) {
        LocalDateTime startTime = parseGarminDateTime(activity.getStartTimeLocal());
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }

        Integer durationSeconds = activity.getMovingDuration() != null ?
                activity.getMovingDuration().intValue() :
                (activity.getDuration() != null ? activity.getDuration().intValue() : 0);

        LocalDateTime endTime = startTime.plusSeconds(durationSeconds);

        // Convert speed from m/s to km/h
        BigDecimal averageSpeedKmh = activity.getAverageSpeed() != null ?
                BigDecimal.valueOf(activity.getAverageSpeed() * 3.6) : null;
        BigDecimal maxSpeedKmh = activity.getMaxSpeed() != null ?
                BigDecimal.valueOf(activity.getMaxSpeed() * 3.6) : null;

        // Calculate average pace in seconds per km
        Double averagePaceSecondsPerKm = null;
        if (activity.getDistance() != null && activity.getDistance() > 0 && durationSeconds > 0) {
            double distanceKm = activity.getDistance() / 1000.0;
            averagePaceSecondsPerKm = durationSeconds / distanceKm;
        }

        return Run.builder()
                .userId(userId)
                .title(activity.getActivityName() != null ? activity.getActivityName() : "Garmin Run")
                .description(activity.getDescription())
                .startTime(startTime)
                .endTime(endTime)
                .durationSeconds(durationSeconds)
                .distanceMeters(activity.getDistance() != null ? BigDecimal.valueOf(activity.getDistance()) : null)
                .averagePaceSecondsPerKm(averagePaceSecondsPerKm)
                .maxSpeedKmh(maxSpeedKmh)
                .averageSpeedKmh(averageSpeedKmh)
                .caloriesBurned(activity.getCalories() != null ? activity.getCalories().intValue() : null)
                .elevationGainMeters(activity.getElevationGain() != null ?
                        BigDecimal.valueOf(activity.getElevationGain()) : null)
                .runType(Run.RunType.OUTDOOR)
                .status(Run.RunStatus.COMPLETED)
                .isPublic(true)
                .garminActivityId(activity.getActivityId())
                .build();
    }

    /**
     * Parses Garmin datetime string to LocalDateTime
     */
    private LocalDateTime parseGarminDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            // Garmin uses ISO format like "2024-01-15T10:30:00.000"
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            try {
                // Try with timezone
                ZonedDateTime zdt = ZonedDateTime.parse(dateTimeStr);
                return zdt.toLocalDateTime();
            } catch (Exception e2) {
                log.warn("Failed to parse Garmin datetime: {}", dateTimeStr);
                return null;
            }
        }
    }

    /**
     * Creates GPS points from Garmin activity details
     */
    private int createGpsPointsFromDetails(Run run, GarminActivityDetails details, String startTimeStr) {
        if (details.getGeoPolylineDTO() == null || details.getGeoPolylineDTO().getPolyline() == null) {
            log.warn("No polyline data available for run {}", run.getId());
            return 0;
        }

        List<GarminActivityDetails.Point> polyline = details.getGeoPolylineDTO().getPolyline();
        if (polyline.isEmpty()) {
            log.warn("Empty polyline for run {}", run.getId());
            return 0;
        }

        LocalDateTime startTime = parseGarminDateTime(startTimeStr);
        if (startTime == null) {
            startTime = run.getStartTime();
        }

        List<GpsPoint> gpsPoints = new ArrayList<>();
        int maxPoints = Math.min(polyline.size(), 1000); // Limit to 1000 points

        for (int i = 0; i < maxPoints; i++) {
            GarminActivityDetails.Point point = polyline.get(i);

            if (point.getLat() == null || point.getLon() == null) {
                continue;
            }

            // Calculate timestamp
            LocalDateTime timestamp = startTime;
            if (point.getTime() != null) {
                // Garmin time is in milliseconds from epoch
                timestamp = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(point.getTime()),
                        ZoneId.systemDefault()
                );
            }

            GpsPoint.GpsPointBuilder builder = GpsPoint.builder()
                    .run(run)
                    .latitude(BigDecimal.valueOf(point.getLat()))
                    .longitude(BigDecimal.valueOf(point.getLon()))
                    .sequenceNumber(i)
                    .timestamp(timestamp);

            if (point.getAltitude() != null) {
                builder.altitude(BigDecimal.valueOf(point.getAltitude()));
            }

            if (point.getDistanceFromStart() != null) {
                builder.distanceFromStartMeters(BigDecimal.valueOf(point.getDistanceFromStart()));
            }

            gpsPoints.add(builder.build());
        }

        if (!gpsPoints.isEmpty()) {
            gpsPointRepository.saveAll(gpsPoints);
            log.info("Saved {} GPS points for run {}", gpsPoints.size(), run.getId());
        }

        return gpsPoints.size();
    }
}
