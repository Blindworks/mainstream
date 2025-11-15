package com.mainstream.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for email sending response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponseDto {

    private boolean success;

    private String message;

    private LocalDateTime sentAt;

    private String errorDetails;
}
