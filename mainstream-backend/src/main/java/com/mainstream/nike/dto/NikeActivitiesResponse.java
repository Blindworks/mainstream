package com.mainstream.nike.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NikeActivitiesResponse {

    @JsonProperty("activities")
    private List<NikeActivitySummary> activities;

    @JsonProperty("paging")
    private NikePaging paging;

    @Data
    public static class NikeActivitySummary {
        @JsonProperty("id")
        private String id;

        @JsonProperty("type")
        private String type;

        @JsonProperty("start_epoch_ms")
        private Long startEpochMs;

        @JsonProperty("active_duration_ms")
        private Long activeDurationMs;
    }

    @Data
    public static class NikePaging {
        @JsonProperty("after_time")
        private Long afterTime;

        @JsonProperty("after_id")
        private String afterId;
    }
}
