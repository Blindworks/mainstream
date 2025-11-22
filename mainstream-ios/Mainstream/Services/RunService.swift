import Foundation

// MARK: - Run Service
class RunService {
    static let shared = RunService()

    private init() {}

    // MARK: - Get All User Runs
    func getAllRuns(page: Int = 0, size: Int = 20) async throws -> PaginatedRunResponse {
        let response: PaginatedRunResponse = try await APIService.shared.request(
            endpoint: "/api/runs?page=\(page)&size=\(size)",
            method: .get,
            headers: AuthService.shared.authHeaders
        )
        return response
    }

    // MARK: - Get Run by ID
    func getRunById(id: Int) async throws -> Run {
        let run: Run = try await APIService.shared.request(
            endpoint: "/api/runs/\(id)",
            method: .get,
            headers: AuthService.shared.authHeaders
        )
        return run
    }

    // MARK: - Delete Run
    func deleteRun(id: Int) async throws {
        try await APIService.shared.requestNoResponse(
            endpoint: "/api/runs/\(id)",
            method: .delete,
            headers: AuthService.shared.authHeaders
        )
    }

    // MARK: - Get Today's Active Users Count
    func getTodayActiveUsersCount() async throws -> Int {
        let count: Int = try await APIService.shared.request(
            endpoint: "/api/runs/today-active-users",
            method: .get,
            headers: AuthService.shared.authHeaders
        )
        return count
    }
}
