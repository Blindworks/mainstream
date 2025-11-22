package com.mainstream.app.data.model

data class PredefinedRoute(
    val id: Long,
    val name: String,
    val description: String? = null,
    val city: String? = null,
    val imageUrl: String? = null,
    val originalFilename: String,
    val distanceMeters: Double,
    val elevationGainMeters: Double? = null,
    val elevationLossMeters: Double? = null,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val isActive: Boolean,
    val trackPointCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val trackPoints: List<RouteTrackPoint>? = null,
    val stats: RouteStats? = null
) {
    val distanceKm: Double
        get() = distanceMeters / 1000.0

    val formattedDistance: String
        get() = "%.2f km".format(distanceKm)
}

data class RouteTrackPoint(
    val id: Long,
    val sequenceNumber: Int,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null,
    val distanceFromStartMeters: Double
)

data class RouteStats(
    val routeId: Long,
    val todayCount: Int,
    val thisWeekCount: Int,
    val thisMonthCount: Int,
    val thisYearCount: Int,
    val totalCount: Int
)
