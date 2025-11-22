import Foundation

// MARK: - API Configuration
class APIConfiguration {
    static let shared = APIConfiguration()

    // Change this to your backend URL
    var baseURL: String = "http://localhost:8080"

    private init() {}
}

// MARK: - API Error
enum APIError: Error, LocalizedError {
    case invalidURL
    case invalidResponse
    case networkError(Error)
    case decodingError(Error)
    case serverError(statusCode: Int, message: String?)
    case unauthorized
    case notFound

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Ungültige URL"
        case .invalidResponse:
            return "Ungültige Antwort vom Server"
        case .networkError(let error):
            return "Netzwerkfehler: \(error.localizedDescription)"
        case .decodingError(let error):
            return "Fehler beim Dekodieren: \(error.localizedDescription)"
        case .serverError(let statusCode, let message):
            return "Serverfehler (\(statusCode)): \(message ?? "Unbekannter Fehler")"
        case .unauthorized:
            return "Nicht autorisiert. Bitte melden Sie sich erneut an."
        case .notFound:
            return "Ressource nicht gefunden"
        }
    }
}

// MARK: - Base API Service
class APIService {
    static let shared = APIService()

    private let decoder: JSONDecoder = {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        return decoder
    }()

    private let encoder: JSONEncoder = {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        return encoder
    }()

    private init() {}

    // MARK: - Generic Request Method
    func request<T: Decodable>(
        endpoint: String,
        method: HTTPMethod = .get,
        body: Encodable? = nil,
        headers: [String: String]? = nil
    ) async throws -> T {
        guard let url = URL(string: APIConfiguration.shared.baseURL + endpoint) else {
            throw APIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue

        // Set default headers
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        // Add custom headers
        headers?.forEach { key, value in
            request.setValue(value, forHTTPHeaderField: key)
        }

        // Add body if present
        if let body = body {
            do {
                request.httpBody = try encoder.encode(body)
            } catch {
                throw APIError.decodingError(error)
            }
        }

        do {
            let (data, response) = try await URLSession.shared.data(for: request)

            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIError.invalidResponse
            }

            // Handle different status codes
            switch httpResponse.statusCode {
            case 200...299:
                do {
                    return try decoder.decode(T.self, from: data)
                } catch {
                    throw APIError.decodingError(error)
                }
            case 401:
                throw APIError.unauthorized
            case 404:
                throw APIError.notFound
            default:
                let message = String(data: data, encoding: .utf8)
                throw APIError.serverError(statusCode: httpResponse.statusCode, message: message)
            }
        } catch let error as APIError {
            throw error
        } catch {
            throw APIError.networkError(error)
        }
    }

    // MARK: - Request without response body
    func requestNoResponse(
        endpoint: String,
        method: HTTPMethod = .post,
        body: Encodable? = nil,
        headers: [String: String]? = nil
    ) async throws {
        guard let url = URL(string: APIConfiguration.shared.baseURL + endpoint) else {
            throw APIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue

        // Set default headers
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        // Add custom headers
        headers?.forEach { key, value in
            request.setValue(value, forHTTPHeaderField: key)
        }

        // Add body if present
        if let body = body {
            do {
                request.httpBody = try encoder.encode(body)
            } catch {
                throw APIError.decodingError(error)
            }
        }

        do {
            let (_, response) = try await URLSession.shared.data(for: request)

            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIError.invalidResponse
            }

            // Handle different status codes
            switch httpResponse.statusCode {
            case 200...299:
                return
            case 401:
                throw APIError.unauthorized
            case 404:
                throw APIError.notFound
            default:
                throw APIError.serverError(statusCode: httpResponse.statusCode, message: nil)
            }
        } catch let error as APIError {
            throw error
        } catch {
            throw APIError.networkError(error)
        }
    }
}

// MARK: - HTTP Method
enum HTTPMethod: String {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case delete = "DELETE"
    case patch = "PATCH"
}
