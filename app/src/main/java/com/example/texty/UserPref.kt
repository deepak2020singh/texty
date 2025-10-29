package com.example.texty

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.texty.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

class UserPreferencesManager(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USERNAME = stringPreferencesKey("username")
        private val EMAIL = stringPreferencesKey("email")
        private val PROFILE_PICTURE_URL = stringPreferencesKey("profile_picture_url")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val NAME = stringPreferencesKey("full_name")
        private val THEME = stringPreferencesKey("theme")
    }

    fun getUserPreferences(): Flow<UserPreferences> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                UserPreferences(
                    userId = preferences[USER_ID] ?: "",
                    username = preferences[USERNAME] ?: "",
                    email = preferences[EMAIL] ?: "",
                    profilePictureUrl = preferences[PROFILE_PICTURE_URL],
                    isLoggedIn = preferences[IS_LOGGED_IN] ?: false,
                    name = preferences[NAME] ?: "",
                    theme = when (preferences[THEME]) {
                        "LIGHT" -> AppTheme.LIGHT
                        "DARK" -> AppTheme.DARK
                        else -> AppTheme.SYSTEM
                    }
                )
            }
    }

    suspend fun saveUserPreferences(user: User) {
        withContext(Dispatchers.IO) {
            try {
                dataStore.edit { preferences ->
                    preferences[USER_ID] = user.userId ?: ""
                    preferences[USERNAME] = user.username ?: ""
                    preferences[EMAIL] = user.email ?: ""
                    preferences[IS_LOGGED_IN] = true
                    preferences[NAME] = user.name ?: ""
                    user.profilePicture?.let { preferences[PROFILE_PICTURE_URL] = it }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                throw e
            }
        }
    }

    suspend fun saveThemePreference(theme: AppTheme) {
        withContext(Dispatchers.IO) {
            try {
                dataStore.edit { preferences ->
                    preferences[THEME] = when (theme) {
                        AppTheme.LIGHT -> "LIGHT"
                        AppTheme.DARK -> "DARK"
                        AppTheme.SYSTEM -> "SYSTEM"
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    suspend fun clearUserPreferences() {
        withContext(Dispatchers.IO) {
            try {
                dataStore.edit { it.clear() }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    suspend fun updateLoginStatus(isLoggedIn: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                dataStore.edit { preferences ->
                    preferences[IS_LOGGED_IN] = isLoggedIn
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

// Separate data class outside the UserPreferencesManager class
data class UserPreferences(
    val userId: String,
    val username: String,
    val email: String,
    val profilePictureUrl: String?,
    val isLoggedIn: Boolean,
    val name: String,
    val theme: AppTheme
)

