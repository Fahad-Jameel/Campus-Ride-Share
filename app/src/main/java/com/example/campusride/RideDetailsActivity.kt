package com.example.campusride

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.campusride.data.model.Ride
import com.example.campusride.data.repository.BookingRepository
import com.example.campusride.databinding.ActivityRideDetailsBinding
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RideDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRideDetailsBinding
    private lateinit var bookingRepository: BookingRepository
    private lateinit var prefsHelper: SharedPreferencesHelper
    private var ride: Ride? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRideDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookingRepository = BookingRepository(this)
        prefsHelper = SharedPreferencesHelper(this)

        // Get ride data from intent
        ride = intent.getSerializableExtra("RIDE_DATA") as? Ride
        
        if (ride == null) {
            Toast.makeText(this, "Error loading ride details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupListeners()
        checkExistingBooking()
    }

    private fun setupUI() {
        ride?.let { rideData ->
            // Driver info
            binding.driverName.text = rideData.driverName
            binding.vehicleInfo.text = rideData.vehicleModel ?: "Vehicle information not available"
            
            // Load driver photo if available
            if (!rideData.driverImageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(rideData.driverImageUrl)
                    .placeholder(R.drawable.ic_profile_circle_placeholder)
                    .circleCrop()
                    .into(binding.driverAvatar)
            }

            // Ride details
            binding.pickupText.text = rideData.pickupLocation
            binding.destinationText.text = rideData.destination
            
            // Format date and time
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            val dateTimeStr = try {
                val dateTimeInput = "${rideData.date} ${rideData.time}"
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val date = parser.parse(dateTimeInput)
                date?.let { dateFormat.format(it) } ?: "${ rideData.date} ${rideData.time}"
            } catch (e: Exception) {
                "${rideData.date} ${rideData.time}"
            }
            binding.dateTimeText.text = dateTimeStr
            
            binding.seatsText.text = "${rideData.availableSeats} seats available"
            binding.costText.text = "PKR ${rideData.cost}"
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.requestRideButton.setOnClickListener {
            submitBookingRequest()
        }
    }

    private fun submitBookingRequest() {
        val userId = prefsHelper.getUserId()
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val stopLocation = binding.stopLocationInput.text?.toString()?.trim() ?: ""
        val seatsRequested =  binding.seatsRequestedInput.text?.toString()?.toIntOrNull() ?: 1

        // Validate
        if (seatsRequested < 1) {
            Toast.makeText(this, "Please request at least 1 seat", Toast.LENGTH_SHORT).show()
            return
        }

        ride?.let { rideData ->
            if (seatsRequested > rideData.availableSeats) {
                Toast.makeText(this, "Only ${rideData.availableSeats} seats available", Toast.LENGTH_SHORT).show()
                return
            }

            // Show loading
            binding.requestRideButton.isEnabled = false
            binding.requestRideButton.text = "Sending Request..."

            // Create booking
            lifecycleScope.launch {
                try {
                    val booking = mapOf(
                        "ride_id" to rideData.id,
                        "passenger_id" to userId,
                        "seats_requested" to seatsRequested.toString(),
                        "stop_location" to stopLocation,
                        "status" to "pending"
                    )

                    // Submit booking request
                    val result = bookingRepository.createBooking(booking)
                    result.onSuccess {
                        Toast.makeText(
                            this@RideDetailsActivity,
                            "Booking request sent successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Update UI to show pending status
                        showBookingStatus("pending")
                    }.onFailure { error ->
                        Toast.makeText(
                            this@RideDetailsActivity,
                            "Failed to send request: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.requestRideButton.isEnabled = true
                        binding.requestRideButton.text = "Send Booking Request"
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@RideDetailsActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.requestRideButton.isEnabled = true
                    binding.requestRideButton.text = "Send Booking Request"
                }
            }
        }
    }
    
    private fun checkExistingBooking() {
        val userId = prefsHelper.getUserId()
        if (userId.isNullOrEmpty() || ride == null) return
        
        lifecycleScope.launch {
            try {
                val result = bookingRepository.checkPendingBooking(userId, ride!!.id)
                result.onSuccess { existingBooking ->
                    if (existingBooking != null) {
                        // User has pending or accepted booking
                        val status = existingBooking["status"] as? String ?: "pending"
                        showBookingStatus(status)
                    }
                }
            } catch (e: Exception) {
                // Silently fail - user can still book
            }
        }
    }
    
    private fun showBookingStatus(status: String) {
        // Hide booking form
        binding.stopLocationInput.isEnabled = false
        binding.seatsRequestedInput.isEnabled = false
        binding.requestRideButton.isEnabled = false
        
        // Update button text to show status
        binding.requestRideButton.text = when (status) {
            "pending" -> "Request Pending"
            "accepted" -> "Booking Accepted"
            else -> "Send Booking Request"
        }
        
        if (status == "pending" || status == "accepted") {
            Toast.makeText(
                this,
                "You already have a $status booking for this ride",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
