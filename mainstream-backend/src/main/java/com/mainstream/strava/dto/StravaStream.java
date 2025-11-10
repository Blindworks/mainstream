package com.mainstream.strava.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for Strava activity stream data
 * See: https://developers.strava.com/docs/reference/#api-Streams-getActivityStreams
 */
@Data
public class StravaStream {

    @JsonProperty("type")
    private String type; // "latlng", "altitude", "time", "distance", etc.

    @JsonProperty("data")
    private List<Object> data; // Can be List<List<Double>> for latlng, List<Double> for altitude/distance, List<Integer> for time

    @JsonProperty("series_type")
    private String seriesType; // "distance" or "time"

    @JsonProperty("original_size")
    private Integer originalSize;

    @JsonProperty("resolution")
    private String resolution; // "low", "medium", "high"

    /**
     * Helper method to get latlng data as List<List<Double>>
     */
    @SuppressWarnings("unchecked")
    public List<List<Double>> getLatLngData() {
        if ("latlng".equals(type) && data != null) {
            return (List<List<Double>>) (List<?>) data;
        }
        return null;
    }

    /**
     * Helper method to get numeric data (altitude, distance, etc.) as List<Double>
     */
    @SuppressWarnings("unchecked")
    public List<Double> getNumericData() {
        if (data != null && !"latlng".equals(type)) {
            return data.stream()
                .map(obj -> obj instanceof Number ? ((Number) obj).doubleValue() : null)
                .toList();
        }
        return null;
    }

    /**
     * Helper method to get time data as List<Integer>
     */
    @SuppressWarnings("unchecked")
    public List<Integer> getTimeData() {
        if ("time".equals(type) && data != null) {
            return data.stream()
                .map(obj -> obj instanceof Number ? ((Number) obj).intValue() : null)
                .toList();
        }
        return null;
    }
}
