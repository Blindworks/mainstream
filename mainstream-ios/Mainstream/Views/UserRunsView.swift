import SwiftUI

struct UserRunsView: View {
    @StateObject private var viewModel = UserRunsViewModel()

    var body: some View {
        NavigationView {
            ZStack {
                if viewModel.isLoading {
                    ProgressView("Lade Läufe...")
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
                                await viewModel.loadRuns()
                            }
                        }
                        .buttonStyle(.bordered)
                    }
                    .padding()
                } else if viewModel.runs.isEmpty {
                    VStack(spacing: 20) {
                        Image(systemName: "figure.run")
                            .font(.system(size: 50))
                            .foregroundColor(.gray)

                        Text("Keine Läufe gefunden")
                            .font(.headline)
                            .foregroundColor(.secondary)

                        Text("Beginne mit deinem ersten Lauf!")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                } else {
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(viewModel.runs) { run in
                                NavigationLink(destination: RunDetailView(runId: run.id)) {
                                    RunCard(run: run)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }

                            // Load More Button
                            if viewModel.hasMore {
                                Button(action: {
                                    Task {
                                        await viewModel.loadMore()
                                    }
                                }) {
                                    HStack {
                                        if viewModel.isLoadingMore {
                                            ProgressView()
                                                .progressViewStyle(CircularProgressViewStyle())
                                        } else {
                                            Text("Mehr laden")
                                        }
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding()
                                    .background(Color(.systemGray6))
                                    .cornerRadius(12)
                                }
                                .disabled(viewModel.isLoadingMore)
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Meine Läufe")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        Task {
                            await viewModel.loadRuns()
                        }
                    }) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .task {
                await viewModel.loadRuns()
            }
        }
    }
}

// MARK: - Run Card
struct RunCard: View {
    let run: RunSummary

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header with title and date
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(run.title)
                        .font(.headline)
                        .foregroundColor(.primary)

                    if let date = run.date {
                        Text(formatDate(date))
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()

                // Run Type Badge
                Text(run.runType.displayName)
                    .font(.caption)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.blue.opacity(0.2))
                    .foregroundColor(.blue)
                    .cornerRadius(4)
            }

            Divider()

            // Stats Grid
            HStack(spacing: 20) {
                RunStatView(icon: "figure.run", label: "Distanz", value: run.formattedDistance)
                RunStatView(icon: "clock", label: "Zeit", value: run.formattedDuration)
                RunStatView(icon: "speedometer", label: "Pace", value: run.averagePace ?? "--:--")
            }

            // Calories (if available)
            if let calories = run.caloriesBurned {
                HStack {
                    Image(systemName: "flame.fill")
                        .foregroundColor(.orange)
                    Text("\(calories) kcal")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }

    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

// MARK: - Run Stat View
struct RunStatView: View {
    let icon: String
    let label: String
    let value: String

    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: icon)
                .font(.caption)
                .foregroundColor(.blue)

            Text(value)
                .font(.subheadline)
                .fontWeight(.semibold)

            Text(label)
                .font(.caption2)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
    }
}

// MARK: - Run Detail View
struct RunDetailView: View {
    let runId: Int
    @State private var run: Run?
    @State private var isLoading = true
    @State private var errorMessage: String?

