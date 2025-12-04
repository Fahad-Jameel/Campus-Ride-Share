package com.example.campusride

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.campusride.data.repository.UserRepository
import com.example.campusride.databinding.ActivityProfileBinding
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var userRepository: UserRepository
    private lateinit var prefsHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(this)
        prefsHelper = SharedPreferencesHelper(this)

        binding.backButton.setOnClickListener { finish() }
        binding.settingsButton.setOnClickListener {
            // TODO: Navigate to settings
            Toast.makeText(this, "Settings feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.quickOfferButton.setOnClickListener {
            startActivity(Intent(this, OfferRideActivity::class.java))
        }
        binding.quickFindButton.setOnClickListener {
            startActivity(Intent(this, FindRideActivity::class.java))
        }
        binding.editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        binding.logoutButton.setOnClickListener {
            performLogout()
        }

        configureDetailItems()
        configurePreferenceItems()
        setupBottomNav()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = prefsHelper.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            // Try to sync from server first
            userRepository.syncUserFromServer(userId)

            // Observe user data from local database
            userRepository.getUserById(userId).collect { user ->
                if (user != null) {
                    // Update profile name
                    binding.profileName.text = user.name

                    // Load profile image
                    if (!user.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this@ProfileActivity)
                            .load(user.profileImageUrl)
                            .placeholder(R.drawable.ic_profile_circle_placeholder)
                            .circleCrop()
                            .into(binding.profileAvatar)
                    }

                    // Update email
                    binding.profileEmailRow.detailSubtitle.text = user.email

                    // Update phone
                    if (!user.phone.isNullOrEmpty()) {
                        binding.profilePhoneRow.detailSubtitle.text = user.phone
                    }

                    // Update affiliation
                    if (!user.affiliation.isNullOrEmpty()) {
                        // You can add affiliation display if needed
                    }
                }
            }
        }
    }

    private fun performLogout() {
        // Clear user session
        prefsHelper.setLoggedIn(false)
        prefsHelper.saveUserId(null)
        prefsHelper.saveUserEmail(null)

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_profile
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_offer -> {
                    startActivity(Intent(this, OfferRideActivity::class.java))
                    true
                }
                R.id.nav_find -> {
                    startActivity(Intent(this, FindRideActivity::class.java))
                    true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatsActivity::class.java))
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun configureDetailItems() {
        binding.profileEmailRow.detailIcon.setImageResource(R.drawable.ic_mail)
        binding.profileEmailRow.detailTitle.text = getString(R.string.profile_email_label)
        binding.profileEmailRow.detailSubtitle.text = getString(R.string.profile_email_value)

        binding.profilePhoneRow.detailIcon.setImageResource(R.drawable.ic_phone)
        binding.profilePhoneRow.detailTitle.text = getString(R.string.profile_phone_label)
        binding.profilePhoneRow.detailSubtitle.text = getString(R.string.profile_phone_value)

        binding.profileSafetyRow.detailIcon.setImageResource(R.drawable.ic_shield_info)
        binding.profileSafetyRow.detailTitle.text = getString(R.string.profile_safety_label)
        binding.profileSafetyRow.detailSubtitle.text = getString(R.string.profile_safety_value)

        binding.profilePaymentRow.detailIcon.setImageResource(R.drawable.ic_payment)
        binding.profilePaymentRow.detailTitle.text = getString(R.string.profile_payment_label)
        binding.profilePaymentRow.detailSubtitle.text = getString(R.string.profile_payment_value)
    }

    private fun configurePreferenceItems() {
        binding.profileMusicRow.prefIcon.setImageResource(R.drawable.ic_music)
        binding.profileMusicRow.prefTitle.text = getString(R.string.profile_preferences_music_label)
        binding.profileMusicRow.prefSubtitle.text = getString(R.string.profile_preferences_music_value)

        binding.profileFoodRow.prefIcon.setImageResource(R.drawable.ic_food)
        binding.profileFoodRow.prefTitle.text = getString(R.string.profile_preferences_food_label)
        binding.profileFoodRow.prefSubtitle.text = getString(R.string.profile_preferences_food_value)

        binding.profileNotificationsRow.prefIcon.setImageResource(R.drawable.ic_notifications)
        binding.profileNotificationsRow.prefTitle.text = getString(R.string.profile_preferences_notifications_label)
        binding.profileNotificationsRow.prefSubtitle.text = getString(R.string.profile_preferences_notifications_value)
    }
}


