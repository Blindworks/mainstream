package com.mainstream.activity.service.trophy.checker;

import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.repository.UserActivityRepository;
import com.mainstream.activity.service.trophy.TrophyChecker;
import com.mainstream.activity.service.trophy.TrophyConfigParser;
import com.mainstream.activity.service.trophy.TrophyProgress;
import com.mainstream.activity.service.trophy.config.ExplorerConfig;
import com.mainstream.fitfile.entity.FitTrackPoint;
import com.mainstream.fitfile.repository.FitTrackPointRepository;
import com.mainstream.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checker for EXPLORER trophies.
 * Awards trophies for exploring different geographical areas.
 * Uses grid-based or radius-based area tracking.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExplorerChecker implements TrophyChecker {

    private final TrophyConfigParser configParser;
    private final UserActivityRepository userActivityRepository;
    private final FitTrackPointRepository fitTrackPointRepository;

    @Override
    public boolean checkCriteria(User user, UserActivity activity, Trophy trophy) {
        try {
            ExplorerConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), ExplorerConfig.class);

            if (config.getUniqueAreasCount() == null) {
                log.warn("Explorer trophy {} has no uniqueAreasCount configured", trophy.getCode());
                return false;
            }

            int uniqueAreas = countUniqueAreas(user, config);
            return uniqueAreas >= config.getUniqueAreasCount();

        } catch (Exception e) {
            log.error("Error checking explorer trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public TrophyProgress calculateProgress(User user, Trophy trophy) {
        try {
            ExplorerConfig config = configParser.parseConfig(trophy.getCriteriaConfig(), ExplorerConfig.class);

            if (config.getUniqueAreasCount() == null) {
                return new TrophyProgress(0, 0);
            }

            int uniqueAreas = countUniqueAreas(user, config);
            return new TrophyProgress(uniqueAreas, config.getUniqueAreasCount());

        } catch (Exception e) {
            log.error("Error calculating progress for trophy {}: {}", trophy.getCode(), e.getMessage(), e);
            return new TrophyProgress(0, 0);
        }
    }

    @Override
    public boolean supports(Trophy.TrophyType type) {
        return type == Trophy.TrophyType.EXPLORER;
    }

    /**
     * Count unique geographical areas visited by the user.
     * Uses either grid-based or radius-based approach.
     */
    private int countUniqueAreas(User user, ExplorerConfig config) {
        // Get all user activities with GPS data
        LocalDateTime lookbackDate = LocalDateTime.now().minusYears(1);
        List<UserActivity> activities = userActivityRepository.findUserActivitiesSince(user.getId(), lookbackDate);

        // Filter by minimum distance if specified
        if (config.getMinDistancePerArea() != null && config.getMinDistancePerArea() > 0) {
            activities = activities.stream()
                .filter(act -> act.getDistanceMeters() != null
                    && act.getDistanceMeters() >= config.getMinDistancePerArea())
                .toList();
        }

        Set<String> uniqueAreas = new HashSet<>();

        for (UserActivity activity : activities) {
            if (activity.getFitFileUpload() == null) {
                continue;
            }

            // Get GPS track points
            List<FitTrackPoint> trackPoints = fitTrackPointRepository.findByFitFileUploadIdWithGpsData(
                activity.getFitFileUpload().getId()
            );

            if (trackPoints.isEmpty()) {
                continue;
            }

            // Use grid-based or radius-based approach
            if (config.getGridSizeMeters() != null && config.getGridSizeMeters() > 0) {
                // Grid-based: divide world into grids and track which ones were visited
                uniqueAreas.addAll(getGridCellsForTrack(trackPoints, config.getGridSizeMeters()));
            } else if (config.getRadiusMeters() != null && config.getRadiusMeters() > 0) {
                // Radius-based: cluster start points by radius
                uniqueAreas.addAll(getRadiusAreasForTrack(trackPoints, config.getRadiusMeters(), uniqueAreas));
            } else {
                // Default: use activity start point with 1km grid
                uniqueAreas.addAll(getGridCellsForTrack(trackPoints, 1000));
            }
        }

        return uniqueAreas.size();
    }

    /**
     * Get grid cells visited in a GPS track.
     * Divides the world into grid cells and identifies which ones were visited.
     */
    private Set<String> getGridCellsForTrack(List<FitTrackPoint> trackPoints, int gridSizeMeters) {
        Set<String> gridCells = new HashSet<>();

        // Convert grid size to approximate degrees
        // At equator: 1 degree ≈ 111km
        // This is a simplification - proper implementation would use proper projection
        double gridSizeDegrees = gridSizeMeters / 111000.0;

        for (FitTrackPoint point : trackPoints) {
            if (point.hasValidGpsPosition()) {
                double lat = point.getPositionLat().doubleValue();
                double lon = point.getPositionLong().doubleValue();

                // Calculate grid cell coordinates
                int cellLat = (int) Math.floor(lat / gridSizeDegrees);
                int cellLon = (int) Math.floor(lon / gridSizeDegrees);

                String gridCell = cellLat + "," + cellLon;
                gridCells.add(gridCell);
            }
        }

        return gridCells;
    }

    /**
     * Get unique areas based on radius clustering.
     * Groups nearby start points into the same area.
     */
    private Set<String> getRadiusAreasForTrack(List<FitTrackPoint> trackPoints, int radiusMeters, Set<String> existingAreas) {
        Set<String> newAreas = new HashSet<>();

        if (trackPoints.isEmpty() || !trackPoints.get(0).hasValidGpsPosition()) {
            return newAreas;
        }

        // Use first track point as representative for this activity
        FitTrackPoint startPoint = trackPoints.get(0);
        double startLat = startPoint.getPositionLat().doubleValue();
        double startLon = startPoint.getPositionLong().doubleValue();

        // Check if this start point is within radius of any existing area
        boolean foundExistingArea = false;
        for (String existingArea : existingAreas) {
            String[] parts = existingArea.split(",");
            if (parts.length == 2) {
                try {
                    double existingLat = Double.parseDouble(parts[0]);
                    double existingLon = Double.parseDouble(parts[1]);

                    double distance = calculateDistance(startLat, startLon, existingLat, existingLon);
                    if (distance <= radiusMeters) {
                        foundExistingArea = true;
                        break;
                    }
                } catch (NumberFormatException e) {
                    log.debug("Invalid area format: {}", existingArea);
                }
            }
        }

        // If not near any existing area, this is a new area
        if (!foundExistingArea) {
            String newArea = String.format("%.6f,%.6f", startLat, startLon);
            newAreas.add(newArea);
        }

        return newAreas;
    }

    /**
     * Calculate distance between two GPS coordinates using Haversine formula.
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
