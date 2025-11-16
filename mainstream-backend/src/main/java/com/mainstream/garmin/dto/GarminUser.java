package com.mainstream.garmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GarminUser {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("fullName")
    private String fullName;

    @JsonProperty("profileImageUrl")
    private String profileImageUrl;

    @JsonProperty("location")
    private String location;
}
