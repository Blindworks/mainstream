import Foundation

// MARK: - Predefined Route Model
struct PredefinedRoute: Codable, Identifiable {
    let id: Int
    let name: String
    let description: String?
    let city: String?
    let imageUrl: String?
    let originalFilename: String
    let distanceMeters: Double
    let elevationGainMeters: Double?
    let elevationLossMeters: Double?
    let startLatitude: Double?
    let startLongitude: Double?
    let isActive: Bool
    let trackPointCount: Int
    let createdAt: String
    let updatedAt: String
    let trackPoints: [RouteTrackPoint]?
    let stats: RouteStats?

    var distanceKm: Double {
        distanceMeters / 1000
    }

    var formattedDistance: String {
        String(format: "%.2f km", distanceKm)
    }

    var formattedElevationGain: String {
        guard let elevation = elevationGainMeters else { return "N/A" }
        return String(format: "%.0f m", elevation)
    }

    var formattedElevationLoss: String {
        guard let elevation = elevationLossMeters else { return "N/A" }
        return String(format: "%.0f m", elevation)
    }
}

// MARK: - Route Track Point
struct RouteTrackPoint: Codable, Identifiable {
    let id: Int
    let sequenceNumber: Int
    let latitude: Double
    let longitude: Double
    let elevation: Double?
    let distanceFromStartMeters: Double
}

// MARK: - Route Stats
struct RouteStats: Codable {
    let routeId: Int
    let todayCount: Int
    let thisWeekCount: Int
    let thisMonthCount: Int
    let thisYearCount: Int
    let totalCount: Int
}
