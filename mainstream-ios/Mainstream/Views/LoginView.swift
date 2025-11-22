import SwiftUI

struct LoginView: View {
    @StateObject private var authService = AuthService.shared
    @State private var email = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        NavigationView {
            ZStack {
                // Background gradient
                LinearGradient(
                    gradient: Gradient(colors: [Color.blue.opacity(0.6), Color.purple.opacity(0.6)]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()

                VStack(spacing: 25) {
                    // Logo/Title
                    VStack(spacing: 10) {
                        Image(systemName: "figure.run")
                            .font(.system(size: 80))
                            .foregroundColor(.white)

                        Text("Mainstream")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                            .foregroundColor(.white)

                        Text("Lauf-Tracking App")
                            .font(.subheadline)
                            .foregroundColor(.white.opacity(0.8))
                    }
                    .padding(.bottom, 40)

                    // Login Form
                    VStack(spacing: 20) {
                        // Email Field
                        VStack(alignment: .leading, spacing: 8) {
                            Text("E-Mail")
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundColor(.white)

                            TextField("", text: $email)
                                .textFieldStyle(CustomTextFieldStyle())
                                .textContentType(.emailAddress)
                                .autocapitalization(.none)
                                .keyboardType(.emailAddress)
                        }

                        // Password Field
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Passwort")
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundColor(.white)

                            SecureField("", text: $password)
                                .textFieldStyle(CustomTextFieldStyle())
                                .textContentType(.password)
                        }

                        // Error Message
                        if let errorMessage = errorMessage {
                            Text(errorMessage)
                                .font(.caption)
                                .foregroundColor(.red)
                                .padding(.horizontal)
                                .padding(.vertical, 8)
                                .background(Color.white.opacity(0.9))
                                .cornerRadius(8)
                        }

                        // Login Button
                        Button(action: performLogin) {
                            HStack {
                                if isLoading {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .blue))
                                } else {
                                    Text("Anmelden")
                                        .fontWeight(.semibold)
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.white)
                            .foregroundColor(.blue)
                            .cornerRadius(12)
                        }
                        .disabled(isLoading || email.isEmpty || password.isEmpty)
                        .opacity((isLoading || email.isEmpty || password.isEmpty) ? 0.6 : 1.0)
                    }
                    .padding(.horizontal, 30)

                    Spacer()
                }
                .padding(.top, 60)
            }
            .navigationBarHidden(true)
        }
    }

    private func performLogin() {
        errorMessage = nil
        isLoading = true

        Task {
            do {
                try await authService.login(email: email, password: password)
                // Navigation happens automatically via authService.isAuthenticated
            } catch {
                await MainActor.run {
                    errorMessage = error.localizedDescription
                    isLoading = false
                }
            }
        }
    }
}

// MARK: - Custom Text Field Style
struct CustomTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding()
            .background(Color.white.opacity(0.9))
            .cornerRadius(10)
            .foregroundColor(.black)
    }
}

#Preview {
    LoginView()
}
