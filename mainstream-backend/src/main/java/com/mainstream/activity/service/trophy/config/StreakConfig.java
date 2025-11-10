package com.mainstream.activity.service.trophy.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for STREAK trophies.
 * Example: {"consecutiveDays": 7, "minimumDistancePerDay": 1000}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("STREAK")
public class StreakConfig implements TrophyConfig {

    /**
     * Number of consecutive days required
     */
    private Integer consecutiveDays;

    /**
     * Optional: Minimum distance per day in meters
     * If null, any activity counts
     */
    private Integer minimumDistancePerDay;
}
