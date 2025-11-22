import Foundation

// MARK: - Trophy Service
class TrophyService {
    static let shared = TrophyService()

    private init() {}

    // MARK: - Get All Trophies
    func getAllTrophies() async throws -> [Trophy] {
        let trophies: [Trophy] = try await APIService.shared.request(
            endpoint: "/api/trophies",
            method: .get,
            headers: AuthService.shared.authHeaders
        )
        return trophies
    }

    // MARK: - Get User Trophies
    func getUserTrophies() async throws -> [UserTrophy] {
        let userTrophies: [UserTrophy] = try await APIService.shared.request(
            endpoint: "/api/trophies/my",
            method: .get,
            headers: AuthService.shared.authHeaders
        )
        return userTrophies
    }

    // MARK: - Get Trophies with Progress
    func getTrophiesWithProgress() async throws -> [TrophyWithProgress] {
        async let allTrophies = getAllTrophies()
        async let userTrophies = getUserTrophies()

        let (all, earned) = try await (allTrophies, userTrophies)

        // Convert to TrophyWithProgress
        let trophiesWithProgress = all.map { trophy -> TrophyWithProgress in
            let userTrophy = earned.first { $0.trophy.id == trophy.id }

            return TrophyWithProgress(
                id: trophy.id,
                code: trophy.code,
                name: trophy.name,
                description: trophy.description,
                type: trophy.type,
                category: trophy.category,
                iconUrl: trophy.iconUrl,
                criteriaValue: trophy.criteriaValue,
                isActive: trophy.isActive,
                displayOrder: trophy.displayOrder,
                createdAt: trophy.createdAt,
                updatedAt: trophy.updatedAt,
                isEarned: userTrophy != nil,
                earnedAt: userTrophy?.earnedAt,
                progress: nil,
                progressMax: nil
            )
        }

        return trophiesWithProgress
    }

    // MARK: - Get Trophies for Activity
    func getTrophiesForActivity(activityId: Int) async throws -> [UserTrophy] {
        let trophies: [UserTrophy] = try await APIService.shared.request(
            endpoint: "/api/trophies/activity/\(activityId)",
            method: .get,
            headers: AuthService.shared.authHeaders
        )
        return trophies
    }

    // MARK: - Get Today's Trophy
    func getTodaysTrophy() async throws -> Trophy? {
        do {
            let trophy: Trophy? = try await APIService.shared.request(
                endpoint: "/api/trophies/daily/today",
                method: .get,
                headers: AuthService.shared.authHeaders
            )
            return trophy
        } catch APIError.notFound {
            return nil
        }
    }

    // MARK: - Get Today's Trophy Winners
    func getTodaysTrophyWinners() async throws -> [UserTrophy] {
        let winners: [UserTrophy] = try await APIService.shared.request(
            endpoint: "/api/trophies/daily/today/winners",
            method: .get,
            headers: AuthService.shared.authHeaders
        )
        return winners
    }

    // MARK: - Get Weekly Trophies
    func getWeeklyTrophies() async throws -> [UserTrophy] {
        let trophies: [UserTrophy] = try await APIService.shared.request(
            endpoint: "/api/trophies/weekly",
            method: .get,
            headers: AuthService.shared.authHeaders
        )
        return trophies
    }
}
