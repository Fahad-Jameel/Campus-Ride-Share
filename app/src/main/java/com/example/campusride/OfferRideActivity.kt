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
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.LocationManager

class OfferRideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOfferRideBinding
    private lateinit var rideRepository: RideRepository
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var prefsHelper: SharedPreferencesHelper
    
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var selectedCalendar: Calendar = Calendar.getInstance()
    private var selectedExpiryTime: Long? = null
    
    private var pickupMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var isSelectingPickup = false
    private var isSelectingDestination = false
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

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
        setupExpiryTimePicker()
        setupMap()
        setupMapButtons()
        requestLocationPermission()

        setupBottomNav()
    }
    
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            showCurrentLocation()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCurrentLocation()
            }
        }
    }
    
    private fun showCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    binding.mapView.controller.animateTo(userLocation)
                    
                    val currentMarker = Marker(binding.mapView).apply {
                        position = userLocation
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Your Location"
                    }
                    binding.mapView.overlays.add(currentMarker)
                    binding.mapView.invalidate()
                }
            }
        } catch (e: Exception) {
            // Silently handle
        }
    }
    
    private fun setupMap() {
        Configuration.getInstance().userAgentValue = packageName
        
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        
        val campusLocation = GeoPoint(31.5204, 74.3587)
        binding.mapView.controller.setZoom(13.0)
        binding.mapView.controller.setCenter(campusLocation)
        
        binding.mapView.overlays.add(object : org.osmdroid.views.overlay.Overlay() {
            override fun onSingleTapConfirmed(e: android.view.MotionEvent, mapView: org.osmdroid.views.MapView): Boolean {
                if (isSelectingPickup || isSelectingDestination) {
                    val projection = mapView.projection
                    val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                    
                    if (isSelectingPickup) {
                        setPickupLocation(geoPoint)
                        isSelectingPickup = false
                    } else if (isSelectingDestination) {
                        setDestinationLocation(geoPoint)
                        isSelectingDestination = false
                    }
                    return true
                }
                return false
            }
        })
    }
    
    private fun setupMapButtons() {
        binding.selectPickupButton.setOnClickListener {
            isSelectingPickup = true
            isSelectingDestination = false
            Toast.makeText(this, "Tap map to select pickup location", Toast.LENGTH_SHORT).show()
        }

        binding.selectDestinationButton.setOnClickListener {
            isSelectingPickup = false
            isSelectingDestination = true
            Toast.makeText(this, "Tap map to select destination", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setPickupLocation(geoPoint: GeoPoint) {
        pickupMarker?.let { binding.mapView.overlays.remove(it) }
        
        pickupMarker = Marker(binding.mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Pickup"
        }
        binding.mapView.overlays.add(pickupMarker)
        binding.mapView.invalidate()
        
        binding.pickupInput.setText("Lat: ${String.format("%.4f", geoPoint.latitude)}, Lng: ${String.format("%.4f", geoPoint.longitude)}")
    }

    private fun setDestinationLocation(geoPoint: GeoPoint) {
        destinationMarker?.let { binding.mapView.overlays.remove(it) }
        
        destinationMarker = Marker(binding.mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Destination"
        }
        binding.mapView.overlays.add(destinationMarker)
        binding.mapView.invalidate()
        
        binding.destinationInput.setText("Lat: ${String.format("%.4f", geoPoint.latitude)}, Lng: ${String.format("%.4f", geoPoint.longitude)}")
    }
    
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }
    
    private fun setupExpiryTimePicker() {
        binding.expiryTimeButton.setOnClickListener {
            DateTimePickerHelper.showTimePicker(
                context = this,
                onTimeSelected = { formattedTime, calendar ->
                    selectedExpiryTime = calendar.timeInMillis
                    binding.expiryTimeButton.text = formattedTime
                }
            )
        }
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
        
        // Validate expiry time if set
        if (selectedExpiryTime != null) {
            val departureTimeMillis = selectedCalendar.timeInMillis
            if (selectedExpiryTime!! <= departureTimeMillis) {
                Toast.makeText(this, "Expiry time must be after departure time", Toast.LENGTH_SHORT).show()
                return
            }
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
            expiryTime = selectedExpiryTime,
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


