package com.mainstream.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.mainstream.app.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val dataStore = context.dataStore

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_KEY = stringPreferencesKey("user_data")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val TOKEN_EXPIRY_KEY = longPreferencesKey("token_expiry")
    }

    val token: Flow<String?> = dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    val user: Flow<User?> = dataStore.data.map { prefs ->
        prefs[USER_KEY]?.let { gson.fromJson(it, User::class.java) }
    }

    val userId: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[USER_ID_KEY]
    }

    suspend fun saveAuthData(token: String, user: User, expiresIn: Long) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_KEY] = gson.toJson(user)
            prefs[USER_ID_KEY] = user.id
            prefs[TOKEN_EXPIRY_KEY] = System.currentTimeMillis() + expiresIn
        }
    }

    suspend fun getToken(): String? {
        return token.first()
    }

    suspend fun getUserId(): Long? {
        return userId.first()
    }

    suspend fun getUser(): User? {
        return user.first()
    }

    suspend fun isTokenExpired(): Boolean {
        val expiry = dataStore.data.map { it[TOKEN_EXPIRY_KEY] }.first()
        return expiry?.let { it < System.currentTimeMillis() } ?: true
    }

    suspend fun clearAuthData() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(TOKEN_EXPIRY_KEY)
        }
    }
}
