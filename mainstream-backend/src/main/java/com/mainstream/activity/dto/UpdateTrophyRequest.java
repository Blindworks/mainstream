package com.mainstream.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrophyRequest {
    private String name;
    private String description;
    private String iconUrl;
    private Integer criteriaValue;
    private Boolean isActive;
    private Integer displayOrder;

    // Location-based trophy fields
    private Double latitude;
    private Double longitude;
    private Integer collectionRadiusMeters;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String imageUrl;
}
