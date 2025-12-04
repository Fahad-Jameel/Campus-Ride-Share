package com.example.campusride

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusride.data.model.Ride
import com.example.campusride.data.repository.RideRepository
import com.example.campusride.data.repository.VehicleRepository
import com.example.campusride.databinding.ActivityOfferRideBinding
import com.example.campusride.util.DateTimePickerHelper
import com.example.campusride.util.SharedPreferencesHelper
import com.example.campusride.util.ValidationUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class OfferRideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOfferRideBinding
    private lateinit var rideRepository: RideRepository
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var prefsHelper: SharedPreferencesHelper
    
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var selectedCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfferRideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rideRepository = RideRepository(this)
        vehicleRepository = VehicleRepository(this)
        prefsHelper = SharedPreferencesHelper(this)

        binding.backButton.setOnClickListener { finish() }

        binding.postRideButton.setOnClickListener {
            submitRide()
        }

        binding.offerSosButton.setOnClickListener {
            // TODO: SOS action
            Toast.makeText(this, "SOS feature coming soon", Toast.LENGTH_SHORT).show()
        }
        
        // Setup date and time pickers
        setupDateTimePickers()

        setupBottomNav()
    }
    
    private fun setupDateTimePickers() {
        // Wire up date button
        binding.dateButton.setOnClickListener {
            showDatePicker()
        }
        
        // Wire up time button
        binding.timeButton.setOnClickListener {
            showTimePicker()
        }
    }
    
    private fun showDatePicker() {
        DateTimePickerHelper.showDatePicker(this, selectedCalendar) { dateString, calendar ->
            selectedDate = dateString
            selectedCalendar = calendar
            // Update UI to show selected date
            binding.dateButton.text = DateTimePickerHelper.formatDateForDisplay(dateString)
        }
    }
    
    private fun showTimePicker() {
        DateTimePickerHelper.showTimePicker(this, selectedCalendar) { timeString, calendar ->
            selectedTime = timeString
            selectedCalendar = calendar
            // Update UI to show selected time
            binding.timeButton.text = DateTimePickerHelper.formatTimeForDisplay(timeString)
        }
    }
    
    private fun submitRide() {
        // Get input values from UI fields
        val pickup = binding.pickupInput.text.toString().trim()
        val destination = binding.destinationInput.text.toString().trim()
        val seats = binding.seatsInput.text.toString().trim()
        val cost = binding.costInput.text.toString().trim()
        
        // Use current date/time if not selected
        if (selectedDate.isEmpty()) {
            selectedDate = DateTimePickerHelper.getCurrentDate()
        }
        if (selectedTime.isEmpty()) {
            selectedTime = DateTimePickerHelper.getCurrentTime()
        }
        
        // Validate inputs
        val pickupError = ValidationUtils.getRequiredFieldError(pickup, "Pickup location")
        if (pickupError != null) {
            Toast.makeText(this, pickupError, Toast.LENGTH_SHORT).show()
            return
        }
        
        val destinationError = ValidationUtils.getRequiredFieldError(destination, "Destination")
        if (destinationError != null) {
            Toast.makeText(this, destinationError, Toast.LENGTH_SHORT).show()
            return
        }
        
        val seatsInt = seats.toIntOrNull() ?: 0
        if (!ValidationUtils.isValidSeats(seatsInt)) {
            Toast.makeText(this, "Please enter valid number of seats (1-8)", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!ValidationUtils.isValidCost(cost)) {
            Toast.makeText(this, "Please enter a valid cost", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get current user ID
        val userId = prefsHelper.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // Create ride object
        val ride = Ride(
            id = UUID.randomUUID().toString(),
            driverId = userId,
            driverName = prefsHelper.getUserEmail() ?: "Unknown",
            pickupLocation = pickup,
            destination = destination,
            date = selectedDate,
            time = selectedTime,
            availableSeats = seatsInt,
            totalSeats = seatsInt,
            cost = cost,
            preferences = emptyList(),
            createdAt = System.currentTimeMillis()
        )
        
        // Show loading state
        binding.postRideButton.isEnabled = false
        binding.postRideButton.text = "Posting..."
        
        // Submit ride
        lifecycleScope.launch {
            val result = rideRepository.createRide(ride)
            
            result.onSuccess {
                Toast.makeText(this@OfferRideActivity, "Ride posted successfully!", Toast.LENGTH_LONG).show()
                finish() // Return to previous screen
            }.onFailure { error ->
                Toast.makeText(this@OfferRideActivity, "Failed to post ride: ${error.message}", Toast.LENGTH_LONG).show()
                binding.postRideButton.isEnabled = true
                binding.postRideButton.text = getString(R.string.offer_post_cta)
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_offer
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_offer -> true
                R.id.nav_find -> {
                    startActivity(Intent(this, FindRideActivity::class.java))
                    true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}


