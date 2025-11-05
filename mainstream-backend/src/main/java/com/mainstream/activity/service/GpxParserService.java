package com.mainstream.activity.service;

import com.mainstream.activity.entity.PredefinedRoute;
import com.mainstream.activity.entity.RouteTrackPoint;
import com.mainstream.activity.repository.PredefinedRouteRepository;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for parsing GPX files and creating predefined routes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GpxParserService {

    private final PredefinedRouteRepository predefinedRouteRepository;

    /**
     * Parse a GPX file and create a predefined route.
     *
     * @param file GPX file
     * @param routeName Name for the route
     * @param description Optional description
     * @return Created PredefinedRoute
     * @throws IOException if file cannot be parsed
     */
    @Transactional
    public PredefinedRoute parseAndCreateRoute(MultipartFile file, String routeName, String description) throws IOException {
        log.info("Parsing GPX file: {} for route: {}", file.getOriginalFilename(), routeName);

        // Check if route with this name already exists
        if (predefinedRouteRepository.existsByName(routeName)) {
            throw new IllegalArgumentException("Route with name '" + routeName + "' already exists");
        }

        GPX gpx;
        try (InputStream inputStream = file.getInputStream()) {
            gpx = GPX.read(inputStream);
        }

        if (gpx.getTracks().isEmpty()) {
            throw new IllegalArgumentException("GPX file contains no tracks");
        }

        PredefinedRoute route = new PredefinedRoute();
        route.setName(routeName);
        route.setDescription(description);
        route.setOriginalFilename(file.getOriginalFilename());
        route.setIsActive(true);

        // Process first track
        Track track = gpx.getTracks().get(0);

        AtomicInteger sequenceNumber = new AtomicInteger(0);
        double totalDistance = 0.0;
        double totalElevationGain = 0.0;
        double totalElevationLoss = 0.0;
        WayPoint previousPoint = null;

        for (TrackSegment segment : track.getSegments()) {
            for (WayPoint wayPoint : segment.getPoints()) {
                RouteTrackPoint trackPoint = new RouteTrackPoint();
                trackPoint.setSequenceNumber(sequenceNumber.getAndIncrement());
                trackPoint.setLatitude(BigDecimal.valueOf(wayPoint.getLatitude().doubleValue()));
                trackPoint.setLongitude(BigDecimal.valueOf(wayPoint.getLongitude().doubleValue()));

                if (wayPoint.getElevation().isPresent()) {
                    double elevation = wayPoint.getElevation().get().doubleValue();
                    trackPoint.setElevation(BigDecimal.valueOf(elevation));

                    // Calculate elevation gain/loss
                    if (previousPoint != null && previousPoint.getElevation().isPresent()) {
                        double elevationDiff = elevation - previousPoint.getElevation().get().doubleValue();
                        if (elevationDiff > 0) {
                            totalElevationGain += elevationDiff;
                        } else {
                            totalElevationLoss += Math.abs(elevationDiff);
                        }
                    }
                }

                // Calculate distance from start
                if (previousPoint != null) {
                    double distance = calculateDistance(
                        previousPoint.getLatitude().doubleValue(),
                        previousPoint.getLongitude().doubleValue(),
                        wayPoint.getLatitude().doubleValue(),
                        wayPoint.getLongitude().doubleValue()
                    );
                    totalDistance += distance;
                }

                trackPoint.setDistanceFromStartMeters(BigDecimal.valueOf(totalDistance));

                // Set start point coordinates
                if (sequenceNumber.get() == 1) {
                    route.setStartLatitude(trackPoint.getLatitude());
                    route.setStartLongitude(trackPoint.getLongitude());
                }

                route.addTrackPoint(trackPoint);
                previousPoint = wayPoint;
            }
        }

        route.setDistanceMeters(BigDecimal.valueOf(totalDistance));
        route.setElevationGainMeters(BigDecimal.valueOf(totalElevationGain));
        route.setElevationLossMeters(BigDecimal.valueOf(totalElevationLoss));

        PredefinedRoute savedRoute = predefinedRouteRepository.save(route);
        log.info("Created predefined route: {} with {} track points, distance: {}m",
                 routeName, route.getTrackPoints().size(), totalDistance);

        return savedRoute;
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
