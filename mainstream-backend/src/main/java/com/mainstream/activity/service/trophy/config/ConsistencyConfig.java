package com.mainstream.activity.service.trophy.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for CONSISTENCY trophies.
 * Example: {"minActivitiesPerWeek": 3, "numberOfWeeks": 4, "minDistancePerActivity": 2000}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("CONSISTENCY")
public class ConsistencyConfig implements TrophyConfig {

    /**
     * Minimum activities per week required
     */
    private Integer minActivitiesPerWeek;

    /**
     * Number of consecutive weeks to maintain the consistency
     */
    private Integer numberOfWeeks;

    /**
     * Optional: Minimum distance per activity in meters
     */
    private Integer minDistancePerActivity;
}
