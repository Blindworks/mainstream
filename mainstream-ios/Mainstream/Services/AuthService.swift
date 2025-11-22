import Foundation
import Combine

// MARK: - Auth Service
@MainActor
class AuthService: ObservableObject {
    static let shared = AuthService()

    @Published var isAuthenticated = false
    @Published var currentUser: User?
    @Published var token: String?

    private let tokenKey = "mainstream_token"
    private let userKey = "mainstream_user"

    private init() {
        loadStoredAuth()
    }

    // MARK: - Login
    func login(email: String, password: String) async throws {
        let loginRequest = LoginRequest(email: email, password: password)

        let response: AuthResponse = try await APIService.shared.request(
            endpoint: "/api/auth/login",
            method: .post,
            body: loginRequest
        )

        // Store credentials
        self.token = response.token
        self.currentUser = response.user
        self.isAuthenticated = true

        // Save to UserDefaults
        UserDefaults.standard.set(response.token, forKey: tokenKey)
        if let userData = try? JSONEncoder().encode(response.user) {
            UserDefaults.standard.set(userData, forKey: userKey)
        }
    }

    // MARK: - Logout
    func logout() {
        self.token = nil
        self.currentUser = nil
        self.isAuthenticated = false

        // Clear UserDefaults
        UserDefaults.standard.removeObject(forKey: tokenKey)
        UserDefaults.standard.removeObject(forKey: userKey)
    }

    // MARK: - Register
    func register(userData: UserRegistration) async throws -> User {
        let user: User = try await APIService.shared.request(
            endpoint: "/api/auth/register",
            method: .post,
            body: userData
        )
        return user
    }

    // MARK: - Validate Token
    func validateToken() async throws -> Bool {
        guard let token = token else {
            return false
        }

        do {
            let isValid: Bool = try await APIService.shared.request(
                endpoint: "/api/auth/validate?token=\(token)",
                method: .post,
                headers: authHeaders
            )

            if !isValid {
                logout()
            }

            return isValid
        } catch {
            logout()
            return false
        }
    }

    // MARK: - Refresh User Data
    func refreshUserData() async throws {
        guard isAuthenticated else { return }

        let user: User = try await APIService.shared.request(
            endpoint: "/api/auth/user",
            method: .get,
            headers: authHeaders
        )

        self.currentUser = user

        // Update stored user
        if let userData = try? JSONEncoder().encode(user) {
            UserDefaults.standard.set(userData, forKey: userKey)
        }
    }

    // MARK: - Helper Methods
    private func loadStoredAuth() {
        if let storedToken = UserDefaults.standard.string(forKey: tokenKey),
           let storedUserData = UserDefaults.standard.data(forKey: userKey),
           let storedUser = try? JSONDecoder().decode(User.self, from: storedUserData) {

            self.token = storedToken
            self.currentUser = storedUser
            self.isAuthenticated = true

            // Validate token in background
            Task {
                _ = try? await validateToken()
            }
        }
    }

    var authHeaders: [String: String] {
        guard let token = token else { return [:] }
        return [
            "Authorization": "Bearer \(token)",
            "Content-Type": "application/json"
        ]
    }

    var userId: Int? {
        currentUser?.id
    }
}
