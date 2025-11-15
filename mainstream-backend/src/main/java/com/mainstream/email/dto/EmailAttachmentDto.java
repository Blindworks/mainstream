package com.mainstream.email.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for email attachments
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachmentDto {

    @NotBlank(message = "Filename is required")
    private String filename;

    @NotBlank(message = "Content type is required")
    private String contentType;

    private byte[] data;

    private String filePath;
}
