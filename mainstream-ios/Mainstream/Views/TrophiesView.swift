import SwiftUI

struct TrophiesView: View {
    @StateObject private var viewModel = TrophiesViewModel()

    var body: some View {
        NavigationView {
            ZStack {
                if viewModel.isLoading {
                    ProgressView("Lade Trophäen...")
                } else if let errorMessage = viewModel.errorMessage {
                    VStack(spacing: 20) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.system(size: 50))
                            .foregroundColor(.orange)

                        Text(errorMessage)
                            .multilineTextAlignment(.center)
                            .foregroundColor(.secondary)

                        Button("Erneut versuchen") {
                            Task {
                                await viewModel.loadTrophies()
                            }
                        }
                        .buttonStyle(.bordered)
                    }
                    .padding()
                } else {
                    ScrollView {
                        VStack(spacing: 20) {
                            // Stats Header
                            HStack(spacing: 20) {
                                StatCard(
                                    title: "Erhalten",
                                    value: "\(viewModel.earnedCount)",
                                    icon: "trophy.fill",
                                    color: .green
                                )

                                StatCard(
                                    title: "Gesamt",
                                    value: "\(viewModel.trophies.count)",
                                    icon: "star.fill",
                                    color: .blue
                                )
                            }
                            .padding(.horizontal)

                            // Filter Picker
                            Picker("Filter", selection: $viewModel.selectedFilter) {
                                Text("Alle").tag(TrophyFilter.all)
                                Text("Erhalten").tag(TrophyFilter.earned)
                                Text("Verfügbar").tag(TrophyFilter.available)
                            }
                            .pickerStyle(.segmented)
                            .padding(.horizontal)

                            // Trophy List
                            LazyVStack(spacing: 12) {
                                ForEach(viewModel.filteredTrophies) { trophy in
                                    TrophyCard(trophy: trophy)
                                }
                            }
                            .padding(.horizontal)
                        }
                        .padding(.vertical)
                    }
                }
            }
            .navigationTitle("Trophäen")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        Task {
                            await viewModel.loadTrophies()
                        }
                    }) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .task {
                await viewModel.loadTrophies()
            }
        }
    }
}

// MARK: - Trophy Card
struct TrophyCard: View {
    let trophy: TrophyWithProgress

    var body: some View {
        HStack(spacing: 15) {
            // Trophy Icon
            ZStack {
                Circle()
                    .fill(trophy.isEarned ? trophy.category.color.opacity(0.2) : Color.gray.opacity(0.1))
                    .frame(width: 60, height: 60)

                Image(systemName: trophy.isEarned ? "trophy.fill" : "trophy")
                    .font(.system(size: 28))
                    .foregroundColor(trophy.isEarned ? trophy.category.color : .gray)
            }

            // Trophy Info
            VStack(alignment: .leading, spacing: 4) {
                Text(trophy.name)
                    .font(.headline)
                    .foregroundColor(trophy.isEarned ? .primary : .secondary)

                Text(trophy.description)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(2)

                HStack {
                    Text(trophy.category.displayName)
                        .font(.caption2)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(trophy.category.color.opacity(0.2))
                        .foregroundColor(trophy.category.color)
                        .cornerRadius(4)

                    if trophy.isEarned, let earnedAt = trophy.earnedAt {
                        Text("Erhalten: \(formatDate(earnedAt))")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }
            }

            Spacer()

            // Earned checkmark
            if trophy.isEarned {
                Image(systemName: "checkmark.circle.fill")
                    .font(.title2)
                    .foregroundColor(.green)
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }

    private func formatDate(_ dateString: String) -> String {
        let formatter = ISO8601DateFormatter()
        guard let date = formatter.date(from: dateString) else { return "" }

        let displayFormatter = DateFormatter()
        displayFormatter.dateStyle = .short
        return displayFormatter.string(from: date)
    }
}

// MARK: - Stat Card
struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(color)

            Text(value)
                .font(.title)
                .fontWeight(.bold)

            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}

// MARK: - Trophy Filter
enum TrophyFilter {
    case all
    case earned
    case available
}

// MARK: - Trophies View Model
@MainActor
class TrophiesViewModel: ObservableObject {
    @Published var trophies: [TrophyWithProgress] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var selectedFilter: TrophyFilter = .all

    var earnedCount: Int {
        trophies.filter { $0.isEarned }.count
    }

    var filteredTrophies: [TrophyWithProgress] {
        switch selectedFilter {
        case .all:
            return trophies
        case .earned:
            return trophies.filter { $0.isEarned }
        case .available:
            return trophies.filter { !$0.isEarned }
        }
    }

    func loadTrophies() async {
        isLoading = true
        errorMessage = nil

        do {
            let loadedTrophies = try await TrophyService.shared.getTrophiesWithProgress()
            self.trophies = loadedTrophies.sorted { trophy1, trophy2 in
                // Earned trophies first, then by category
                if trophy1.isEarned != trophy2.isEarned {
                    return trophy1.isEarned
                }
                return trophy1.category.rawValue < trophy2.category.rawValue
            }
        } catch {
            self.errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}

#Preview {
    TrophiesView()
}
