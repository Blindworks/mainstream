package com.mainstream.app.data.repository

import com.mainstream.app.data.api.RouteApi
import com.mainstream.app.data.model.PredefinedRoute
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepository @Inject constructor(
    private val routeApi: RouteApi
) {
    suspend fun getAllRoutes(activeOnly: Boolean = true, city: String? = null): Result<List<PredefinedRoute>> {
        return try {
            val response = routeApi.getAllRoutes(activeOnly, city)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllRoutesWithStats(activeOnly: Boolean = true, city: String? = null): Result<List<PredefinedRoute>> {
        return try {
            val response = routeApi.getAllRoutesWithStats(activeOnly, city)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRouteById(id: Long): Result<PredefinedRoute> {
        return try {
            val response = routeApi.getRouteById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
