package com.mainstream.app.data.api

import com.mainstream.app.data.model.PredefinedRoute
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RouteApi {
    @GET("api/routes")
    suspend fun getAllRoutes(
        @Query("activeOnly") activeOnly: Boolean = true,
        @Query("city") city: String? = null
    ): Response<List<PredefinedRoute>>

    @GET("api/routes/with-stats")
    suspend fun getAllRoutesWithStats(
        @Query("activeOnly") activeOnly: Boolean = true,
        @Query("city") city: String? = null
    ): Response<List<PredefinedRoute>>

    @GET("api/routes/{id}")
    suspend fun getRouteById(@Path("id") id: Long): Response<PredefinedRoute>
}
