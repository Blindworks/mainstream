package com.mainstream.email.service;

import com.mainstream.email.dto.EmailRequestDto;
import com.mainstream.email.dto.EmailResponseDto;

/**
 * Service interface for email operations
 */
public interface EmailService {

    /**
     * Send a simple email
     *
     * @param to recipient email address
     * @param subject email subject
     * @param body email body
     * @return EmailResponseDto with sending result
     */
    EmailResponseDto sendSimpleEmail(String to, String subject, String body);

    /**
     * Send an HTML email
     *
     * @param to recipient email address
     * @param subject email subject
     * @param htmlBody HTML email body
     * @return EmailResponseDto with sending result
     */
    EmailResponseDto sendHtmlEmail(String to, String subject, String htmlBody);

    /**
     * Send an email based on EmailRequestDto
     *
     * @param emailRequest the email request containing all email details
     * @return EmailResponseDto with sending result
     */
    EmailResponseDto sendEmail(EmailRequestDto emailRequest);

    /**
     * Send an email using a template
     *
     * @param to recipient email address
     * @param subject email subject
     * @param templateName name of the template
     * @param templateVariables variables to be used in the template
     * @return EmailResponseDto with sending result
     */
    EmailResponseDto sendTemplatedEmail(String to, String subject, String templateName,
                                       java.util.Map<String, Object> templateVariables);
}
