package com.mainstream.activity.service.trophy.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for ROUTE_COMPLETION trophies.
 * Example: {"routeId": 123} or {"uniqueRoutesCount": 5}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("ROUTE_COMPLETION")
public class RouteCompletionConfig implements TrophyConfig {

    /**
     * Specific route ID to complete (for single route trophy)
     * If null, use uniqueRoutesCount instead
     */
    private Long routeId;

    /**
     * Number of unique routes to complete (for explorer-style trophy)
     * Only used if routeId is null
     */
    private Integer uniqueRoutesCount;

    /**
     * Optional: Minimum match percentage required (0-100)
     * Default: 80% match
     */
    private Integer minMatchPercentage = 80;
}
