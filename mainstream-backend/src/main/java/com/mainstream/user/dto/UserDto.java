package com.mainstream.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mainstream.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    private User.Gender gender;
    private String phoneNumber;
    private String profilePictureUrl;
    private String bio;
    private User.FitnessLevel fitnessLevel;
    private User.DistanceUnit preferredDistanceUnit;
    private Boolean isPublicProfile;
    private User.Role role;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;
}