package com.mainstream.garmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GarminActivityDetails {
    @JsonProperty("activityId")
    private Long activityId;

    @JsonProperty("activityName")
    private String activityName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("geoPolylineDTO")
    private GeoPolylineDTO geoPolylineDTO;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoPolylineDTO {
        @JsonProperty("startPoint")
        private Point startPoint;

        @JsonProperty("endPoint")
        private Point endPoint;

        @JsonProperty("minLat")
        private Double minLat;

        @JsonProperty("maxLat")
        private Double maxLat;

        @JsonProperty("minLon")
        private Double minLon;

        @JsonProperty("maxLon")
        private Double maxLon;

        @JsonProperty("polyline")
        private List<Point> polyline;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Point {
        @JsonProperty("lat")
        private Double lat;

        @JsonProperty("lon")
        private Double lon;

        @JsonProperty("altitude")
        private Double altitude;

        @JsonProperty("time")
        private Long time;

        @JsonProperty("cumulativeAscent")
        private Double cumulativeAscent;

        @JsonProperty("cumulativeDescent")
        private Double cumulativeDescent;

        @JsonProperty("distanceFromStart")
        private Double distanceFromStart;

        @JsonProperty("distanceFromPreviousPoint")
        private Double distanceFromPreviousPoint;
    }
}
