package com.mainstream.nike.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NikeActivity {

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type; // "run"

    @JsonProperty("start_epoch_ms")
    private Long startEpochMs;

    @JsonProperty("end_epoch_ms")
    private Long endEpochMs;

    @JsonProperty("active_duration_ms")
    private Long activeDurationMs;

    @JsonProperty("tags")
    private NikeTags tags;

    @JsonProperty("summaries")
    private List<NikeSummary> summaries;

    @JsonProperty("metrics")
    private List<NikeMetric> metrics;

    @JsonProperty("metric_types")
    private List<String> metricTypes;

    @JsonProperty("moments")
    private List<NikeMoment> moments;

    @Data
    public static class NikeTags {
        @JsonProperty("com.nike.name")
        private String name;

        @JsonProperty("com.nike.weather")
        private String weather;

        @JsonProperty("com.nike.temperature")
        private String temperature;
    }

    @Data
    public static class NikeSummary {
        @JsonProperty("metric")
        private String metric; // "distance", "calories", etc.

        @JsonProperty("summary")
        private String summary;

        @JsonProperty("value")
        private Double value;
    }

    @Data
    public static class NikeMetric {
        @JsonProperty("type")
        private String type; // "distance", "pace", "heart_rate", "latitude", "longitude", "elevation", etc.

        @JsonProperty("unit")
        private String unit; // "KM", "MIN_PER_KM", "BPM", "DEGREES", "METERS"

        @JsonProperty("values")
        private List<Double> values;

        @JsonProperty("interval_metric")
        private Integer intervalMetric;

        @JsonProperty("interval_unit")
        private String intervalUnit;
    }

    @Data
    public static class NikeMoment {
        @JsonProperty("start_epoch_ms")
        private Long startEpochMs;

        @JsonProperty("type")
        private String type;

        @JsonProperty("subType")
        private String subType;
    }
}
