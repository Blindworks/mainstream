import Foundation

// MARK: - Route Service
class RouteService {
    static let shared = RouteService()

    private init() {}

    // MARK: - Get All Routes
    func getAllRoutes(activeOnly: Bool = true, city: String? = nil) async throws -> [PredefinedRoute] {
        var endpoint = "/api/routes?activeOnly=\(activeOnly)"
        if let city = city {
            endpoint += "&city=\(city)"
        }

        let routes: [PredefinedRoute] = try await APIService.shared.request(
            endpoint: endpoint,
            method: .get,
            headers: AuthService.shared.authHeaders
        )
        return routes
    }

    // MARK: - Get All Routes with Stats
    func getAllRoutesWithStats(activeOnly: Bool = true, city: String? = nil) async throws -> [PredefinedRoute] {
        var endpoint = "/api/routes/with-stats?activeOnly=\(activeOnly)"
        if let city = city {
            endpoint += "&city=\(city)"
        }

        let routes: [PredefinedRoute] = try await APIService.shared.request(
            endpoint: endpoint,
            method: .get,
            headers: AuthService.shared.authHeaders
        )
        return routes
    }

    // MARK: - Get Route by ID
    func getRouteById(id: Int) async throws -> PredefinedRoute {
        let route: PredefinedRoute = try await APIService.shared.request(
            endpoint: "/api/routes/\(id)",
            method: .get,
            headers: AuthService.shared.authHeaders
        )
        return route
    }
}
