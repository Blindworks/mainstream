package com.mainstream.garmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GarminActivity {
    @JsonProperty("activityId")
    private Long activityId;

    @JsonProperty("activityName")
    private String activityName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("activityType")
    private ActivityType activityType;

    @JsonProperty("startTimeLocal")
    private String startTimeLocal;

    @JsonProperty("startTimeGMT")
    private String startTimeGMT;

    @JsonProperty("distance")
    private Double distance; // in meters

    @JsonProperty("duration")
    private Double duration; // in seconds

    @JsonProperty("elapsedDuration")
    private Double elapsedDuration; // in seconds

    @JsonProperty("movingDuration")
    private Double movingDuration; // in seconds

    @JsonProperty("elevationGain")
    private Double elevationGain; // in meters

    @JsonProperty("elevationLoss")
    private Double elevationLoss;

    @JsonProperty("averageSpeed")
    private Double averageSpeed; // in m/s

    @JsonProperty("maxSpeed")
    private Double maxSpeed; // in m/s

    @JsonProperty("calories")
    private Double calories;

    @JsonProperty("averageHR")
    private Integer averageHR;

    @JsonProperty("maxHR")
    private Integer maxHR;

    @JsonProperty("averageRunningCadenceInStepsPerMinute")
    private Double averageRunningCadence;

    @JsonProperty("maxRunningCadenceInStepsPerMinute")
    private Double maxRunningCadence;

    @JsonProperty("steps")
    private Integer steps;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActivityType {
        @JsonProperty("typeId")
        private Integer typeId;

        @JsonProperty("typeKey")
        private String typeKey;

        @JsonProperty("parentTypeId")
        private Integer parentTypeId;

        @JsonProperty("isHidden")
        private Boolean isHidden;

        @JsonProperty("restricted")
        private Boolean restricted;

        @JsonProperty("trpimageDaytime")
        private String trpimageDaytime;

        @JsonProperty("trpimageNight")
        private String trpimageNight;
    }
}
