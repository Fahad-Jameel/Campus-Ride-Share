package com.example.campusride.data.repository

import android.content.Context
import com.example.campusride.data.api.RetrofitClient
import com.example.campusride.data.api.NotificationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepository(private val context: Context) {
    private val apiService = RetrofitClient.apiService

    suspend fun getNotifications(userId: String): Result<List<NotificationResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getNotifications(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val notifications = response.body()?.notifications ?: emptyList()
                    Result.success(notifications)
                } else {
                    Result.failure(Exception(response.body()?.error ?: "Failed to get notifications"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun markAsRead(notificationId: String, userId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "notificationId" to notificationId,
                    "userId" to userId
                )
                val response = apiService.markNotificationRead(data)
                if (response.isSuccessful && response.body()?.get("success") == true) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Failed to mark notification as read"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

