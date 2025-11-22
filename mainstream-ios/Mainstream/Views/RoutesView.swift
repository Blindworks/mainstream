import SwiftUI

struct RoutesView: View {
    @StateObject private var viewModel = RoutesViewModel()

    var body: some View {
        NavigationView {
            ZStack {
                if viewModel.isLoading {
                    ProgressView("Lade Routen...")
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
                                await viewModel.loadRoutes()
                            }
                        }
                        .buttonStyle(.bordered)
                    }
                    .padding()
                } else if viewModel.routes.isEmpty {
                    VStack(spacing: 20) {
                        Image(systemName: "map")
                            .font(.system(size: 50))
                            .foregroundColor(.gray)

                        Text("Keine Routen verfügbar")
                            .font(.headline)
                            .foregroundColor(.secondary)
                    }
                } else {
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(viewModel.routes) { route in
                                NavigationLink(destination: RouteDetailView(route: route)) {
                                    RouteCard(route: route)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Routen")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        Task {
                            await viewModel.loadRoutes()
                        }
                    }) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .task {
                await viewModel.loadRoutes()
            }
        }
    }
}

// MARK: - Route Card
struct RouteCard: View {
    let route: PredefinedRoute

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Route Image (if available) or placeholder
            if let imageUrl = route.imageUrl {
                AsyncImage(url: URL(string: imageUrl)) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    RouteImagePlaceholder()
                }
                .frame(height: 150)
                .clipped()
                .cornerRadius(12)
            } else {
                RouteImagePlaceholder()
                    .frame(height: 150)
                    .cornerRadius(12)
            }

            // Route Info
            VStack(alignment: .leading, spacing: 8) {
                Text(route.name)
                    .font(.headline)
                    .foregroundColor(.primary)

                if let city = route.city {
                    HStack {
                        Image(systemName: "location.fill")
                            .font(.caption)
                        Text(city)
                            .font(.subheadline)
                    }
                    .foregroundColor(.secondary)
                }

                if let description = route.description {
                    Text(description)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(2)
                }

                // Stats
                HStack(spacing: 20) {
                    RouteStatView(icon: "ruler", value: route.formattedDistance)
                    RouteStatView(icon: "arrow.up", value: route.formattedElevationGain)

                    if let stats = route.stats {
                        Spacer()
                        VStack(alignment: .trailing) {
                            Text("\(stats.totalCount)")
                                .font(.headline)
                                .foregroundColor(.blue)
                            Text("Läufe")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .padding(.top, 4)
            }
            .padding(.horizontal, 4)
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}

// MARK: - Route Image Placeholder
struct RouteImagePlaceholder: View {
    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color.blue.opacity(0.3), Color.purple.opacity(0.3)]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )

            Image(systemName: "map")
                .font(.system(size: 40))
                .foregroundColor(.white)
        }
    }
}

// MARK: - Route Stat View
struct RouteStatView: View {
    let icon: String
    let value: String

    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: icon)
                .font(.caption)
                .foregroundColor(.blue)
            Text(value)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

// MARK: - Route Detail View
struct RouteDetailView: View {
    let route: PredefinedRoute

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Route Image
                if let imageUrl = route.imageUrl {
                    AsyncImage(url: URL(string: imageUrl)) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        RouteImagePlaceholder()
                    }
                    .frame(height: 250)
                    .clipped()
                } else {
                    RouteImagePlaceholder()
                        .frame(height: 250)
                }

                VStack(alignment: .leading, spacing: 16) {
                    // Title and City
                    VStack(alignment: .leading, spacing: 8) {
                        Text(route.name)
                            .font(.title)
                            .fontWeight(.bold)

                        if let city = route.city {
                            HStack {
                                Image(systemName: "location.fill")
                                Text(city)
                            }
                            .foregroundColor(.secondary)
                        }
                    }

                    // Description
                    if let description = route.description {
                        Text(description)
                            .font(.body)
                            .foregroundColor(.secondary)
                    }

                    Divider()

                    // Stats Grid
                    VStack(spacing: 12) {
                        HStack {
                            DetailStatCard(title: "Distanz", value: route.formattedDistance, icon: "ruler")
                            DetailStatCard(title: "Anstieg", value: route.formattedElevationGain, icon: "arrow.up")
                        }

                        HStack {
                            DetailStatCard(title: "Abstieg", value: route.formattedElevationLoss, icon: "arrow.down")
                            DetailStatCard(title: "Punkte", value: "\(route.trackPointCount)", icon: "point.3.connected.trianglepath.dotted")
                        }
                    }

                    // Activity Stats (if available)
                    if let stats = route.stats {
                        Divider()

                        Text("Aktivitäten")
                            .font(.headline)

                        VStack(spacing: 8) {
                            ActivityStatRow(label: "Heute", count: stats.todayCount)
                            ActivityStatRow(label: "Diese Woche", count: stats.thisWeekCount)
                            ActivityStatRow(label: "Dieser Monat", count: stats.thisMonthCount)
                            ActivityStatRow(label: "Dieses Jahr", count: stats.thisYearCount)
                            ActivityStatRow(label: "Gesamt", count: stats.totalCount, highlight: true)
                        }
                    }
                }
                .padding()
            }
        }
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - Detail Stat Card
struct DetailStatCard: View {
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

// MARK: - Activity Stat Row
struct ActivityStatRow: View {
    let label: String
    let count: Int
    var highlight: Bool = false

    var body: some View {
        HStack {
            Text(label)
                .foregroundColor(highlight ? .primary : .secondary)
                .fontWeight(highlight ? .semibold : .regular)

            Spacer()

            Text("\(count)")
                .foregroundColor(highlight ? .blue : .secondary)
                .fontWeight(highlight ? .bold : .regular)
        }
        .padding(.vertical, 4)
    }
}

// MARK: - Routes View Model
@MainActor
class RoutesViewModel: ObservableObject {
    @Published var routes: [PredefinedRoute] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    func loadRoutes() async {
        isLoading = true
        errorMessage = nil

        do {
            let loadedRoutes = try await RouteService.shared.getAllRoutesWithStats()
            self.routes = loadedRoutes.sorted { $0.name < $1.name }
        } catch {
            self.errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}

#Preview {
    RoutesView()
}
