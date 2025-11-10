package com.mainstream.strava.service;

import com.mainstream.run.entity.GpsPoint;
import com.mainstream.run.entity.Run;
import com.mainstream.run.repository.GpsPointRepository;
import com.mainstream.run.repository.RunRepository;
import com.mainstream.strava.dto.StravaActivity;
import com.mainstream.strava.dto.StravaStream;
import com.mainstream.strava.dto.StravaTokenResponse;
import com.mainstream.user.entity.User;
import com.mainstream.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StravaSyncService {

    private final StravaApiService stravaApiService;
    private final UserRepository userRepository;
    private final RunRepository runRepository;
    private final GpsPointRepository gpsPointRepository;

    /**
     * Connects a user to Strava using the authorization code
     */
    @Transactional
    public User connectStrava(Long userId, String authorizationCode) {
        log.info("Connecting Strava for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Exchange authorization code for tokens
        StravaTokenResponse tokenResponse = stravaApiService.exchangeToken(authorizationCode);

        // Update user with Strava tokens
        user.setStravaUserId(tokenResponse.getAthlete().getId());
        user.setStravaAccessToken(tokenResponse.getAccessToken());
        user.setStravaRefreshToken(tokenResponse.getRefreshToken());
        user.setStravaTokenExpiresAt(LocalDateTime.ofEpochSecond(
                tokenResponse.getExpiresAt(), 0, ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now())
        ));
        user.setStravaConnectedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("Successfully connected Strava for user ID: {}, Strava athlete ID: {}",
                userId, tokenResponse.getAthlete().getId());

        return savedUser;
    }

    /**
     * Disconnects Strava from a user account
     */
    @Transactional
    public void disconnectStrava(Long userId) {
        log.info("Disconnecting Strava for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStravaUserId(null);
        user.setStravaAccessToken(null);
        user.setStravaRefreshToken(null);
        user.setStravaTokenExpiresAt(null);
        user.setStravaConnectedAt(null);

        userRepository.save(user);
        log.info("Successfully disconnected Strava for user ID: {}", userId);
    }

    /**
     * Synchronizes activities from Strava
     */
    @Transactional
    public List<Run> syncActivities(Long userId, LocalDateTime since) {
        log.info("Syncing Strava activities for user ID: {} since: {}", userId, since);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isStravaConnected()) {
            throw new RuntimeException("User is not connected to Strava");
        }

        // Check if token needs refresh
        String accessToken = getValidAccessToken(user);

        // Fetch activities from Strava
        List<StravaActivity> activities = stravaApiService.getActivities(
                accessToken,
                since,
                100 // fetch up to 100 activities
        );

        List<Run> syncedRuns = new ArrayList<>();

        for (StravaActivity activity : activities) {
            // Only sync running activities
            if (!"Run".equalsIgnoreCase(activity.getType())) {
                log.debug("Skipping non-running activity: {} (type: {})", activity.getName(), activity.getType());
                continue;
            }

            // Check if activity already exists
            if (runRepository.findByStravaActivityIdAndUserId(activity.getId(), userId).isPresent()) {
                log.debug("Activity {} already exists, skipping", activity.getId());
                continue;
            }

            // Fetch detailed activity to get calories and other detailed info
            StravaActivity detailedActivity = stravaApiService.getActivity(accessToken, activity.getId());

            // Convert Strava activity to Run (using detailed activity data)
            Run run = convertStravaActivityToRun(detailedActivity, userId);
            Run savedRun = runRepository.save(run);
            syncedRuns.add(savedRun);

            // Fetch and save GPS points from activity streams
            try {
                List<StravaStream> streams = stravaApiService.getActivityStreams(accessToken, activity.getId());
                int gpsPointCount = createGpsPointsFromStreams(savedRun, streams, detailedActivity.getStartDateLocal());
                log.info("Synced activity: {} (Strava ID: {}) with {} kcal and {} GPS points",
                        activity.getName(), activity.getId(),
                        detailedActivity.getCalories() != null ? detailedActivity.getCalories().intValue() : 0,
                        gpsPointCount);
            } catch (Exception e) {
                log.error("Error fetching GPS data for activity {}: {}", activity.getId(), e.getMessage());
                log.info("Synced activity: {} (Strava ID: {}) with {} kcal (no GPS data)",
                        activity.getName(), activity.getId(),
                        detailedActivity.getCalories() != null ? detailedActivity.getCalories().intValue() : 0);
            }
        }

        log.info("Successfully synced {} new runs for user ID: {}", syncedRuns.size(), userId);
        return syncedRuns;
    }

    /**
     * Gets a valid access token, refreshing if necessary
     */
    private String getValidAccessToken(User user) {
        LocalDateTime now = LocalDateTime.now();

        // Check if token is expired or about to expire (within 5 minutes)
        if (user.getStravaTokenExpiresAt() == null ||
                user.getStravaTokenExpiresAt().isBefore(now.plusMinutes(5))) {

            log.info("Refreshing expired token for user ID: {}", user.getId());

            StravaTokenResponse tokenResponse = stravaApiService.refreshToken(user.getStravaRefreshToken());

            user.setStravaAccessToken(tokenResponse.getAccessToken());
            user.setStravaRefreshToken(tokenResponse.getRefreshToken());
            user.setStravaTokenExpiresAt(LocalDateTime.ofEpochSecond(
                    tokenResponse.getExpiresAt(), 0, ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now())
            ));

            userRepository.save(user);
        }

        return user.getStravaAccessToken();
    }

    /**
     * Converts a Strava activity to a Run entity
     */
    private Run convertStravaActivityToRun(StravaActivity activity, Long userId) {
        LocalDateTime startTime = activity.getStartDateLocal() != null ?
                LocalDateTime.ofInstant(activity.getStartDateLocal().toInstant(), ZoneId.systemDefault()) :
                LocalDateTime.now();

        Integer durationSeconds = activity.getMovingTime() != null ? activity.getMovingTime() : 0;
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
                .title(activity.getName() != null ? activity.getName() : "Strava Run")
                .description(activity.getDescription())
                .startTime(startTime)
                .endTime(endTime)
                .durationSeconds(durationSeconds)
                .distanceMeters(activity.getDistance() != null ? BigDecimal.valueOf(activity.getDistance()) : null)
                .averagePaceSecondsPerKm(averagePaceSecondsPerKm)
                .maxSpeedKmh(maxSpeedKmh)
                .averageSpeedKmh(averageSpeedKmh)
                .caloriesBurned(activity.getCalories() != null ? activity.getCalories().intValue() : null)
                .elevationGainMeters(activity.getTotalElevationGain() != null ?
                        BigDecimal.valueOf(activity.getTotalElevationGain()) : null)
                .runType(activity.getManual() != null && activity.getManual() ?
                        Run.RunType.OUTDOOR : Run.RunType.OUTDOOR)
                .status(Run.RunStatus.COMPLETED)
                .isPublic(true)
                .stravaActivityId(activity.getId())
                .build();
    }

    /**
     * Creates GPS points from Strava activity streams
     */
    private int createGpsPointsFromStreams(Run run, List<StravaStream> streams, java.time.ZonedDateTime activityStartTime) {
        if (streams == null || streams.isEmpty()) {
            log.debug("No streams available for activity");
            return 0;
        }

        // Find the required streams
        StravaStream latlngStream = streams.stream()
                .filter(s -> "latlng".equals(s.getType()))
                .findFirst()
                .orElse(null);

        if (latlngStream == null || latlngStream.getLatLngData() == null || latlngStream.getLatLngData().isEmpty()) {
            log.debug("No GPS coordinates available in streams");
            return 0;
        }

        // Get optional streams
        StravaStream altitudeStream = streams.stream()
                .filter(s -> "altitude".equals(s.getType()))
                .findFirst()
                .orElse(null);

        StravaStream timeStream = streams.stream()
                .filter(s -> "time".equals(s.getType()))
                .findFirst()
                .orElse(null);

        StravaStream distanceStream = streams.stream()
                .filter(s -> "distance".equals(s.getType()))
                .findFirst()
                .orElse(null);

        List<List<Double>> latlngData = latlngStream.getLatLngData();
        List<Double> altitudeData = altitudeStream != null ? altitudeStream.getNumericData() : null;
        List<Integer> timeData = timeStream != null ? timeStream.getTimeData() : null;
        List<Double> distanceData = distanceStream != null ? distanceStream.getNumericData() : null;

        // Convert activity start time to LocalDateTime
        LocalDateTime startTime = activityStartTime != null ?
                LocalDateTime.ofInstant(activityStartTime.toInstant(), ZoneId.systemDefault()) :
                run.getStartTime();

        List<GpsPoint> gpsPoints = new ArrayList<>();
        int maxPoints = Math.min(latlngData.size(), 1000); // Limit to 1000 points for performance

        for (int i = 0; i < maxPoints; i++) {
            List<Double> coords = latlngData.get(i);
            if (coords == null || coords.size() < 2) {
                continue;
            }

            Double latitude = coords.get(0);
            Double longitude = coords.get(1);

            if (latitude == null || longitude == null) {
                continue;
            }

            // Calculate timestamp from time offset
            LocalDateTime timestamp = startTime;
            if (timeData != null && i < timeData.size() && timeData.get(i) != null) {
                timestamp = startTime.plusSeconds(timeData.get(i));
            }

            GpsPoint.GpsPointBuilder builder = GpsPoint.builder()
                    .run(run)
                    .latitude(BigDecimal.valueOf(latitude))
                    .longitude(BigDecimal.valueOf(longitude))
                    .sequenceNumber(i)
                    .timestamp(timestamp);

            // Add altitude if available
            if (altitudeData != null && i < altitudeData.size() && altitudeData.get(i) != null) {
                builder.altitude(BigDecimal.valueOf(altitudeData.get(i)));
            }

            // Add distance from start if available
            if (distanceData != null && i < distanceData.size() && distanceData.get(i) != null) {
                builder.distanceFromStartMeters(BigDecimal.valueOf(distanceData.get(i)));
            }

            gpsPoints.add(builder.build());
        }

        // Save all GPS points in batch
        if (!gpsPoints.isEmpty()) {
            gpsPointRepository.saveAll(gpsPoints);
            log.info("Saved {} GPS points for run {}", gpsPoints.size(), run.getId());
        }

        return gpsPoints.size();
    }
}
