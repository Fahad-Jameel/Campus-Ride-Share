package com.example.campusride

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusride.data.api.NotificationResponse
import com.example.campusride.data.repository.NotificationRepository
import com.example.campusride.databinding.ActivityNotificationsBinding
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsHelper = SharedPreferencesHelper(this)
        notificationRepository = NotificationRepository(this)

        setupUI()
        setupRecyclerView()
        loadNotifications()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notification ->
            // Mark as read when clicked
            markAsRead(notification)
            
            // Navigate based on notification type
            when (notification.type) {
                "booking_request" -> {
                    // Navigate to manage bookings
                    val intent = Intent(this, ManageBookingsActivity::class.java)
                    notification.rideId?.let { intent.putExtra("RIDE_ID", it) }
                    notification.bookingId?.let { intent.putExtra("BOOKING_ID", it) }
                    startActivity(intent)
                }
                "booking_accepted", "booking_rejected" -> {
                    // Navigate to home or booking details
                    val intent = Intent(this, HomeActivity::class.java)
                    notification.bookingId?.let { intent.putExtra("BOOKING_ID", it) }
                    startActivity(intent)
                }
                else -> {
                    // Default to home
                    startActivity(Intent(this, HomeActivity::class.java))
                }
            }
        }
        
        binding.notificationsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notificationsRecyclerView.adapter = adapter
    }

    private fun loadNotifications() {
        val userId = prefsHelper.getUserId()
        if (userId.isNullOrEmpty()) {
            showEmptyState()
            return
        }

        lifecycleScope.launch {
            try {
                val result = notificationRepository.getNotifications(userId)
                result.onSuccess { notifications ->
                    if (notifications.isEmpty()) {
                        showEmptyState()
                    } else {
                        showNotifications(notifications)
                    }
                }.onFailure { error ->
                    Toast.makeText(
                        this@NotificationsActivity,
                        "Failed to load notifications: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyState()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@NotificationsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                showEmptyState()
            }
        }
    }

    private fun showNotifications(notifications: List<NotificationResponse>) {
        binding.emptyView.visibility = View.GONE
        binding.notificationsRecyclerView.visibility = View.VISIBLE
        adapter.submitList(notifications)
    }

    private fun showEmptyState() {
        binding.emptyView.visibility = View.VISIBLE
        binding.notificationsRecyclerView.visibility = View.GONE
    }

    private fun markAsRead(notification: NotificationResponse) {
        if (notification.isRead) return
        
        val userId = prefsHelper.getUserId() ?: return
        
        lifecycleScope.launch {
            notificationRepository.markAsRead(notification.id, userId)
            // Update local list
            val updatedList = adapter.currentList.map { notif ->
                if (notif.id == notification.id) {
                    notif.copy(isRead = true)
                } else {
                    notif
                }
            }
            adapter.submitList(updatedList)
        }
    }
}
