package com.example.campusride

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM Token: $token")
        
        // Save token locally
        val prefsHelper = SharedPreferencesHelper(this)
        prefsHelper.saveFCMToken(token)
        
        // Send token to server if user is logged in
        val userId = prefsHelper.getUserId()
        if (!userId.isNullOrEmpty()) {
            sendTokenToServer(userId, token)
        }
    }
    
    private fun sendTokenToServer(userId: String, token: String) {
        // Use coroutines to send token to server
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = com.example.campusride.data.api.RetrofitClient.apiService
                val data = mapOf(
                    "userId" to userId,
                    "fcmToken" to token
                )
                val response = apiService.saveFCMToken(data)
                if (response.isSuccessful) {
                    Log.d(TAG, "FCM token saved to server successfully")
                } else {
                    Log.e(TAG, "Failed to save FCM token to server")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving FCM token to server: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Message from: ${message.from}")
        
        // Check if message contains data payload
        message.data.isNotEmpty().let {
            Log.d(TAG, "Message data: ${message.data}")
            
            val type = message.data["type"]
            val title = message.data["title"] ?: "Campus Ride"
            val body = message.data["body"] ?: ""
            
            when (type) {
                "booking_request" -> {
                    val bookingId = message.data["booking_id"]
                    val rideId = message.data["ride_id"]
                    showBookingRequestNotification(title, body, bookingId, rideId)
                }
                "booking_accepted" -> {
                    val bookingId = message.data["booking_id"]
                    showBookingStatusNotification(title, body, bookingId, true)
                }
                "booking_rejected" -> {
                    val bookingId = message.data["booking_id"]
                    val reason = message.data["reason"]
                    val fullBody = if (!reason.isNullOrEmpty()) "$body\nReason: $reason" else body
                    showBookingStatusNotification(title, fullBody, bookingId, false)
                }
                else -> {
                    showGeneralNotification(title, body)
                }
            }
        }
        
        // Check if message contains notification payload
        message.notification?.let {
            Log.d(TAG, "Notification title: ${it.title}")
            Log.d(TAG, "Notification body: ${it.body}")
            showGeneralNotification(it.title ?: "Campus Ride", it.body ?: "")
        }
    }

    private fun showBookingRequestNotification(title: String, body: String, bookingId: String?, rideId: String?) {
        val intent = Intent(this, ManageBookingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            bookingId?.let { putExtra("BOOKING_ID", it) }
            rideId?.let { putExtra("RIDE_ID", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        showNotification(title, body, pendingIntent, CHANNEL_BOOKING_REQUESTS)
    }

    private fun showBookingStatusNotification(title: String, body: String, bookingId: String?, accepted: Boolean) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            bookingId?.let { putExtra("BOOKING_ID", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        showNotification(title, body, pendingIntent, CHANNEL_BOOKING_STATUS)
    }

    private fun showGeneralNotification(title: String, body: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        showNotification(title, body, pendingIntent, CHANNEL_GENERAL)
    }

    private fun showNotification(title: String, body: String, pendingIntent: PendingIntent, channelId: String) {
        createNotificationChannel(channelId)
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = when (channelId) {
                CHANNEL_BOOKING_REQUESTS -> "Booking Requests"
                CHANNEL_BOOKING_STATUS -> "Booking Status"
                else -> "General"
            }
            
            val descriptionText = when (channelId) {
                CHANNEL_BOOKING_REQUESTS -> "Notifications for new booking requests"
                CHANNEL_BOOKING_STATUS -> "Notifications for booking status updates"
                else -> "General notifications"
            }
            
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_BOOKING_REQUESTS = "booking_requests"
        private const val CHANNEL_BOOKING_STATUS = "booking_status"
        private const val CHANNEL_GENERAL = "general"
    }
}
