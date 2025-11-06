package com.mainstream.strava.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class StravaActivity {
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("distance")
    private Double distance; // in meters

    @JsonProperty("moving_time")
    private Integer movingTime; // in seconds

    @JsonProperty("elapsed_time")
    private Integer elapsedTime; // in seconds

    @JsonProperty("total_elevation_gain")
    private Double totalElevationGain; // in meters

    @JsonProperty("type")
    private String type; // "Run", "Walk", etc.

    @JsonProperty("start_date")
    private ZonedDateTime startDate;

    @JsonProperty("start_date_local")
    private ZonedDateTime startDateLocal;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("average_speed")
    private Double averageSpeed; // in meters/second

    @JsonProperty("max_speed")
    private Double maxSpeed; // in meters/second

    @JsonProperty("calories")
    private Double calories;

    @JsonProperty("has_heartrate")
    private Boolean hasHeartrate;

    @JsonProperty("average_heartrate")
    private Double averageHeartrate;

    @JsonProperty("max_heartrate")
    private Double maxHeartrate;

    @JsonProperty("description")
    private String description;

    @JsonProperty("manual")
    private Boolean manual;

    @JsonProperty("elev_high")
    private Double elevHigh;

    @JsonProperty("elev_low")
    private Double elevLow;
}
