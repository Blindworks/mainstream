package com.mainstream.app.data.repository

import com.mainstream.app.data.api.AuthApi
import com.mainstream.app.data.local.TokenManager
import com.mainstream.app.data.model.AuthResponse
import com.mainstream.app.data.model.LoginRequest
import com.mainstream.app.data.model.RegisterRequest
import com.mainstream.app.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    val currentUser: Flow<User?> = tokenManager.user
    val isAuthenticated: Flow<Boolean> = tokenManager.token.let { tokenFlow ->
        kotlinx.coroutines.flow.map(tokenFlow) { it != null }
    }

    suspend fun login(email: String, password: String): AuthResult<AuthResponse> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveAuthData(
                    token = authResponse.token,
                    user = authResponse.user,
                    expiresIn = authResponse.expiresIn
                )
                AuthResult.Success(authResponse)
            } else {
                AuthResult.Error(response.message() ?: "Login failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun register(request: RegisterRequest): AuthResult<User> {
        return try {
            val response = authApi.register(request)
            if (response.isSuccessful && response.body() != null) {
                AuthResult.Success(response.body()!!)
            } else {
                AuthResult.Error(response.message() ?: "Registration failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun logout() {
        tokenManager.clearAuthData()
    }

    suspend fun validateToken(): Boolean {
        val token = tokenManager.getToken() ?: return false
        return try {
            val response = authApi.validateToken(token)
            response.isSuccessful && response.body() == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isTokenExpired(): Boolean {
        return tokenManager.isTokenExpired()
    }
}
