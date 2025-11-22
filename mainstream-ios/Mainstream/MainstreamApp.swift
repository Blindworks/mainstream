import SwiftUI

@main
struct MainstreamApp: App {
    @StateObject private var authService = AuthService.shared

    var body: some Scene {
        WindowGroup {
            if authService.isAuthenticated {
                MainTabView()
            } else {
                LoginView()
            }
        }
    }
}
