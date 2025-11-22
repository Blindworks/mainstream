import Foundation

// MARK: - Run Model
struct Run: Codable, Identifiable {
    let id: Int
    let userId: Int
    let title: String
    let description: String?
    let startTime: String
    let endTime: String?
    let durationSeconds: Int?
    let distanceMeters: Double?
    let averagePaceSecondsPerKm: Double?
    let maxSpeedKmh: Double?
    let averageSpeedKmh: Double?
    let caloriesBurned: Int?
    let elevationGainMeters: Double?
    let elevationLossMeters: Double?
    let runType: RunType
    let status: RunStatus
    let weatherCondition: String?
    let temperatureCelsius: Double?
    let humidityPercentage: Int?
    let isPublic: Bool
    let routeId: Int?
    let userActivity: UserActivity?
    let createdAt: String
    let updatedAt: String

    var distanceKm: Double? {
        guard let meters = distanceMeters else { return nil }
        return meters / 1000
    }

    var formattedDistance: String {
        guard let km = distanceKm else { return "0.00 km" }
        return String(format: "%.2f km", km)
    }

    var formattedDuration: String {
        guard let seconds = durationSeconds else { return "00:00:00" }
        let hours = seconds / 3600
        let minutes = (seconds % 3600) / 60
        let secs = seconds % 60
        return String(format: "%02d:%02d:%02d", hours, minutes, secs)
    }

    var formattedPace: String {
        guard let pace = averagePaceSecondsPerKm, pace > 0 else { return "--:--" }
        let minutes = Int(pace) / 60
        let seconds = Int(pace) % 60
        return String(format: "%d:%02d", minutes, seconds)
    }
}

// MARK: - Run Summary
struct RunSummary: Codable, Identifiable {
    let id: Int
    let title: String
    let startTime: String
    let durationSeconds: Int?
    let distanceKm: Double?
    let averagePace: String?
    let runType: RunType
    let status: RunStatus
    let caloriesBurned: Int?

    var formattedDistance: String {
        guard let km = distanceKm else { return "0.00 km" }
        return String(format: "%.2f km", km)
    }

    var formattedDuration: String {
        guard let seconds = durationSeconds else { return "00:00:00" }
        let hours = seconds / 3600
        let minutes = (seconds % 3600) / 60
        let secs = seconds % 60
        return String(format: "%02d:%02d:%02d", hours, minutes, secs)
    }

    var date: Date? {
        ISO8601DateFormatter().date(from: startTime)
    }
}

// MARK: - Paginated Run Response
struct PaginatedRunResponse: Codable {
    let content: [RunSummary]
    let totalElements: Int
}

// MARK: - Run Enums
enum RunType: String, Codable {
    case outdoor = "OUTDOOR"
    case treadmill = "TREADMILL"
    case track = "TRACK"
    case trail = "TRAIL"

    var displayName: String {
        switch self {
        case .outdoor: return "Outdoor"
        case .treadmill: return "Laufband"
        case .track: return "Bahn"
        case .trail: return "Trail"
        }
    }
}

enum RunStatus: String, Codable {
    case draft = "DRAFT"
    case active = "ACTIVE"
    case completed = "COMPLETED"
    case paused = "PAUSED"
    case cancelled = "CANCELLED"

    var displayName: String {
        switch self {
        case .draft: return "Entwurf"
        case .active: return "Aktiv"
        case .completed: return "Abgeschlossen"
        case .paused: return "Pausiert"
        case .cancelled: return "Abgebrochen"
        }
    }
}

// MARK: - User Activity
struct UserActivity: Codable, Identifiable {
    let id: Int
    let userId: Int
    let runId: Int?
    let fitFileUploadId: Int?
    let matchedRouteId: Int?
    let matchedRouteName: String?
    let direction: ActivityDirection
    let activityStartTime: String
    let activityEndTime: String?
    let durationSeconds: Int?
    let distanceMeters: Double?
    let matchedDistanceMeters: Double?
    let routeCompletionPercentage: Double?
    let averageMatchingAccuracyMeters: Double?
    let isCompleteRoute: Bool
    let createdAt: String
}

enum ActivityDirection: String, Codable {
    case clockwise = "CLOCKWISE"
    case counterClockwise = "COUNTER_CLOCKWISE"
    case unknown = "UNKNOWN"
}
