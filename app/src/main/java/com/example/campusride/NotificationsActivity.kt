package com.example.campusride

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusride.databinding.ActivityNotificationsBinding
import com.example.campusride.util.SharedPreferencesHelper

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var prefsHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsHelper = SharedPreferencesHelper(this)

        setupUI()
        loadNotifications()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadNotifications() {
        // For now, show empty state
        // In future, this would load from backend or local database
        showEmptyState()
    }

    private fun showEmptyState() {
        binding.emptyView.visibility = View.VISIBLE
        binding.notificationsRecyclerView.visibility = View.GONE
    }
}
