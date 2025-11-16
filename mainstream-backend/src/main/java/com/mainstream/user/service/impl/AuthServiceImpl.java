package com.mainstream.user.service.impl;

import com.mainstream.email.service.EmailService;
import com.mainstream.user.dto.AuthResponseDto;
import com.mainstream.user.dto.ForgotPasswordRequestDto;
import com.mainstream.user.dto.LoginDto;
import com.mainstream.user.dto.MessageResponseDto;
import com.mainstream.user.dto.ResetPasswordRequestDto;
import com.mainstream.user.dto.UserDto;
import com.mainstream.user.entity.PasswordResetToken;
import com.mainstream.user.entity.User;
import com.mainstream.user.exception.InvalidCredentialsException;
import com.mainstream.user.mapper.UserMapper;
import com.mainstream.user.repository.PasswordResetTokenRepository;
import com.mainstream.user.repository.UserRepository;
import com.mainstream.user.service.AuthService;
import com.mainstream.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${mainstream.password-reset.frontend-url}")
    private String frontendUrl;

    @Value("${mainstream.password-reset.expiration-hours}")
    private int expirationHours;

    @Value("${mainstream.password-reset.max-attempts-per-hour}")
    private int maxAttemptsPerHour;

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public AuthResponseDto authenticate(LoginDto loginDto) {
        log.debug("Authenticating user with email: {}", loginDto.getEmail());

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        UserDto userDto = userMapper.toDto(user);
        String token = jwtUtil.generateToken(userDto);
        Long expiresIn = jwtUtil.getExpirationTime();

        log.info("User authenticated successfully: {}", loginDto.getEmail());

        return AuthResponseDto.builder()
                .token(token)
                .user(userDto)
                .expiresIn(expiresIn)
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public String extractEmailFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }

    @Override
    @Transactional
    public MessageResponseDto forgotPassword(ForgotPasswordRequestDto request) {
        log.debug("Processing forgot password request for email: {}", request.getEmail());

        // Always return success message to prevent email enumeration
        String successMessage = "Falls ein Konto mit dieser E-Mail-Adresse existiert, wurde ein Link zum Zurücksetzen des Passworts gesendet.";

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            log.debug("No user found for email: {}", request.getEmail());
            return MessageResponseDto.success(successMessage);
        }

        User user = userOptional.get();

        if (!user.getIsActive()) {
            log.debug("User account is deactivated: {}", request.getEmail());
            return MessageResponseDto.success(successMessage);
        }

        // Check rate limiting
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentAttempts = passwordResetTokenRepository.countRecentTokensByUser(user, oneHourAgo);

        if (recentAttempts >= maxAttemptsPerHour) {
            log.warn("Rate limit exceeded for password reset: {}", request.getEmail());
            return MessageResponseDto.success(successMessage);
        }

        // Invalidate all previous tokens for this user
        passwordResetTokenRepository.invalidateAllTokensForUser(user, LocalDateTime.now());

        // Generate new token
        String plainToken = generateSecureToken();
        String tokenHash = hashToken(plainToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(expirationHours))
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send email
        sendPasswordResetEmail(user, plainToken);

        log.info("Password reset token created for user: {}", request.getEmail());

        return MessageResponseDto.success(successMessage);
    }

    @Override
    @Transactional
    public MessageResponseDto resetPassword(ResetPasswordRequestDto request) {
        log.debug("Processing password reset request");

        String tokenHash = hashToken(request.getToken());

        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByTokenHash(tokenHash);

        if (tokenOptional.isEmpty()) {
            log.warn("Invalid password reset token");
            return MessageResponseDto.error("Ungültiger oder abgelaufener Token.");
        }

        PasswordResetToken resetToken = tokenOptional.get();

        if (!resetToken.isValid()) {
            log.warn("Password reset token is expired or already used");
            return MessageResponseDto.error("Ungültiger oder abgelaufener Token.");
        }

        User user = resetToken.getUser();

        if (!user.getIsActive()) {
            log.warn("User account is deactivated for password reset");
            return MessageResponseDto.error("Das Konto ist deaktiviert.");
        }

        // Update password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        log.info("Password successfully reset for user: {}", user.getEmail());

        return MessageResponseDto.success("Dein Passwort wurde erfolgreich zurückgesetzt.");
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private void sendPasswordResetEmail(User user, String token) {
        String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("username", user.getFirstName());
        templateVariables.put("resetUrl", resetUrl);
        templateVariables.put("expirationHours", String.valueOf(expirationHours));

        emailService.sendTemplatedEmail(
                user.getEmail(),
                "Passwort zurücksetzen - MainStream",
                "password-reset",
                templateVariables
        );

        log.debug("Password reset email sent to: {}", user.getEmail());
    }
}