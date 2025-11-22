package com.mainstream.app.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val type: String = "Bearer",
    val user: User,
    val expiresIn: Long
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String? = null,
    val gender: Gender? = null,
    val phoneNumber: String? = null,
    val bio: String? = null,
    val city: String? = null,
    val fitnessLevel: FitnessLevel? = null,
    val preferredDistanceUnit: DistanceUnit? = null,
    val isPublicProfile: Boolean? = null
)
