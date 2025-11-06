package com.mainstream.activity.service;

import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.repository.UserActivityRepository;
import com.mainstream.fitfile.entity.FitFileUpload;
import com.mainstream.fitfile.entity.FitTrackPoint;
import com.mainstream.fitfile.repository.FitTrackPointRepository;
import com.mainstream.run.entity.GpsPoint;
import com.mainstream.run.entity.Run;
import com.mainstream.run.repository.GpsPointRepository;
import com.mainstream.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing user activities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityService {

    private final UserActivityRepository userActivityRepository;
    private final FitTrackPointRepository fitTrackPointRepository;
    private final GpsPointRepository gpsPointRepository;
    private final RouteMatchingService routeMatchingService;
    private final TrophyService trophyService;

    /**
     * Process a FIT file upload and create a user activity with route matching.
     *
     * @param user The user
     * @param fitFileUpload The processed FIT file upload
     * @return Created UserActivity
     */
    @Transactional
    public UserActivity processAndCreateActivity(User user, FitFileUpload fitFileUpload) {
        log.info("Processing activity for user {} from FIT file {}", user.getId(), fitFileUpload.getId());

        // Get track points from FIT file (only those with valid GPS data)
        List<FitTrackPoint> trackPoints = fitTrackPointRepository.findByFitFileUploadIdWithGpsData(fitFileUpload.getId());

        log.info("Found {} track points with GPS data for FIT file {}", trackPoints.size(), fitFileUpload.getId());

        if (trackPoints.isEmpty()) {
            log.warn("No track points with GPS data found for FIT file {} - creating activity without route match", fitFileUpload.getId());
            return createBasicActivity(user, fitFileUpload, null);
        }

        // Match against predefined routes
        log.info("Attempting to match {} track points against predefined routes", trackPoints.size());
        RouteMatchingService.RouteMatchResult matchResult = routeMatchingService.matchRoute(trackPoints);

        if (matchResult != null && matchResult.getMatchedRoute() != null) {
            log.info("Successfully matched FIT file {} to route: {} ({}% complete)",
                     fitFileUpload.getId(),
                     matchResult.getMatchedRoute().getName(),
                     matchResult.getRouteCompletionPercentage());
        } else {
            log.info("No route match found for FIT file {}", fitFileUpload.getId());
        }

        // Create activity
        UserActivity activity = createBasicActivity(user, fitFileUpload, matchResult);

        // Check and award trophies
        trophyService.checkAndAwardTrophies(user, activity);

        return activity;
    }

    /**
     * Create a basic user activity with optional route matching result.
     */
    private UserActivity createBasicActivity(User user, FitFileUpload fitFileUpload,
                                             RouteMatchingService.RouteMatchResult matchResult) {
        UserActivity activity = new UserActivity();
        activity.setUser(user);
        activity.setFitFileUpload(fitFileUpload);

        // Set basic activity data from FIT file
        if (fitFileUpload.getActivityStartTime() != null) {
            activity.setActivityStartTime(fitFileUpload.getActivityStartTime());
        }
        if (fitFileUpload.getTotalTimerTime() != null) {
            activity.setDurationSeconds(fitFileUpload.getTotalTimerTime().intValue());
            if (activity.getActivityStartTime() != null) {
                activity.setActivityEndTime(activity.getActivityStartTime().plusSeconds(activity.getDurationSeconds()));
            }
        }
        if (fitFileUpload.getTotalDistance() != null) {
            activity.setDistanceMeters(fitFileUpload.getTotalDistance());
        }

        // Set route matching data if available
        if (matchResult != null && matchResult.getMatchedRoute() != null) {
            activity.setMatchedRoute(matchResult.getMatchedRoute());
            activity.setMatchedDistanceMeters(BigDecimal.valueOf(matchResult.getMatchedDistanceMeters()));
            activity.setRouteCompletionPercentage(BigDecimal.valueOf(matchResult.getRouteCompletionPercentage()));
            activity.setAverageMatchingAccuracyMeters(BigDecimal.valueOf(matchResult.getAverageAccuracyMeters()));
            activity.setIsCompleteRoute(matchResult.isCompleteRoute());
            activity.setDirection(matchResult.getDirection());

            log.info("Activity matched to route: {} ({}% complete, direction: {})",
                     matchResult.getMatchedRoute().getName(),
                     matchResult.getRouteCompletionPercentage(),
                     matchResult.getDirection());
        } else {
            activity.setIsCompleteRoute(false);
            activity.setDirection(UserActivity.RunDirection.UNKNOWN);
            log.info("Activity did not match any predefined route");
        }

        return userActivityRepository.save(activity);
    }

    /**
     * Get all activities for a user.
     */
    public List<UserActivity> getUserActivities(Long userId) {
        return userActivityRepository.findByUserIdOrderByActivityStartTimeDesc(userId);
    }

    /**
     * Get all activities with matched routes (for community map).
     * Returns only activities that have been matched to a predefined route.
     */
    public List<UserActivity> getAllActivitiesWithRoutes() {
        return userActivityRepository.findAll().stream()
                .filter(activity -> activity.getMatchedRoute() != null)
                .sorted((a, b) -> b.getActivityStartTime().compareTo(a.getActivityStartTime()))
                .collect(Collectors.toList());
    }

    /**
     * Get activity by ID.
     */
    public UserActivity getActivityById(Long id) {
        return userActivityRepository.findById(id).orElse(null);
    }

    /**
     * Process a manual Run and create a user activity with route matching.
     *
     * @param user The user
     * @param run The completed run
     * @return Created UserActivity, or null if no match found
     */
    @Transactional
    public UserActivity processAndCreateActivityFromRun(User user, Run run) {
        log.info("Processing activity for user {} from run {}", user.getId(), run.getId());

        // Get GPS points from the run
        List<GpsPoint> gpsPoints = gpsPointRepository.findByRunIdOrderBySequenceNumberAsc(run.getId());

        if (gpsPoints.isEmpty()) {
            log.warn("No GPS points found for run {} - cannot match to route", run.getId());
            return null;
        }

        log.info("Found {} GPS points for run {}", gpsPoints.size(), run.getId());

        // Match against predefined routes
        RouteMatchingService.RouteMatchResult matchResult = routeMatchingService.matchRouteFromGpsPoints(gpsPoints);

        if (matchResult == null || matchResult.getMatchedRoute() == null) {
            log.info("Run {} did not match any predefined route", run.getId());
            return null;
        }

        // Create activity from run
        UserActivity activity = createActivityFromRun(user, run, matchResult);

        // Check and award trophies
        trophyService.checkAndAwardTrophies(user, activity);

        return activity;
    }

    /**
     * Create a user activity from a run with route matching result.
     */
    private UserActivity createActivityFromRun(User user, Run run,
                                               RouteMatchingService.RouteMatchResult matchResult) {
        UserActivity activity = new UserActivity();
        activity.setUser(user);
        activity.setRun(run);

        // Set basic activity data from run
        activity.setActivityStartTime(run.getStartTime());
        activity.setActivityEndTime(run.getEndTime());
        activity.setDurationSeconds(run.getDurationSeconds());
        activity.setDistanceMeters(run.getDistanceMeters());

        // Set route matching data
        activity.setMatchedRoute(matchResult.getMatchedRoute());
        activity.setMatchedDistanceMeters(BigDecimal.valueOf(matchResult.getMatchedDistanceMeters()));
        activity.setRouteCompletionPercentage(BigDecimal.valueOf(matchResult.getRouteCompletionPercentage()));
        activity.setAverageMatchingAccuracyMeters(BigDecimal.valueOf(matchResult.getAverageAccuracyMeters()));
        activity.setIsCompleteRoute(matchResult.isCompleteRoute());
        activity.setDirection(matchResult.getDirection());

        log.info("Run {} matched to route: {} ({}% complete, direction: {})",
                 run.getId(),
                 matchResult.getMatchedRoute().getName(),
                 matchResult.getRouteCompletionPercentage(),
                 matchResult.getDirection());

        return userActivityRepository.save(activity);
    }

    /**
     * Find activity by FIT file upload ID.
     */
    public java.util.Optional<UserActivity> findByFitFileUploadId(Long fitFileUploadId) {
        return userActivityRepository.findByFitFileUploadId(fitFileUploadId);
    }

    /**
     * Find activity by run ID.
     */
    public java.util.Optional<UserActivity> findByRunId(Long runId) {
        return userActivityRepository.findByRunId(runId);
    }

    /**
     * Update an existing activity with new route matching result.
     */
    private UserActivity updateActivityWithMatchResult(UserActivity existingActivity, Run run,
                                                       RouteMatchingService.RouteMatchResult matchResult) {
        // Update basic activity data from run
        existingActivity.setActivityStartTime(run.getStartTime());
        existingActivity.setActivityEndTime(run.getEndTime());
        existingActivity.setDurationSeconds(run.getDurationSeconds());
        existingActivity.setDistanceMeters(run.getDistanceMeters());

        // Update route matching data
        existingActivity.setMatchedRoute(matchResult.getMatchedRoute());
        existingActivity.setMatchedDistanceMeters(BigDecimal.valueOf(matchResult.getMatchedDistanceMeters()));
        existingActivity.setRouteCompletionPercentage(BigDecimal.valueOf(matchResult.getRouteCompletionPercentage()));
        existingActivity.setAverageMatchingAccuracyMeters(BigDecimal.valueOf(matchResult.getAverageAccuracyMeters()));
        existingActivity.setIsCompleteRoute(matchResult.isCompleteRoute());
        existingActivity.setDirection(matchResult.getDirection());

        log.info("Updated activity {} for run {} with route: {} ({}% complete, direction: {})",
                 existingActivity.getId(),
                 run.getId(),
                 matchResult.getMatchedRoute().getName(),
                 matchResult.getRouteCompletionPercentage(),
                 matchResult.getDirection());

        return userActivityRepository.save(existingActivity);
    }

    /**
     * Process and create or update activity from a run.
     * If an activity already exists for the run, it will be updated.
     * Otherwise, a new activity will be created.
     *
     * @param user The user
     * @param run The completed run
     * @return Created or updated UserActivity, or null if no match found
     */
    @Transactional
    public UserActivity processAndUpdateOrCreateActivityFromRun(User user, Run run) {
        log.info("Processing or updating activity for user {} from run {}", user.getId(), run.getId());

        // Get GPS points from the run
        List<GpsPoint> gpsPoints = gpsPointRepository.findByRunIdOrderBySequenceNumberAsc(run.getId());

        if (gpsPoints.isEmpty()) {
            log.warn("No GPS points found for run {} - cannot match to route", run.getId());
            return null;
        }

        log.info("Found {} GPS points for run {}", gpsPoints.size(), run.getId());

        // Match against predefined routes
        RouteMatchingService.RouteMatchResult matchResult = routeMatchingService.matchRouteFromGpsPoints(gpsPoints);

        if (matchResult == null || matchResult.getMatchedRoute() == null) {
            log.info("Run {} did not match any predefined route", run.getId());
            return null;
        }

        // Check if activity already exists for this run
        java.util.Optional<UserActivity> existingActivityOpt = userActivityRepository.findByRunId(run.getId());

        UserActivity activity;
        if (existingActivityOpt.isPresent()) {
            // Update existing activity
            log.info("Found existing activity {} for run {}, updating it", existingActivityOpt.get().getId(), run.getId());
            activity = updateActivityWithMatchResult(existingActivityOpt.get(), run, matchResult);
        } else {
            // Create new activity
            log.info("No existing activity for run {}, creating new one", run.getId());
            activity = createActivityFromRun(user, run, matchResult);
        }

        // Check and award trophies
        trophyService.checkAndAwardTrophies(user, activity);

        return activity;
    }

    /**
     * Update an existing activity with new FIT file route matching result.
     */
    private UserActivity updateActivityWithFitFileMatchResult(UserActivity existingActivity, FitFileUpload fitFileUpload,
                                                              RouteMatchingService.RouteMatchResult matchResult) {
        // Update basic activity data from FIT file
        if (fitFileUpload.getActivityStartTime() != null) {
            existingActivity.setActivityStartTime(fitFileUpload.getActivityStartTime());
        }
        if (fitFileUpload.getTotalTimerTime() != null) {
            existingActivity.setDurationSeconds(fitFileUpload.getTotalTimerTime().intValue());
            if (existingActivity.getActivityStartTime() != null) {
                existingActivity.setActivityEndTime(existingActivity.getActivityStartTime().plusSeconds(existingActivity.getDurationSeconds()));
            }
        }
        if (fitFileUpload.getTotalDistance() != null) {
            existingActivity.setDistanceMeters(fitFileUpload.getTotalDistance());
        }

        // Update route matching data
        if (matchResult != null && matchResult.getMatchedRoute() != null) {
            existingActivity.setMatchedRoute(matchResult.getMatchedRoute());
            existingActivity.setMatchedDistanceMeters(BigDecimal.valueOf(matchResult.getMatchedDistanceMeters()));
            existingActivity.setRouteCompletionPercentage(BigDecimal.valueOf(matchResult.getRouteCompletionPercentage()));
            existingActivity.setAverageMatchingAccuracyMeters(BigDecimal.valueOf(matchResult.getAverageAccuracyMeters()));
            existingActivity.setIsCompleteRoute(matchResult.isCompleteRoute());
            existingActivity.setDirection(matchResult.getDirection());

            log.info("Updated activity {} for FIT file {} with route: {} ({}% complete, direction: {})",
                     existingActivity.getId(),
                     fitFileUpload.getId(),
                     matchResult.getMatchedRoute().getName(),
                     matchResult.getRouteCompletionPercentage(),
                     matchResult.getDirection());
        } else {
            existingActivity.setIsCompleteRoute(false);
            existingActivity.setDirection(UserActivity.RunDirection.UNKNOWN);
            log.info("Updated activity {} for FIT file {} - no route match", existingActivity.getId(), fitFileUpload.getId());
        }

        return userActivityRepository.save(existingActivity);
    }

    /**
     * Process and create or update activity from a FIT file.
     * If an activity already exists for the FIT file, it will be updated.
     * Otherwise, a new activity will be created.
     *
     * @param user The user
     * @param fitFileUpload The processed FIT file upload
     * @return Created or updated UserActivity
     */
    @Transactional
    public UserActivity processAndUpdateOrCreateActivity(User user, FitFileUpload fitFileUpload) {
        log.info("Processing or updating activity for user {} from FIT file {}", user.getId(), fitFileUpload.getId());

        // Get track points from FIT file (only those with valid GPS data)
        List<FitTrackPoint> trackPoints = fitTrackPointRepository.findByFitFileUploadIdWithGpsData(fitFileUpload.getId());

        log.info("Found {} track points with GPS data for FIT file {}", trackPoints.size(), fitFileUpload.getId());

        // Match against predefined routes (even if no track points, we can still create an activity)
        RouteMatchingService.RouteMatchResult matchResult = null;
        if (!trackPoints.isEmpty()) {
            log.info("Attempting to match {} track points against predefined routes", trackPoints.size());
            matchResult = routeMatchingService.matchRoute(trackPoints);

            if (matchResult != null && matchResult.getMatchedRoute() != null) {
                log.info("Successfully matched FIT file {} to route: {} ({}% complete)",
                         fitFileUpload.getId(),
                         matchResult.getMatchedRoute().getName(),
                         matchResult.getRouteCompletionPercentage());
            } else {
                log.info("No route match found for FIT file {}", fitFileUpload.getId());
            }
        } else {
            log.warn("No track points with GPS data found for FIT file {} - creating activity without route match", fitFileUpload.getId());
        }

        // Check if activity already exists for this FIT file
        java.util.Optional<UserActivity> existingActivityOpt = userActivityRepository.findByFitFileUploadId(fitFileUpload.getId());

        UserActivity activity;
        if (existingActivityOpt.isPresent()) {
            // Update existing activity
            log.info("Found existing activity {} for FIT file {}, updating it", existingActivityOpt.get().getId(), fitFileUpload.getId());
            activity = updateActivityWithFitFileMatchResult(existingActivityOpt.get(), fitFileUpload, matchResult);
        } else {
            // Create new activity
            log.info("No existing activity for FIT file {}, creating new one", fitFileUpload.getId());
            activity = createBasicActivity(user, fitFileUpload, matchResult);
        }

        // Check and award trophies
        trophyService.checkAndAwardTrophies(user, activity);

        return activity;
    }

    /**
     * Delete an activity.
     */
    @Transactional
    public void deleteActivity(Long activityId) {
        log.info("Deleting activity {}", activityId);
        userActivityRepository.deleteById(activityId);
    }
}
