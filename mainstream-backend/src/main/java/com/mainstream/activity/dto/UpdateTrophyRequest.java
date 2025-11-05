package com.mainstream.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
