import SwiftUI

struct ProfileView: View {
    @StateObject private var authService = AuthService.shared

    var body: some View {
        NavigationView {
            List {
                // User Info Section
                if let user = authService.currentUser {
                    Section {
                        HStack(spacing: 15) {
                            // Profile Picture
                            ZStack {
                                Circle()
                                    .fill(LinearGradient(
                                        gradient: Gradient(colors: [.blue, .purple]),
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    ))
                                    .frame(width: 70, height: 70)

                                if let profilePictureUrl = user.profilePictureUrl,
                                   let url = URL(string: profilePictureUrl) {
                                    AsyncImage(url: url) { image in
                                        image
                                            .resizable()
                                            .aspectRatio(contentMode: .fill)
                                    } placeholder: {
                                        Text(user.firstName.prefix(1).uppercased())
                                            .font(.title)
                                            .foregroundColor(.white)
                                    }
                                    .frame(width: 70, height: 70)
                                    .clipShape(Circle())
                                } else {
                                    Text(user.firstName.prefix(1).uppercased())
                                        .font(.title)
                                        .foregroundColor(.white)
                                }
                            }

                            VStack(alignment: .leading, spacing: 4) {
                                Text(user.displayName)
                                    .font(.headline)

                                Text(user.email)
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)

                                if let city = user.city {
                                    HStack {
                                        Image(systemName: "location.fill")
                                            .font(.caption)
                                        Text(city)
                                            .font(.caption)
                                    }
                                    .foregroundColor(.secondary)
                                }
                            }
                        }
                        .padding(.vertical, 8)
                    }

                    // User Details Section
                    Section("Details") {
                        if let fitnessLevel = user.fitnessLevel {
                            LabeledContent("Fitnesslevel", value: fitnessLevel.rawValue.capitalized)
                        }

                        if let preferredUnit = user.preferredDistanceUnit {
                            LabeledContent("Einheit", value: preferredUnit == .kilometers ? "Kilometer" : "Meilen")
                        }

                        LabeledContent("Rolle", value: user.role.rawValue.capitalized)
                    }

                    // Bio Section
                    if let bio = user.bio, !bio.isEmpty {
                        Section("Bio") {
                            Text(bio)
                                .font(.body)
                        }
                    }
                }

                // Settings Section
                Section("Einstellungen") {
                    NavigationLink(destination: SettingsView()) {
                        Label("App-Einstellungen", systemImage: "gearshape")
                    }
                }

                // Logout Section
                Section {
                    Button(action: {
                        authService.logout()
                    }) {
                        HStack {
                            Spacer()
                            Text("Abmelden")
                                .foregroundColor(.red)
                            Spacer()
                        }
                    }
                }
            }
            .navigationTitle("Profil")
        }
    }
}

// MARK: - Settings View
struct SettingsView: View {
    @AppStorage("apiBaseUrl") private var apiBaseUrl = "http://localhost:8080"

    var body: some View {
        Form {
            Section("API-Einstellungen") {
                TextField("Backend URL", text: $apiBaseUrl)
                    .textContentType(.URL)
                    .autocapitalization(.none)
                    .keyboardType(.URL)

                Button("Speichern") {
                    APIConfiguration.shared.baseURL = apiBaseUrl
                }
                .buttonStyle(.bordered)
            }

            Section {
                Text("Die Backend URL wird verwendet, um mit dem Mainstream Server zu kommunizieren.")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .navigationTitle("Einstellungen")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            apiBaseUrl = APIConfiguration.shared.baseURL
        }
    }
}

#Preview {
    ProfileView()
}
