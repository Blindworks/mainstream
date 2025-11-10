package com.mainstream.activity.service.trophy.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Configuration for LOCATION_BASED trophies.
 * Example: {"latitude": 51.5074, "longitude": -0.1278, "collectionRadiusMeters": 100}
 *
 * Note: This largely duplicates the Trophy entity fields but is included
 * for consistency and future extensibility.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("LOCATION_BASED")
public class LocationBasedConfig implements TrophyConfig {

    /**
     * Latitude of the trophy location
     */
    private Double latitude;

    /**
     * Longitude of the trophy location
     */
    private Double longitude;

    /**
     * Collection radius in meters
     */
    private Integer collectionRadiusMeters;

    /**
     * Optional: Name/description of the location
     */
    private String locationName;
}
