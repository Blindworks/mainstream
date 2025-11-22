import SwiftUI

struct MainTabView: View {
    @StateObject private var authService = AuthService.shared

    var body: some View {
        TabView {
            // Trophies Tab
            TrophiesView()
                .tabItem {
                    Label("Trophäen", systemImage: "trophy.fill")
                }

            // Routes Tab
            RoutesView()
                .tabItem {
                    Label("Routen", systemImage: "map.fill")
                }

            // User Runs Tab
            UserRunsView()
                .tabItem {
                    Label("Läufe", systemImage: "figure.run")
                }

            // Profile Tab
            ProfileView()
                .tabItem {
                    Label("Profil", systemImage: "person.fill")
                }
        }
        .accentColor(.blue)
    }
}

#Preview {
    MainTabView()
}
