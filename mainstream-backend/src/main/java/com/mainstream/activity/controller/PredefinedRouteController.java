package com.mainstream.activity.controller;

import com.mainstream.activity.dto.PredefinedRouteDto;
import com.mainstream.activity.dto.RouteTrackPointDto;
import com.mainstream.activity.entity.PredefinedRoute;
import com.mainstream.activity.entity.RouteTrackPoint;
import com.mainstream.activity.repository.PredefinedRouteRepository;
import com.mainstream.activity.service.GpxParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing predefined routes.
 * Admin-only endpoints for uploading GPX files.
 */
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Slf4j
public class PredefinedRouteController {

    private final GpxParserService gpxParserService;
    private final PredefinedRouteRepository predefinedRouteRepository;

    /**
     * Upload a GPX file to create a predefined route (Admin only).
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadGpxRoute(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".gpx")) {
                return ResponseEntity.badRequest().body("File must be a GPX file");
            }

            PredefinedRoute route = gpxParserService.parseAndCreateRoute(file, name, description);
            PredefinedRouteDto dto = toDto(route);

            log.info("Successfully uploaded GPX route: {}", name);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);

        } catch (IllegalArgumentException e) {
            log.error("Invalid GPX file or route name: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading GPX route", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading route: " + e.getMessage());
        }
    }

    /**
     * Get all predefined routes.
     */
    @GetMapping
    public ResponseEntity<List<PredefinedRouteDto>> getAllRoutes(
            @RequestParam(value = "activeOnly", defaultValue = "true") boolean activeOnly) {

        List<PredefinedRoute> routes = activeOnly
                ? predefinedRouteRepository.findByIsActiveTrueWithTrackPoints()
                : predefinedRouteRepository.findAllWithTrackPoints();

        List<PredefinedRouteDto> dtos = routes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get a specific route by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PredefinedRouteDto> getRouteById(@PathVariable Long id) {
        return predefinedRouteRepository.findByIdWithTrackPoints(id)
                .map(route -> ResponseEntity.ok(toDtoWithTrackPoints(route)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deactivate a route (Admin only).
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PredefinedRouteDto> deactivateRoute(@PathVariable Long id) {
        return predefinedRouteRepository.findByIdWithTrackPoints(id)
                .map(route -> {
                    route.setIsActive(false);
                    PredefinedRoute updated = predefinedRouteRepository.save(route);
                    return ResponseEntity.ok(toDto(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Activate a route (Admin only).
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PredefinedRouteDto> activateRoute(@PathVariable Long id) {
        return predefinedRouteRepository.findByIdWithTrackPoints(id)
                .map(route -> {
                    route.setIsActive(true);
                    PredefinedRoute updated = predefinedRouteRepository.save(route);
                    return ResponseEntity.ok(toDto(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Convert entity to DTO (without trackPoints for list views).
     */
    private PredefinedRouteDto toDto(PredefinedRoute route) {
        return PredefinedRouteDto.builder()
                .id(route.getId())
                .name(route.getName())
                .description(route.getDescription())
                .originalFilename(route.getOriginalFilename())
                .distanceMeters(route.getDistanceMeters())
                .elevationGainMeters(route.getElevationGainMeters())
                .elevationLossMeters(route.getElevationLossMeters())
                .startLatitude(route.getStartLatitude())
                .startLongitude(route.getStartLongitude())
                .isActive(route.getIsActive())
                .trackPointCount(route.getTrackPoints() != null ? route.getTrackPoints().size() : 0)
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .build();
    }

    /**
     * Convert entity to DTO with trackPoints (for detail views).
     */
    private PredefinedRouteDto toDtoWithTrackPoints(PredefinedRoute route) {
        List<RouteTrackPointDto> trackPointDtos = route.getTrackPoints() != null
                ? route.getTrackPoints().stream()
                    .map(this::toTrackPointDto)
                    .collect(Collectors.toList())
                : null;

        return PredefinedRouteDto.builder()
                .id(route.getId())
                .name(route.getName())
                .description(route.getDescription())
                .originalFilename(route.getOriginalFilename())
                .distanceMeters(route.getDistanceMeters())
                .elevationGainMeters(route.getElevationGainMeters())
                .elevationLossMeters(route.getElevationLossMeters())
                .startLatitude(route.getStartLatitude())
                .startLongitude(route.getStartLongitude())
                .isActive(route.getIsActive())
                .trackPointCount(route.getTrackPoints() != null ? route.getTrackPoints().size() : 0)
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .trackPoints(trackPointDtos)
                .build();
    }

    /**
     * Convert RouteTrackPoint entity to DTO.
     */
    private RouteTrackPointDto toTrackPointDto(RouteTrackPoint trackPoint) {
        return RouteTrackPointDto.builder()
                .id(trackPoint.getId())
                .sequenceNumber(trackPoint.getSequenceNumber())
                .latitude(trackPoint.getLatitude())
                .longitude(trackPoint.getLongitude())
                .elevation(trackPoint.getElevation())
                .distanceFromStartMeters(trackPoint.getDistanceFromStartMeters())
                .build();
    }
}
