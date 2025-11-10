package com.mainstream.activity.service.trophy.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for SPECIAL trophies.
 * Flexible configuration for various special achievements.
 *
 * Examples:
 * - Birthday run: {"specialType": "BIRTHDAY_RUN"}
 * - Performance: {"specialType": "PERFORMANCE", "distanceMeters": 10000, "maxDurationSeconds": 2700}
 * - Date-based: {"specialType": "DATE_BASED", "month": 1, "day": 1}
 * - First of type: {"specialType": "FIRST_ACTIVITY"}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("SPECIAL")
public class SpecialConfig implements TrophyConfig {

    /**
     * Type of special trophy
     */
    private String specialType;

    /**
     * For performance-based: distance in meters
     */
    private Integer distanceMeters;

    /**
     * For performance-based: maximum duration in seconds
     */
    private Integer maxDurationSeconds;

    /**
     * For date-based: month (1-12)
     */
    private Integer month;

    /**
     * For date-based: day (1-31)
     */
    private Integer day;

    /**
     * For weather-based: weather condition
     */
    private String weatherCondition;

    /**
     * Generic value field for custom special trophies
     */
    private String customValue;
}
