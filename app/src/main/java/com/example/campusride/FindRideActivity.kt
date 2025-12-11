package com.example.campusride

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.campusride.data.model.Ride
import com.example.campusride.data.repository.RideRepository
import com.example.campusride.databinding.ActivityFindRideBinding
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.LocationManager
import com.example.campusride.util.DateTimePickerHelper

class FindRideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindRideBinding
    private lateinit var rideRepository: RideRepository
    
    private var pickupMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var isSelectingPickup = false
    private var isSelectingDestination = false
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFindRideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rideRepository = RideRepository(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupMap()
        setupListeners()
        setupDateTimePickers()
        loadAvailableRides()
        requestLocationPermission()
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
                    
                    // Add marker for current location
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
            // Silently handle - will use default location
        }
    }
    
    private fun setupDateTimePickers() {
        binding.dateButton.setOnClickListener {
            DateTimePickerHelper.showDatePicker(this) { formattedDate, calendar ->
                selectedDate = formattedDate
                binding.dateButton.text = formattedDate
            }
        }
        
        binding.timeButton.setOnClickListener {
            DateTimePickerHelper.showTimePicker(this) { formattedTime, calendar ->
                selectedTime = formattedTime
                binding.timeButton.text = formattedTime
            }
        }
    }

    private fun setupMap() {
        Configuration.getInstance().userAgentValue = packageName
        
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        
        // Default campus location
        val campusLocation = GeoPoint(31.5204, 74.3587)
        binding.mapView.controller.setZoom(13.0)
        binding.mapView.controller.setCenter(campusLocation)
        
        // Map tap listener for location selection
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

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

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

        binding.searchRidesButton.setOnClickListener {
            searchRides()
        }
    }

    private fun setPickupLocation(geoPoint: GeoPoint) {
        // Remove old marker
        pickupMarker?.let { binding.mapView.overlays.remove(it) }
        
        // Add new marker
        pickupMarker = Marker(binding.mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Pickup"
        }
        binding.mapView.overlays.add(pickupMarker)
        binding.mapView.invalidate()
        
        // Update input field
        binding.pickupInput.setText("Lat: ${String.format("%.4f", geoPoint.latitude)}, Lng: ${String.format("%.4f", geoPoint.longitude)}")
    }

    private fun setDestinationLocation(geoPoint: GeoPoint) {
        // Remove old marker
        destinationMarker?.let { binding.mapView.overlays.remove(it) }
        
        // Add new marker
        destinationMarker = Marker(binding.mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Destination"
        }
        binding.mapView.overlays.add(destinationMarker)
        binding.mapView.invalidate()
        
        // Update input field
        binding.destinationInput.setText("Lat: ${String.format("%.4f", geoPoint.latitude)}, Lng: ${String.format("%.4f", geoPoint.longitude)}")
    }

    private fun loadAvailableRides() {
        lifecycleScope.launch {
            try {
                // First sync rides from server (wait for it to complete)
                val syncResult = rideRepository.syncRidesFromServer()
                syncResult.onSuccess { syncedRides ->
                    android.util.Log.d("FindRideActivity", "Synced ${syncedRides.size} rides from server")
                    // Sync successful, now load from local database
                    loadRidesFromLocal()
                }.onFailure { error ->
                    // Log error but still try to load from local database
                    android.util.Log.e("FindRideActivity", "Sync failed: ${error.message}")
                    // Still try to load from local database
                    loadRidesFromLocal()
                }
            } catch (e: Exception) {
                android.util.Log.e("FindRideActivity", "Exception in loadAvailableRides: ${e.message}", e)
                // If sync fails, still try to load from local database
                loadRidesFromLocal()
            }
        }
    }
    
    private suspend fun loadRidesFromLocal() {
        try {
            android.util.Log.d("FindRideActivity", "Loading rides from local database...")
            val allRides = rideRepository.searchRides("").firstOrNull() ?: emptyList()
            android.util.Log.d("FindRideActivity", "Loaded ${allRides.size} rides from local database")
            
            if (allRides.isEmpty()) {
                android.util.Log.w("FindRideActivity", "No rides found in local database. This might mean:")
                android.util.Log.w("FindRideActivity", "1. The API sync failed or returned no rides")
                android.util.Log.w("FindRideActivity", "2. The database is empty")
                android.util.Log.w("FindRideActivity", "3. There are no rides in the server database")
            }
            
            val filteredRides = filterActiveRides(allRides).take(3)
            android.util.Log.d("FindRideActivity", "Found ${filteredRides.size} active rides after filtering (out of ${allRides.size} total)")
            
            if (filteredRides.isEmpty()) {
                android.util.Log.d("FindRideActivity", "No active rides found after filtering")
                hideAllRideCards()
            } else {
                displayRides(filteredRides)
            }
        } catch (e: Exception) {
            android.util.Log.e("FindRideActivity", "Error loading rides from local: ${e.message}", e)
            e.printStackTrace()
            hideAllRideCards()
            // Only show error if it's a real error, not just empty results
            if (e.message?.contains("database") == true || e.message?.contains("SQL") == true) {
                Toast.makeText(this@FindRideActivity, "Error loading rides: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchRides() {
        val pickup = binding.pickupInput.text?.toString()?.trim()
        val destination = binding.destinationInput.text?.toString()?.trim()

        lifecycleScope.launch {
            try {
                val allRides = rideRepository.searchRides("").firstOrNull() ?: emptyList()
                val filteredRides = filterAndSortRides(allRides, pickup, destination).take(3)
                
                if (filteredRides.isEmpty()) {
                    Toast.makeText(this@FindRideActivity, "No rides found", Toast.LENGTH_SHORT).show()
                    hideAllRideCards()
                } else {
                    displayRides(filteredRides)
                    Toast.makeText(this@FindRideActivity, "Found ${filteredRides.size} rides", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FindRideActivity, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayRides(rides: List<Ride>) {
        // Hide all cards first
        hideAllRideCards()
        
        // Show rides
        rides.forEachIndexed { index, ride ->
            val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
            val rideTime = try {
                // Handle both "HH:mm" and "HH:mm:ss" formats
                val timeStr = if (ride.time.length > 5) {
                    ride.time.substring(0, 5) // Take only HH:mm part
                } else {
                    ride.time
                }
                val dateTimeStr = "${ride.date} $timeStr"
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val date = parser.parse(dateTimeStr)
                date?.let { dateFormat.format(it) } ?: "${ride.date} ${ride.time}"
            } catch (e: Exception) {
                "${ride.date} ${ride.time}"
            }
            
            when (index) {
                0 -> {
                    binding.findRideCard1.root.visibility = View.VISIBLE
                    binding.findRideCard1.rideRoute.text = "${ride.pickupLocation} → ${ride.destination}"
                    binding.findRideCard1.rideDetails.text = "$rideTime • ${ride.availableSeats} seats"
                    binding.findRideCard1.ridePrice.text = "PKR ${ride.cost}"
                    binding.findRideCard1.root.setOnClickListener {
                        openRideDetails(ride)
                    }
                }
                1 -> {
                    binding.findRideCard2.root.visibility = View.VISIBLE
                    binding.findRideCard2.rideRoute.text = "${ride.pickupLocation} → ${ride.destination}"
                    binding.findRideCard2.rideDetails.text = "$rideTime • ${ride.availableSeats} seats"
                    binding.findRideCard2.ridePrice.text = "PKR ${ride.cost}"
                    binding.findRideCard2.root.setOnClickListener {
                        openRideDetails(ride)
                    }
                }
                2 -> {
                    binding.findRideCard3.root.visibility = View.VISIBLE
                    binding.findRideCard3.rideRoute.text = "${ride.pickupLocation} → ${ride.destination}"
                    binding.findRideCard3.rideDetails.text = "$rideTime • ${ride.availableSeats} seats"
                    binding.findRideCard3.ridePrice.text = "PKR ${ride.cost}"
                    binding.findRideCard3.root.setOnClickListener {
                        openRideDetails(ride)
                    }
                }
            }
        }
    }

    private fun hideAllRideCards() {
        binding.findRideCard1.root.visibility = View.GONE
        binding.findRideCard2.root.visibility = View.GONE
        binding.findRideCard3.root.visibility = View.GONE
    }

    private fun filterActiveRides(rides: List<Ride>): List<Ride> {
        val now = System.currentTimeMillis()
        return rides.filter { ride ->
            ride.availableSeats > 0 && isRideNotExpired(ride, now)
        }.sortedByDescending { it.createdAt } // Show latest first
    }

    private fun filterAndSortRides(
        allRides: List<Ride>,
        pickup: String?,
        destination: String?
    ): List<Ride> {
        val now = System.currentTimeMillis()
        
        // Filter out expired and full rides
        val activeRides = allRides.filter { ride ->
            ride.availableSeats > 0 && isRideNotExpired(ride, now)
        }.sortedByDescending { it.createdAt } // Show latest first
        
        if (pickup.isNullOrEmpty() && destination.isNullOrEmpty()) {
            return activeRides
        }
        
        // Categorize rides by match quality
        val exactMatches = mutableListOf<Ride>()
        val pickupMatches = mutableListOf<Ride>()
        val destMatches = mutableListOf<Ride>()
        val others = mutableListOf<Ride>()
        
        activeRides.forEach { ride ->
            val pickupMatch = pickup?.let { 
                ride.pickupLocation.contains(it, ignoreCase = true) 
            } ?: false
            val destMatch = destination?.let { 
                ride.destination.contains(it, ignoreCase = true) 
            } ?: false
            
            when {
                pickupMatch && destMatch -> exactMatches.add(ride)
                pickupMatch -> pickupMatches.add(ride)
                destMatch -> destMatches.add(ride)
                else -> others.add(ride)
            }
        }
        
        // Return in priority order
        return exactMatches + pickupMatches + destMatches + others
    }
    
    private fun openRideDetails(ride: Ride) {
        val intent = Intent(this, RideDetailsActivity::class.java)
        intent.putExtra("RIDE_DATA", ride)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }
    
    private fun isRideNotExpired(ride: Ride, currentTime: Long): Boolean {
        // Check expiryTime if set
        if (ride.expiryTime != null) {
            return ride.expiryTime!! > currentTime
        }
        
        // Otherwise check date+time
        // Handle both "HH:mm" and "HH:mm:ss" formats
        return try {
            val timeStr = if (ride.time.length > 5) {
                ride.time.substring(0, 5) // Take only HH:mm part
            } else {
                ride.time
            }
            val dateTimeStr = "${ride.date} $timeStr"
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val rideDate = parser.parse(dateTimeStr)
            (rideDate?.time ?: Long.MAX_VALUE) > currentTime
        } catch (e: Exception) {
            true // If parsing fails, don't filter it out
        }
    }
}
