package com.mainstream.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for email sending requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDto {

    @NotEmpty(message = "At least one recipient is required")
    private List<@Email(message = "Invalid email address") String> to;

    private List<@Email(message = "Invalid email address") String> cc;

    private List<@Email(message = "Invalid email address") String> bcc;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Email body is required")
    private String body;

    private boolean isHtml;

    private List<EmailAttachmentDto> attachments;

    private Map<String, Object> templateVariables;

    private String templateName;
}
