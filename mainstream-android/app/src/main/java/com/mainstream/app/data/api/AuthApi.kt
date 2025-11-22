package com.mainstream.app.data.api

import com.mainstream.app.data.model.AuthResponse
import com.mainstream.app.data.model.LoginRequest
import com.mainstream.app.data.model.RegisterRequest
import com.mainstream.app.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<User>

    @GET("api/auth/user")
    suspend fun getCurrentUser(@Header("X-User-Email") email: String): Response<User>

    @POST("api/auth/validate")
    suspend fun validateToken(@Query("token") token: String): Response<Boolean>
}
