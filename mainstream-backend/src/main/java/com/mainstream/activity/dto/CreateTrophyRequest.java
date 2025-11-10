package com.mainstream.activity.dto;

import com.mainstream.activity.entity.Trophy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrophyRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Type is required")
    private Trophy.TrophyType type;

    @NotNull(message = "Category is required")
    private Trophy.TrophyCategory category;

    private String iconUrl;
    private Integer criteriaValue;

    @NotNull(message = "IsActive is required")
    private Boolean isActive;

    private Integer displayOrder;

    // Location-based trophy fields
    private Double latitude;
    private Double longitude;
    private Integer collectionRadiusMeters;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String imageUrl;

    // Generic configurable trophy criteria
    private String criteriaConfig;
    private Trophy.CheckScope checkScope;
}
