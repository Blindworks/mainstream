package com.mainstream.email.service;

import com.mainstream.email.dto.EmailAttachmentDto;
import com.mainstream.email.dto.EmailRequestDto;
import com.mainstream.email.dto.EmailResponseDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Implementation of EmailService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;

    @Override
    public EmailResponseDto sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("Simple email sent successfully to: {}", to);
            return createSuccessResponse("Simple email sent successfully");

        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            return createErrorResponse("Failed to send simple email", e.getMessage());
        }
    }

    @Override
    public EmailResponseDto sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);

            log.info("HTML email sent successfully to: {}", to);
            return createSuccessResponse("HTML email sent successfully");

        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            return createErrorResponse("Failed to send HTML email", e.getMessage());
        }
    }

    @Override
    public EmailResponseDto sendEmail(EmailRequestDto emailRequest) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            boolean hasAttachments = emailRequest.getAttachments() != null &&
                                    !emailRequest.getAttachments().isEmpty();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, hasAttachments, "UTF-8");

            // Set recipients
            helper.setTo(emailRequest.getTo().toArray(new String[0]));

            // Set CC if present
            if (emailRequest.getCc() != null && !emailRequest.getCc().isEmpty()) {
                helper.setCc(emailRequest.getCc().toArray(new String[0]));
            }

            // Set BCC if present
            if (emailRequest.getBcc() != null && !emailRequest.getBcc().isEmpty()) {
                helper.setBcc(emailRequest.getBcc().toArray(new String[0]));
            }

            // Set subject and body
            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getBody(), emailRequest.isHtml());

            // Add attachments if present
            if (hasAttachments) {
                for (EmailAttachmentDto attachment : emailRequest.getAttachments()) {
                    addAttachment(helper, attachment);
                }
            }

            mailSender.send(mimeMessage);

            log.info("Email sent successfully to: {}", emailRequest.getTo());
            return createSuccessResponse("Email sent successfully");

        } catch (Exception e) {
            log.error("Failed to send email to: {}", emailRequest.getTo(), e);
            return createErrorResponse("Failed to send email", e.getMessage());
        }
    }

    @Override
    public EmailResponseDto sendTemplatedEmail(String to, String subject, String templateName,
                                              Map<String, Object> templateVariables) {
        try {
            String processedBody = templateService.processTemplate(templateName, templateVariables);

            return sendHtmlEmail(to, subject, processedBody);

        } catch (Exception e) {
            log.error("Failed to send templated email to: {}", to, e);
            return createErrorResponse("Failed to send templated email", e.getMessage());
        }
    }

    /**
     * Add attachment to the email
     */
    private void addAttachment(MimeMessageHelper helper, EmailAttachmentDto attachment)
            throws MessagingException {
        if (attachment.getData() != null) {
            // Add attachment from byte array
            helper.addAttachment(attachment.getFilename(),
                               new ByteArrayResource(attachment.getData()));
        } else if (attachment.getFilePath() != null) {
            // Add attachment from file path
            FileSystemResource file = new FileSystemResource(new File(attachment.getFilePath()));
            helper.addAttachment(attachment.getFilename(), file);
        }
    }

    /**
     * Create success response
     */
    private EmailResponseDto createSuccessResponse(String message) {
        return EmailResponseDto.builder()
                .success(true)
                .message(message)
                .sentAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response
     */
    private EmailResponseDto createErrorResponse(String message, String errorDetails) {
        return EmailResponseDto.builder()
                .success(false)
                .message(message)
                .errorDetails(errorDetails)
                .sentAt(LocalDateTime.now())
                .build();
    }
}