    var body: some View {
        ScrollView {
            if isLoading {
                ProgressView("Lade Laufdetails...")
                    .padding()
            } else if let errorMessage = errorMessage {
                VStack(spacing: 20) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 50))
                        .foregroundColor(.orange)

                    Text(errorMessage)
                        .multilineTextAlignment(.center)
                        .foregroundColor(.secondary)
                }
                .padding()
            } else if let run = run {
                VStack(alignment: .leading, spacing: 20) {
                    // Header
                    VStack(alignment: .leading, spacing: 8) {
                        Text(run.title)
                            .font(.title)
                            .fontWeight(.bold)

                        HStack {
                            Text(run.runType.displayName)
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.blue.opacity(0.2))
                                .foregroundColor(.blue)
                                .cornerRadius(4)

                            Text(run.status.displayName)
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.green.opacity(0.2))
                                .foregroundColor(.green)
                                .cornerRadius(4)
                        }
                    }
                    .padding(.horizontal)

                    Divider()

                    // Main Stats
                    VStack(spacing: 12) {
                        HStack {
                            RunDetailStatCard(title: "Distanz", value: run.formattedDistance, icon: "figure.run")
                            RunDetailStatCard(title: "Zeit", value: run.formattedDuration, icon: "clock")
                        }

                        HStack {
                            RunDetailStatCard(title: "Pace", value: run.formattedPace + " min/km", icon: "speedometer")
                            if let calories = run.caloriesBurned {
                                RunDetailStatCard(title: "Kalorien", value: "\(calories) kcal", icon: "flame.fill")
                            }
                        }

                        if let avgSpeed = run.averageSpeedKmh {
                            HStack {
                                RunDetailStatCard(title: "Ø Geschw.", value: String(format: "%.1f km/h", avgSpeed), icon: "gauge")
                                if let maxSpeed = run.maxSpeedKmh {
                                    RunDetailStatCard(title: "Max Geschw.", value: String(format: "%.1f km/h", maxSpeed), icon: "gauge.high")
                                }
                            }
                        }

                        if let elevGain = run.elevationGainMeters {
                            HStack {
                                RunDetailStatCard(title: "Anstieg", value: String(format: "%.0f m", elevGain), icon: "arrow.up")
                                if let elevLoss = run.elevationLossMeters {
                                    RunDetailStatCard(title: "Abstieg", value: String(format: "%.0f m", elevLoss), icon: "arrow.down")
                                }
                            }
                        }
                    }
                    .padding(.horizontal)

                    // Description
                    if let description = run.description, !description.isEmpty {
                        Divider()

                        VStack(alignment: .leading, spacing: 8) {
                            Text("Beschreibung")
                                .font(.headline)

                            Text(description)
                                .font(.body)
                                .foregroundColor(.secondary)
                        }
                        .padding(.horizontal)
                    }

                    // Weather Info
                    if run.weatherCondition != nil || run.temperatureCelsius != nil {
                        Divider()

                        VStack(alignment: .leading, spacing: 8) {
                            Text("Wetter")
                                .font(.headline)

                            HStack {
                                if let weather = run.weatherCondition {
                                    Text(weather)
                                        .foregroundColor(.secondary)
                                }

                                if let temp = run.temperatureCelsius {
                                    Text(String(format: "%.1f°C", temp))
                                        .foregroundColor(.secondary)
                                }

                                if let humidity = run.humidityPercentage {
                                    Text("\(humidity)% Luftfeuchtigkeit")
                                        .foregroundColor(.secondary)
                                }
                            }
                        }
                        .padding(.horizontal)
                    }

                    // Matched Route Info
                    if let activity = run.userActivity, let routeName = activity.matchedRouteName {
                        Divider()

                        VStack(alignment: .leading, spacing: 8) {
                            Text("Zugeordnete Route")
                                .font(.headline)

                            VStack(alignment: .leading, spacing: 4) {
                                Text(routeName)
                                    .font(.body)
                                    .foregroundColor(.blue)

                                if activity.isCompleteRoute {
                                    HStack {
                                        Image(systemName: "checkmark.circle.fill")
                                            .foregroundColor(.green)
                                        Text("Route vollständig")
                                            .foregroundColor(.green)
                                    }
                                    .font(.caption)
                                } else if let percentage = activity.routeCompletionPercentage {
                                    Text(String(format: "%.1f%% abgeschlossen", percentage))
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                            }
                        }
                        .padding(.horizontal)
                    }
                }
                .padding(.vertical)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await loadRunDetail()
        }
    }

    private func loadRunDetail() async {
        isLoading = true
        errorMessage = nil

        do {
            let loadedRun = try await RunService.shared.getRunById(id: runId)
            self.run = loadedRun
        } catch {
            self.errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}

// MARK: - Run Detail Stat Card
struct RunDetailStatCard: View {
    let title: String
    let value: String
    let icon: String

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(.blue)

            Text(value)
                .font(.headline)

            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

// MARK: - User Runs View Model
@MainActor
class UserRunsViewModel: ObservableObject {
    @Published var runs: [RunSummary] = []
    @Published var isLoading = false
    @Published var isLoadingMore = false
    @Published var errorMessage: String?

    private var currentPage = 0
    private let pageSize = 20
    private var totalElements = 0

    var hasMore: Bool {
        runs.count < totalElements
    }

    func loadRuns() async {
        currentPage = 0
        runs = []
        await fetchRuns()
    }

    func loadMore() async {
        guard !isLoadingMore else { return }
        currentPage += 1
        isLoadingMore = true
        await fetchRuns()
        isLoadingMore = false
    }

    private func fetchRuns() async {
        if currentPage == 0 {
            isLoading = true
        }
        errorMessage = nil

        do {
            let response = try await RunService.shared.getAllRuns(page: currentPage, size: pageSize)

            if currentPage == 0 {
                self.runs = response.content
            } else {
                self.runs.append(contentsOf: response.content)
            }

            self.totalElements = response.totalElements
        } catch {
            self.errorMessage = error.localizedDescription
        }

        if currentPage == 0 {
            isLoading = false
        }
    }
}

#Preview {
    UserRunsView()
}
