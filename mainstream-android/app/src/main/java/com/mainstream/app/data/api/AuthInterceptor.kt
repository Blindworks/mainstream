package com.mainstream.app.data.api

import com.mainstream.app.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // Skip auth for login/register endpoints
        if (path.contains("/api/auth/login") || path.contains("/api/auth/register")) {
            return chain.proceed(request)
        }

        // Add auth headers
        val token = runBlocking { tokenManager.getToken() }
        val userId = runBlocking { tokenManager.getUserId() }

        val newRequest = request.newBuilder().apply {
            token?.let {
                addHeader("Authorization", "Bearer $it")
            }
            userId?.let {
                addHeader("X-User-Id", it.toString())
            }
        }.build()

        return chain.proceed(newRequest)
    }
}
