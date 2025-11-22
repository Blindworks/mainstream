package com.mainstream.app.data.model

import com.google.gson.annotations.SerializedName

data class Trophy(
    val id: Long,
    val code: String,
    val name: String,
    val description: String,
    val type: TrophyType,
    val category: TrophyCategory,
    val iconUrl: String? = null,
    val criteriaValue: Double? = null,
    val isActive: Boolean,
    val displayOrder: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val collectionRadiusMeters: Double? = null,
    val validFrom: String? = null,
    val validUntil: String? = null,
    val imageUrl: String? = null,
    val criteriaConfig: String? = null,
    val checkScope: CheckScope? = null,
    val createdAt: String,
    val updatedAt: String
)

data class UserTrophy(
    val id: Long,
    val userId: Long,
    val userName: String,
    val trophy: Trophy,
    val activityId: Long? = null,
    val earnedAt: String,
    val metadata: String? = null
)

data class TrophyProgress(
    val trophyId: Long,
    val currentValue: Double,
    val targetValue: Double,
    val percentage: Double,
    val isComplete: Boolean,
    val isEarned: Boolean
)

data class TrophyWithProgress(
    val trophy: Trophy,
    val isEarned: Boolean,
    val earnedAt: String? = null,
    val progress: TrophyProgress? = null
)

enum class TrophyType {
    @SerializedName("DISTANCE_MILESTONE") DISTANCE_MILESTONE,
    @SerializedName("STREAK") STREAK,
    @SerializedName("ROUTE_COMPLETION") ROUTE_COMPLETION,
    @SerializedName("CONSISTENCY") CONSISTENCY,
    @SerializedName("TIME_BASED") TIME_BASED,
    @SerializedName("EXPLORER") EXPLORER,
    @SerializedName("LOCATION_BASED") LOCATION_BASED,
    @SerializedName("SPECIAL") SPECIAL
}

enum class TrophyCategory {
    @SerializedName("BEGINNER") BEGINNER,
    @SerializedName("INTERMEDIATE") INTERMEDIATE,
    @SerializedName("ADVANCED") ADVANCED,
    @SerializedName("ELITE") ELITE,
    @SerializedName("SPECIAL") SPECIAL
}

enum class CheckScope {
    @SerializedName("SINGLE_ACTIVITY") SINGLE_ACTIVITY,
    @SerializedName("TOTAL") TOTAL,
    @SerializedName("TIME_RANGE") TIME_RANGE,
    @SerializedName("COUNT") COUNT
}

fun TrophyCategory.getDisplayName(): String = when (this) {
    TrophyCategory.BEGINNER -> "Anfänger"
    TrophyCategory.INTERMEDIATE -> "Fortgeschritten"
    TrophyCategory.ADVANCED -> "Erfahren"
    TrophyCategory.ELITE -> "Elite"
    TrophyCategory.SPECIAL -> "Spezial"
}

fun TrophyCategory.getColor(): Long = when (this) {
    TrophyCategory.BEGINNER -> 0xFF4CAF50 // Green
    TrophyCategory.INTERMEDIATE -> 0xFF2196F3 // Blue
    TrophyCategory.ADVANCED -> 0xFF9C27B0 // Purple
    TrophyCategory.ELITE -> 0xFFFF9800 // Orange
    TrophyCategory.SPECIAL -> 0xFFF44336 // Red
}
