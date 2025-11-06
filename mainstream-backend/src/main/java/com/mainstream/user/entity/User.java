package com.mainstream.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Column(name = "first_name", nullable = false)
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "phone_number")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "fitness_level")
    @Enumerated(EnumType.STRING)
    private FitnessLevel fitnessLevel;

    @Column(name = "preferred_distance_unit")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DistanceUnit preferredDistanceUnit = DistanceUnit.KILOMETERS;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_public_profile")
    @Builder.Default
    private Boolean isPublicProfile = true;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    // Strava Integration Fields
    @Column(name = "strava_user_id")
    private Long stravaUserId;

    @Column(name = "strava_access_token", length = 500)
    private String stravaAccessToken;

    @Column(name = "strava_refresh_token", length = 500)
    private String stravaRefreshToken;

    @Column(name = "strava_token_expires_at")
    private LocalDateTime stravaTokenExpiresAt;

    @Column(name = "strava_connected_at")
    private LocalDateTime stravaConnectedAt;

    // Nike Run Club Integration Fields
    @Column(name = "nike_user_id", length = 100)
    private String nikeUserId;

    @Column(name = "nike_access_token", length = 1000)
    private String nikeAccessToken;

    @Column(name = "nike_connected_at")
    private LocalDateTime nikeConnectedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    public enum FitnessLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public enum DistanceUnit {
        KILOMETERS, MILES
    }

    public enum Role {
        USER, ADMIN, MODERATOR
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isModerator() {
        return role == Role.MODERATOR || role == Role.ADMIN;
    }

    public boolean isStravaConnected() {
        return stravaAccessToken != null && stravaUserId != null;
    }

    public boolean isNikeConnected() {
        return nikeAccessToken != null && nikeUserId != null;
    }
}