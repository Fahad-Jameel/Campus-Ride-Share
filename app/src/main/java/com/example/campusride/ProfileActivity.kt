package com.example.campusride

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.campusride.data.repository.UserRepository
import com.example.campusride.data.repository.VehicleRepository
import com.example.campusride.databinding.ActivityProfileBinding
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var userRepository: UserRepository
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var prefsHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(this)
        vehicleRepository = VehicleRepository(this)
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
        setupBottomNav()
        loadUserProfile()
    }

    override fun onResume() {
        super.onResume()
        // Refresh vehicle data when returning to profile
        val userId = prefsHelper.getUserId()
        if (userId != null) {
            lifecycleScope.launch {
                vehicleRepository.syncVehiclesFromServer(userId)
            }
        }
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
            vehicleRepository.syncVehiclesFromServer(userId)
        }

        // Observe user data from local database
        lifecycleScope.launch {
            userRepository.getUserById(userId).collect { user ->
                if (user != null) {
                    // Update profile name - show dash if empty
                    binding.profileName.text = user.name.ifEmpty { "-" }

                    // Load profile image
                    if (!user.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this@ProfileActivity)
                            .load(user.profileImageUrl)
                            .placeholder(R.drawable.ic_profile_circle_placeholder)
                            .circleCrop()
                            .into(binding.profileAvatar)
                    }

                    // Update email - show dash if empty
                    binding.profileEmailRow.detailSubtitle.text = user.email.ifEmpty { "-" }

                    // Update phone - show dash if empty
                    binding.profilePhoneRow.detailSubtitle.text = user.phone?.ifEmpty { null } ?: "-"
                } else {
                    // Show dashes if user not found
                    binding.profileName.text = "-"
                    binding.profileEmailRow.detailSubtitle.text = "-"
                    binding.profilePhoneRow.detailSubtitle.text = "-"
                }
            }
        }

        // Observe vehicle data from local database
        lifecycleScope.launch {
            vehicleRepository.getVehiclesByUser(userId).collect { vehicles ->
                if (vehicles.isNotEmpty()) {
                    val vehicle = vehicles.first()
                    val makeModel = "${vehicle.make ?: ""} ${vehicle.model ?: ""}".trim()
                    binding.vehicleName.text = if (makeModel.isNotEmpty()) makeModel else "-"
                    
                    val metaParts = mutableListOf<String>()
                    if (vehicle.year > 0) metaParts.add(vehicle.year.toString())
                    if (!vehicle.color.isNullOrEmpty()) metaParts.add(vehicle.color)
                    if (!vehicle.licensePlate.isNullOrEmpty()) metaParts.add(vehicle.licensePlate)
                    binding.vehicleMeta.text = if (metaParts.isNotEmpty()) metaParts.joinToString(" • ") else "-"
                    
                    // Show vehicle card
                    binding.vehicleCard.visibility = android.view.View.VISIBLE
                } else {
                    binding.vehicleName.text = "-"
                    binding.vehicleMeta.text = "-"
                    // Still show vehicle card but with dashes
                    binding.vehicleCard.visibility = android.view.View.VISIBLE
                }
            }
        }

        // Stats - show dashes for now (can be fetched from API later if needed)
        binding.profileRatingValue.text = "-"
        binding.profileRidesValue.text = "-"
        binding.profileCarpoolValue.text = "-"
    }

    private fun performLogout() {
        // Clear user session
        prefsHelper.setLoggedIn(false)
        prefsHelper.saveUserId("")
        prefsHelper.saveUserEmail("")

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
        binding.profileEmailRow.detailSubtitle.text = "-"

        binding.profilePhoneRow.detailIcon.setImageResource(R.drawable.ic_phone)
        binding.profilePhoneRow.detailTitle.text = getString(R.string.profile_phone_label)
        binding.profilePhoneRow.detailSubtitle.text = "-"
    }
}


