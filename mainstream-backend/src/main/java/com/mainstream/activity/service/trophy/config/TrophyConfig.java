package com.mainstream.activity.service.trophy.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base interface for trophy configuration objects.
 * Each trophy type has its own configuration class.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DistanceConfig.class, name = "DISTANCE_MILESTONE"),
        @JsonSubTypes.Type(value = StreakConfig.class, name = "STREAK"),
        @JsonSubTypes.Type(value = TimeBasedConfig.class, name = "TIME_BASED"),
        @JsonSubTypes.Type(value = ConsistencyConfig.class, name = "CONSISTENCY"),
        @JsonSubTypes.Type(value = RouteCompletionConfig.class, name = "ROUTE_COMPLETION"),
        @JsonSubTypes.Type(value = ExplorerConfig.class, name = "EXPLORER"),
        @JsonSubTypes.Type(value = LocationBasedConfig.class, name = "LOCATION_BASED"),
        @JsonSubTypes.Type(value = SpecialConfig.class, name = "SPECIAL")
})
public interface TrophyConfig {
    // Marker interface for JSON serialization
}
