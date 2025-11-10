package com.mainstream.activity.service.trophy.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for EXPLORER trophies.
 * Example: {"uniqueAreasCount": 10, "gridSizeMeters": 1000}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("EXPLORER")
public class ExplorerConfig implements TrophyConfig {

    /**
     * Number of unique areas/grids to visit
     */
    private Integer uniqueAreasCount;

    /**
     * Size of grid cells in meters (e.g., 1000 = 1km x 1km grids)
     * If null, use radiusMeters instead
     */
    private Integer gridSizeMeters;

    /**
     * Alternative: Radius in meters from different start points
     * Only used if gridSizeMeters is null
     */
    private Integer radiusMeters;

    /**
     * Optional: Minimum distance per unique area
     */
    private Integer minDistancePerArea;
}
