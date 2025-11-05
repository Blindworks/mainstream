package com.mainstream.activity.service;

import com.mainstream.activity.entity.PredefinedRoute;
import com.mainstream.activity.entity.RouteTrackPoint;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.activity.repository.PredefinedRouteRepository;
import com.mainstream.fitfile.entity.FitTrackPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for matching user GPS tracks against predefined routes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteMatchingService {

    private final PredefinedRouteRepository predefinedRouteRepository;
    private static final double MATCHING_TOLERANCE_METERS = 10.0; // 10m tolerance

    /**
     * Result of route matching
     */
    public static class RouteMatchResult {
        private PredefinedRoute matchedRoute;
        private double matchedDistanceMeters;
        private double routeCompletionPercentage;
        private double averageAccuracyMeters;
        private boolean isCompleteRoute;
        private UserActivity.RunDirection direction;
        private List<Integer> matchedIndices; // Indices of matched route points

        public RouteMatchResult() {
            this.matchedIndices = new ArrayList<>();
        }

        // Getters and setters
        public PredefinedRoute getMatchedRoute() { return matchedRoute; }
        public void setMatchedRoute(PredefinedRoute matchedRoute) { this.matchedRoute = matchedRoute; }

        public double getMatchedDistanceMeters() { return matchedDistanceMeters; }
        public void setMatchedDistanceMeters(double matchedDistanceMeters) { this.matchedDistanceMeters = matchedDistanceMeters; }

        public double getRouteCompletionPercentage() { return routeCompletionPercentage; }
        public void setRouteCompletionPercentage(double routeCompletionPercentage) { this.routeCompletionPercentage = routeCompletionPercentage; }

        public double getAverageAccuracyMeters() { return averageAccuracyMeters; }
        public void setAverageAccuracyMeters(double averageAccuracyMeters) { this.averageAccuracyMeters = averageAccuracyMeters; }

        public boolean isCompleteRoute() { return isCompleteRoute; }
        public void setCompleteRoute(boolean completeRoute) { isCompleteRoute = completeRoute; }

        public UserActivity.RunDirection getDirection() { return direction; }
        public void setDirection(UserActivity.RunDirection direction) { this.direction = direction; }

        public List<Integer> getMatchedIndices() { return matchedIndices; }
        public void setMatchedIndices(List<Integer> matchedIndices) { this.matchedIndices = matchedIndices; }
    }

    /**
     * Match GPS track points against all active predefined routes.
     *
     * @param trackPoints User's GPS track points from FIT file
     * @return Best matching route result, or null if no match found
     */
    public RouteMatchResult matchRoute(List<FitTrackPoint> trackPoints) {
        if (trackPoints == null || trackPoints.isEmpty()) {
            log.warn("Cannot match route: no track points provided");
            return null;
        }

        List<PredefinedRoute> activeRoutes = predefinedRouteRepository.findByIsActiveTrue();
        if (activeRoutes.isEmpty()) {
            log.warn("No active predefined routes available for matching");
            return null;
        }

        log.info("Matching {} track points against {} active routes", trackPoints.size(), activeRoutes.size());

        RouteMatchResult bestMatch = null;
        double bestMatchScore = 0.0;

        for (PredefinedRoute route : activeRoutes) {
            RouteMatchResult matchResult = matchAgainstRoute(trackPoints, route);
            if (matchResult != null) {
                double matchScore = calculateMatchScore(matchResult);
                log.debug("Route '{}' match score: {}", route.getName(), matchScore);

                if (matchScore > bestMatchScore) {
                    bestMatchScore = matchScore;
                    bestMatch = matchResult;
                }
            }
        }

        if (bestMatch != null) {
            log.info("Best match: Route '{}' with {}% completion, avg accuracy: {}m",
                     bestMatch.getMatchedRoute().getName(),
                     bestMatch.getRouteCompletionPercentage(),
                     bestMatch.getAverageAccuracyMeters());
        } else {
            log.info("No matching route found for track");
        }

        return bestMatch;
    }

    /**
     * Match track points against a specific route.
     */
    private RouteMatchResult matchAgainstRoute(List<FitTrackPoint> trackPoints, PredefinedRoute route) {
        List<RouteTrackPoint> routePoints = route.getTrackPoints();
        if (routePoints.isEmpty()) {
            return null;
        }

        RouteMatchResult result = new RouteMatchResult();
        result.setMatchedRoute(route);

        List<Integer> matchedIndices = new ArrayList<>();
        List<Double> accuracies = new ArrayList<>();
        int consecutiveMatches = 0;
        int maxConsecutiveMatches = 0;
        double matchedDistance = 0.0;

        // Try to match each user track point to the closest route point
        for (FitTrackPoint userPoint : trackPoints) {
            if (userPoint.getPositionLat() == null || userPoint.getPositionLong() == null) {
                continue;
            }

            // Find closest route point within tolerance
            int closestIndex = -1;
            double minDistance = Double.MAX_VALUE;

            for (int i = 0; i < routePoints.size(); i++) {
                RouteTrackPoint routePoint = routePoints.get(i);
                double distance = calculateDistance(
                    userPoint.getPositionLat().doubleValue(),
                    userPoint.getPositionLong().doubleValue(),
                    routePoint.getLatitude().doubleValue(),
                    routePoint.getLongitude().doubleValue()
                );

                if (distance < minDistance && distance <= MATCHING_TOLERANCE_METERS) {
                    minDistance = distance;
                    closestIndex = i;
                }
            }

            if (closestIndex >= 0) {
                matchedIndices.add(closestIndex);
                accuracies.add(minDistance);
                consecutiveMatches++;
                maxConsecutiveMatches = Math.max(maxConsecutiveMatches, consecutiveMatches);
            } else {
                consecutiveMatches = 0;
            }
        }

        // If we have too few matches, this is not a valid match
        if (matchedIndices.size() < 5 || maxConsecutiveMatches < 5) {
            return null;
        }

        // Calculate matched distance and completion percentage
        if (!matchedIndices.isEmpty()) {
            int minIndex = matchedIndices.stream().min(Integer::compareTo).orElse(0);
            int maxIndex = matchedIndices.stream().max(Integer::compareTo).orElse(0);

            if (maxIndex > minIndex) {
                BigDecimal startDist = routePoints.get(minIndex).getDistanceFromStartMeters();
                BigDecimal endDist = routePoints.get(maxIndex).getDistanceFromStartMeters();
                matchedDistance = endDist.subtract(startDist).doubleValue();
            }
        }

        double totalRouteDistance = route.getDistanceMeters().doubleValue();
        double completionPercentage = (matchedDistance / totalRouteDistance) * 100.0;

        result.setMatchedDistanceMeters(matchedDistance);
        result.setRouteCompletionPercentage(completionPercentage);
        result.setCompleteRoute(completionPercentage >= 95.0); // 95% threshold for "complete"
        result.setMatchedIndices(matchedIndices);

        // Calculate average accuracy
        double avgAccuracy = accuracies.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        result.setAverageAccuracyMeters(avgAccuracy);

        // Determine direction
        UserActivity.RunDirection direction = determineDirection(matchedIndices, routePoints.size());
        result.setDirection(direction);

        return result;
    }

    /**
     * Determine if the route was run clockwise or counter-clockwise.
     */
    private UserActivity.RunDirection determineDirection(List<Integer> matchedIndices, int totalRoutePoints) {
        if (matchedIndices.size() < 3) {
            return UserActivity.RunDirection.UNKNOWN;
        }

        // Count increasing vs decreasing index transitions
        int increasingCount = 0;
        int decreasingCount = 0;

        for (int i = 1; i < matchedIndices.size(); i++) {
            int diff = matchedIndices.get(i) - matchedIndices.get(i - 1);

            // Handle wrap-around for circular routes
            if (Math.abs(diff) > totalRoutePoints / 2) {
                diff = diff > 0 ? diff - totalRoutePoints : diff + totalRoutePoints;
            }

            if (diff > 0) {
                increasingCount++;
            } else if (diff < 0) {
                decreasingCount++;
            }
        }

        // Determine predominant direction
        if (increasingCount > decreasingCount * 2) {
            return UserActivity.RunDirection.CLOCKWISE;
        } else if (decreasingCount > increasingCount * 2) {
            return UserActivity.RunDirection.COUNTER_CLOCKWISE;
        } else {
            return UserActivity.RunDirection.UNKNOWN;
        }
    }

    /**
     * Calculate a match score for ranking multiple route matches.
     * Higher score = better match.
     */
    private double calculateMatchScore(RouteMatchResult result) {
        // Score based on:
        // - Completion percentage (0-100)
        // - Accuracy (inverse of average distance, max 10m)
        double completionScore = result.getRouteCompletionPercentage();
        double accuracyScore = (MATCHING_TOLERANCE_METERS - result.getAverageAccuracyMeters()) / MATCHING_TOLERANCE_METERS * 100.0;

        // Weighted combination: 70% completion, 30% accuracy
        return (completionScore * 0.7) + (accuracyScore * 0.3);
    }

    /**
     * Calculate distance between two GPS coordinates using Haversine formula.
     * Returns distance in meters.
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS = 6371000; // meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
