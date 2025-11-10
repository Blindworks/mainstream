package com.mainstream.activity.service.trophy.checker;

import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.service.trophy.TrophyChecker;
import com.mainstream.activity.service.trophy.TrophyConfigParser;
import com.mainstream.activity.service.trophy.TrophyProgress;
import com.mainstream.activity.service.trophy.config.LocationBasedConfig;
import com.mainstream.fitfile.entity.FitTrackPoint;
import com.mainstream.fitfile.repository.FitTrackPointRepository;
import com.mainstream.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Checker for LOCATION_BASED trophies.
 * Awards trophies when user's GPS track passes through a specific location.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocationBasedChecker implements TrophyChecker {

    private final TrophyConfigParser configParser;
    private final FitTrackPointRepository fitTrackPointRepository;

    @Override
    public boolean checkCriteria(User user, UserActivity activity, Trophy trophy) {
        try {
            // Check if activity has GPS data
            if (activity == null || activity.getFitFileUpload() == null) {
                return false;
            }

            // Check validity window
            LocalDateTime now = activity.getActivityStartTime();
            if (trophy.getValidFrom() != null && now.isBefore(trophy.getValidFrom())) {
                return false;
            }
            if (trophy.getValidUntil() != null && now.isAfter(trophy.getValidUntil())) {
                return false;
            }

            // Use Trophy entity fields for location (backward compatibility)
            // Or parse from config if available
            Double latitude = trophy.getLatitude();
            Double longitude = trophy.getLongitude();
            Integer radiusMeters = trophy.getCollectionRadiusMeters();

            // Try to get from config if not in Trophy entity
            if ((latitude == null || longitude == null || radiusMeters == null)
                && trophy.getCriteriaConfig() != null) {
                try {
                    LocationBasedConfig config = configParser.parseConfig(
                        trophy.getCriteriaConfig(),
                        LocationBasedConfig.class
                    );
                    if (config.getLatitude() != null) latitude = config.getLatitude();
                    if (config.getLongitude() != null) longitude = config.getLongitude();
                    if (config.getCollectionRadiusMeters() != null) radiusMeters = config.getCollectionRadiusMeters();
                } catch (Exception e) {
                    log.debug("Could not parse location config for trophy {}", trophy.getCode());
                }
            }

            if (latitude == null || longitude == null || radiusMeters == null) {
                log.warn("Location trophy {} missing required coordinates", trophy.getCode());
                return false;
            }

            // Get GPS track points
            List<FitTrackPoint> trackPoints = fitTrackPointRepository.findByFitFileUploadIdWithGpsData(
                activity.getFitFileUpload().getId()
            );

            if (trackPoints.isEmpty()) {
                log.debug("No GPS track points found for activity {}", activity.getId());
                return false;
            }

            // Check if any track point is within collection radius
            for (FitTrackPoint trackPoint : trackPoints) {
                if (trackPoint.hasValidGpsPosition()) {
                    double distance = calculateDistance(
                        latitude,
                        longitude,
                        trackPoint.getPositionLat().doubleValue(),
                        trackPoint.getPositionLong().doubleValue()
                    );

                    if (distance <= radiusMeters) {
                        return true;
                    }
                }
            }

            return false;

        } catch (Exception e) {
            log.error("Error checking location-based trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public TrophyProgress calculateProgress(User user, Trophy trophy) {
        // Location trophies are binary: either collected or not
        // Progress doesn't make sense here
        return new TrophyProgress(0, 1);
    }

    @Override
    public boolean supports(Trophy.TrophyType type) {
        return type == Trophy.TrophyType.LOCATION_BASED;
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
}
