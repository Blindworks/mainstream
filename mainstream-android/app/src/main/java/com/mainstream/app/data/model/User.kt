package com.mainstream.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long,
    val email: String,
    val username: String? = null,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String? = null,
    val gender: Gender? = null,
    val phoneNumber: String? = null,
    val profilePictureUrl: String? = null,
    val bio: String? = null,
    val city: String? = null,
    val fitnessLevel: FitnessLevel? = null,
    val preferredDistanceUnit: DistanceUnit? = null,
    val isPublicProfile: Boolean? = null,
    val role: Role,
    val createdAt: String,
    val updatedAt: String
) {
    val fullName: String
        get() = "$firstName $lastName"
}

enum class Gender {
    @SerializedName("MALE") MALE,
    @SerializedName("FEMALE") FEMALE,
    @SerializedName("OTHER") OTHER,
    @SerializedName("PREFER_NOT_TO_SAY") PREFER_NOT_TO_SAY
}

enum class FitnessLevel {
    @SerializedName("BEGINNER") BEGINNER,
    @SerializedName("INTERMEDIATE") INTERMEDIATE,
    @SerializedName("ADVANCED") ADVANCED,
    @SerializedName("EXPERT") EXPERT
}

enum class DistanceUnit {
    @SerializedName("KILOMETERS") KILOMETERS,
    @SerializedName("MILES") MILES
}

enum class Role {
    @SerializedName("USER") USER,
    @SerializedName("ADMIN") ADMIN,
    @SerializedName("MODERATOR") MODERATOR
}
