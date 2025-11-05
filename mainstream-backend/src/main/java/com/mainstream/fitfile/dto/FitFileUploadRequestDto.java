package com.mainstream.fitfile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FitFileUploadRequestDto {

    private String description;
    private Boolean isPublic;
    private String tags;
}