package com.mainstream.email.controller;

import com.mainstream.email.dto.EmailRequestDto;
import com.mainstream.email.dto.EmailResponseDto;
import com.mainstream.email.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for email operations
 */
@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * Send a simple text email
     */
    @PostMapping("/send/simple")
    public ResponseEntity<EmailResponseDto> sendSimpleEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body) {

        log.info("Received request to send simple email to: {}", to);
        EmailResponseDto response = emailService.sendSimpleEmail(to, subject, body);

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Send an HTML email
     */
    @PostMapping("/send/html")
    public ResponseEntity<EmailResponseDto> sendHtmlEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String htmlBody) {

        log.info("Received request to send HTML email to: {}", to);
        EmailResponseDto response = emailService.sendHtmlEmail(to, subject, htmlBody);

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Send an email with full options (attachments, CC, BCC, etc.)
     */
    @PostMapping("/send")
    public ResponseEntity<EmailResponseDto> sendEmail(@Valid @RequestBody EmailRequestDto emailRequest) {
        log.info("Received request to send email to: {}", emailRequest.getTo());
        EmailResponseDto response = emailService.sendEmail(emailRequest);

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Send a templated email
     */
    @PostMapping("/send/template")
    public ResponseEntity<EmailResponseDto> sendTemplatedEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String templateName,
            @RequestBody(required = false) Map<String, Object> variables) {

        log.info("Received request to send templated email to: {} using template: {}", to, templateName);

        if (variables == null) {
            variables = new HashMap<>();
        }

        EmailResponseDto response = emailService.sendTemplatedEmail(to, subject, templateName, variables);

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Send a welcome email using the welcome template
     */
    @PostMapping("/send/welcome")
    public ResponseEntity<EmailResponseDto> sendWelcomeEmail(
            @RequestParam String to,
            @RequestParam String username) {

        log.info("Sending welcome email to: {}", to);

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        variables.put("loginUrl", "http://localhost:4200/login");

        EmailResponseDto response = emailService.sendTemplatedEmail(
                to,
                "Willkommen bei MainStream!",
                "welcome",
                variables
        );

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Send a password reset email using the password-reset template
     */
    @PostMapping("/send/password-reset")
    public ResponseEntity<EmailResponseDto> sendPasswordResetEmail(
            @RequestParam String to,
            @RequestParam String username,
            @RequestParam String resetToken) {

        log.info("Sending password reset email to: {}", to);

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        variables.put("resetUrl", "http://localhost:4200/reset-password?token=" + resetToken);
        variables.put("expirationHours", "24");

        EmailResponseDto response = emailService.sendTemplatedEmail(
                to,
                "Passwort zurücksetzen - MainStream",
                "password-reset",
                variables
        );

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Email Service");
        return ResponseEntity.ok(response);
    }
}
