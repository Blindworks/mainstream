import Foundation

// MARK: - User Model
struct User: Codable, Identifiable {
    let id: Int
    let email: String
    let username: String?
    let firstName: String
    let lastName: String
    let fullName: String?
    let dateOfBirth: String?
    let gender: Gender?
    let phoneNumber: String?
    let profilePictureUrl: String?
    let bio: String?
    let city: String?
    let fitnessLevel: FitnessLevel?
    let preferredDistanceUnit: DistanceUnit?
    let isPublicProfile: Bool?
    let role: UserRole
    let createdAt: String
    let updatedAt: String

    var displayName: String {
        fullName ?? "\(firstName) \(lastName)"
    }
}

// MARK: - Enums
enum Gender: String, Codable {
    case male = "MALE"
    case female = "FEMALE"
    case other = "OTHER"
    case preferNotToSay = "PREFER_NOT_TO_SAY"
}

enum FitnessLevel: String, Codable {
    case beginner = "BEGINNER"
    case intermediate = "INTERMEDIATE"
    case advanced = "ADVANCED"
    case expert = "EXPERT"
}

enum DistanceUnit: String, Codable {
    case kilometers = "KILOMETERS"
    case miles = "MILES"
}

enum UserRole: String, Codable {
    case user = "USER"
    case admin = "ADMIN"
    case moderator = "MODERATOR"
}

// MARK: - Auth Models
struct LoginRequest: Codable {
    let email: String
    let password: String
}

struct AuthResponse: Codable {
    let token: String
    let user: User
    let expiresIn: Int
}

struct UserRegistration: Codable {
    let email: String
    let password: String
    let firstName: String
    let lastName: String
    let dateOfBirth: String?
    let gender: Gender?
    let phoneNumber: String?
    let bio: String?
    let city: String?
    let fitnessLevel: FitnessLevel?
    let preferredDistanceUnit: DistanceUnit?
    let isPublicProfile: Bool?
}
