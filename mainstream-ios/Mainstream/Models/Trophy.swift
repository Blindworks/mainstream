import Foundation
import SwiftUI

// MARK: - Trophy Model
struct Trophy: Codable, Identifiable {
    let id: Int
    let code: String
    let name: String
    let description: String
    let type: TrophyType
    let category: TrophyCategory
    let iconUrl: String?
    let criteriaValue: Double?
    let isActive: Bool
    let displayOrder: Int?

    // Location-based trophy fields
    let latitude: Double?
    let longitude: Double?
    let collectionRadiusMeters: Double?
    let validFrom: String?
    let validUntil: String?
    let imageUrl: String?

    // Generic configurable trophy criteria
    let criteriaConfig: String?
    let checkScope: CheckScope?

    let createdAt: String
    let updatedAt: String
}

// MARK: - Trophy Enums
enum TrophyType: String, Codable {
    case distanceMilestone = "DISTANCE_MILESTONE"
    case streak = "STREAK"
    case routeCompletion = "ROUTE_COMPLETION"
    case consistency = "CONSISTENCY"
    case timeBased = "TIME_BASED"
    case explorer = "EXPLORER"
    case locationBased = "LOCATION_BASED"
    case special = "SPECIAL"

    var displayName: String {
        switch self {
        case .distanceMilestone: return "Distanz-Meilenstein"
        case .streak: return "Serie"
        case .routeCompletion: return "Streckenabschluss"
        case .consistency: return "Beständigkeit"
        case .timeBased: return "Zeitbasiert"
        case .explorer: return "Entdecker"
        case .locationBased: return "Standortbasiert"
        case .special: return "Spezial"
        }
    }
}

enum TrophyCategory: String, Codable {
    case beginner = "BEGINNER"
    case intermediate = "INTERMEDIATE"
    case advanced = "ADVANCED"
    case elite = "ELITE"
    case special = "SPECIAL"

    var displayName: String {
        switch self {
        case .beginner: return "Anfänger"
        case .intermediate: return "Fortgeschritten"
        case .advanced: return "Erweitert"
        case .elite: return "Elite"
        case .special: return "Spezial"
        }
    }

    var color: Color {
        switch self {
        case .beginner: return Color.green
        case .intermediate: return Color.blue
        case .advanced: return Color.orange
        case .elite: return Color.purple
        case .special: return Color.red
        }
    }
}

enum CheckScope: String, Codable {
    case singleActivity = "SINGLE_ACTIVITY"
    case total = "TOTAL"
    case timeRange = "TIME_RANGE"
    case count = "COUNT"
}

// MARK: - User Trophy
struct UserTrophy: Codable, Identifiable {
    let id: Int
    let userId: Int
    let userName: String
    let trophy: Trophy
    let activityId: Int?
    let earnedAt: String
    let metadata: String?

    var earnedDate: Date? {
        ISO8601DateFormatter().date(from: earnedAt)
    }
}

// MARK: - Trophy With Progress
struct TrophyWithProgress: Codable, Identifiable {
    let id: Int
    let code: String
    let name: String
    let description: String
    let type: TrophyType
    let category: TrophyCategory
    let iconUrl: String?
    let criteriaValue: Double?
    let isActive: Bool
    let displayOrder: Int?
    let createdAt: String
    let updatedAt: String

    let isEarned: Bool
    let earnedAt: String?
    let progress: Double?
    let progressMax: Double?

    var progressPercentage: Double {
        guard let progress = progress, let progressMax = progressMax, progressMax > 0 else {
            return 0
        }
        return min(progress / progressMax * 100, 100)
    }
}
