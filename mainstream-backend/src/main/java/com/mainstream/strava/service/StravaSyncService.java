package com.mainstream.strava.service;

import com.mainstream.run.entity.Run;
import com.mainstream.run.repository.RunRepository;
import com.mainstream.strava.dto.StravaActivity;
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

            log.info("Synced activity: {} (Strava ID: {}) with {} kcal",
                    activity.getName(), activity.getId(),
                    detailedActivity.getCalories() != null ? detailedActivity.getCalories().intValue() : 0);
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
}
