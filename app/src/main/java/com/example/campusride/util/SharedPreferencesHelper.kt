package com.example.campusride.util

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "CampusRidePrefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }
    
    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }
    
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    
    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }
    
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    
    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }
    
    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)
    
    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    
    fun saveLastSyncTime(time: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIME, time).apply()
    }
    
    fun getLastSyncTime(): Long = prefs.getLong(KEY_LAST_SYNC_TIME, 0)
    
    fun saveFCMToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }
    
    fun getFCMToken(): String? = prefs.getString(KEY_FCM_TOKEN, null)
    
    fun clear() {
        prefs.edit().clear().apply()
    }
}


