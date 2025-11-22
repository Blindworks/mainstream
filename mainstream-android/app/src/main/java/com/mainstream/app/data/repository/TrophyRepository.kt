package com.mainstream.app.data.repository

import com.mainstream.app.data.api.TrophyApi
import com.mainstream.app.data.local.TokenManager
import com.mainstream.app.data.model.Trophy
import com.mainstream.app.data.model.TrophyProgress
import com.mainstream.app.data.model.UserTrophy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrophyRepository @Inject constructor(
    private val trophyApi: TrophyApi,
    private val tokenManager: TokenManager
) {
    suspend fun getAllTrophies(): Result<List<Trophy>> {
        return try {
            val response = trophyApi.getAllTrophies()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserTrophies(): Result<List<UserTrophy>> {
        return try {
            val userId = tokenManager.getUserId() ?: return Result.failure(Exception("User not logged in"))
            val response = trophyApi.getUserTrophies(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrophyProgress(): Result<List<TrophyProgress>> {
        return try {
            val userId = tokenManager.getUserId() ?: return Result.failure(Exception("User not logged in"))
            val response = trophyApi.getTrophyProgress(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTodaysTrophy(): Result<Trophy> {
        return try {
            val response = trophyApi.getTodaysTrophy()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeeklyTrophies(): Result<List<UserTrophy>> {
        return try {
            val response = trophyApi.getWeeklyTrophies()
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
