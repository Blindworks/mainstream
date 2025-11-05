package com.mainstream.fitfile.dto;

import com.mainstream.fitfile.entity.FitFileUpload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FitFileUploadResponseDto {

    private Long id;
    private String originalFilename;
    private Long fileSize;
    private FitFileUpload.ProcessingStatus processingStatus;
    private String message;
    private String errorMessage;
}