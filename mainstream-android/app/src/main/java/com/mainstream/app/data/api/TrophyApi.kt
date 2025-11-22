package com.mainstream.app.data.api

import com.mainstream.app.data.model.Trophy
import com.mainstream.app.data.model.TrophyProgress
import com.mainstream.app.data.model.UserTrophy
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface TrophyApi {
    @GET("api/trophies")
    suspend fun getAllTrophies(): Response<List<Trophy>>

    @GET("api/trophies/my")
    suspend fun getUserTrophies(@Header("X-User-Id") userId: Long): Response<List<UserTrophy>>

    @GET("api/trophies/progress")
    suspend fun getTrophyProgress(@Header("X-User-Id") userId: Long): Response<List<TrophyProgress>>

    @GET("api/trophies/activity/{activityId}")
    suspend fun getTrophiesForActivity(
        @Path("activityId") activityId: Long,
        @Header("X-User-Id") userId: Long
    ): Response<List<UserTrophy>>

    @GET("api/trophies/daily/today")
    suspend fun getTodaysTrophy(): Response<Trophy>

    @GET("api/trophies/daily/today/winners")
    suspend fun getTodaysTrophyWinners(): Response<List<UserTrophy>>

    @GET("api/trophies/weekly")
    suspend fun getWeeklyTrophies(): Response<List<UserTrophy>>
}
