package com.mainstream.activity.service.trophy.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration for TIME_BASED trophies.
 * Example: {"startHour": 5, "endHour": 7, "requiredCount": 10, "daysOfWeek": [1,2,3,4,5]}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("TIME_BASED")
public class TimeBasedConfig implements TrophyConfig {

    /**
     * Start hour (0-23)
     */
    private Integer startHour;

    /**
     * End hour (0-23)
     */
    private Integer endHour;

    /**
     * Number of activities required in this time window
     */
    private Integer requiredCount;

    /**
     * Optional: Days of week (1=Monday, 7=Sunday)
     * If null, all days count
     */
    private List<Integer> daysOfWeek;

    /**
     * Optional: Minimum distance per activity in meters
     */
    private Integer minimumDistance;
}
