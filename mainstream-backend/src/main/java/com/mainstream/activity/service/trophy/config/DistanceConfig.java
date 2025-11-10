package com.mainstream.activity.service.trophy.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for DISTANCE_MILESTONE trophies.
 * Example: {"type": "DISTANCE_MILESTONE", "distanceMeters": 10000, "scope": "TOTAL"}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("DISTANCE_MILESTONE")
public class DistanceConfig implements TrophyConfig {

    /**
     * Distance in meters required to earn the trophy
     */
    private Integer distanceMeters;

    /**
     * Scope of the check:
     * - SINGLE_ACTIVITY: Distance in a single run
     * - TOTAL: Total distance across all runs
     */
    private String scope = "TOTAL";
}
