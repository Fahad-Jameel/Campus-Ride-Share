package com.example.campusride

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.campusride.data.repository.RideRepository
import com.example.campusride.data.repository.UserRepository
import com.example.campusride.databinding.ActivityHomeBinding
import com.example.campusride.databinding.ItemRideCardBinding
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import com.example.campusride.data.model.Ride

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var rideRepository: RideRepository
    private lateinit var userRepository: UserRepository
    private lateinit var prefsHelper: SharedPreferencesHelper
    private var currentRides: List<Ride> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(this)
        rideRepository = RideRepository(this)
        prefsHelper = SharedPreferencesHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupGreeting()
        loadUserProfile()
        setupMap()
        setupBottomNavigation()
        setupActions()
        loadAvailableRides()
    }

    private fun setupMap() {
        // Configure osmdroid
        Configuration.getInstance().userAgentValue = packageName
        
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        
        // Set default location (example: Lahore, Pakistan - adjust to your campus)
        val campusLocation = GeoPoint(31.5204, 74.3587) // Lahore coordinates
        binding.mapView.controller.setZoom(15.0)
        binding.mapView.controller.setCenter(campusLocation)
        
        // Optional: Add a marker for campus
        val marker = Marker(binding.mapView)
        marker.position = campusLocation
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Campus Location"
        binding.mapView.overlays.add(marker)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
        
        // Will be updated with user name when profile loads
        binding.profileGreeting.text = greeting
    }

    private fun loadUserProfile() {
        val userId = prefsHelper.getUserId()
        if (userId.isNullOrEmpty()) {
            binding.profileGreeting.text = "${binding.profileGreeting.text}, Guest"
            return
        }

        lifecycleScope.launch {
            try {
                val user = userRepository.getUserById(userId).firstOrNull()
                if (user != null) {
                    // Update greeting with user name
                    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val greeting = when (hour) {
                        in 5..11 -> "Good Morning"
                        in 12..16 -> "Good Afternoon"
                        else -> "Good Evening"
                    }
                    binding.profileGreeting.text = "$greeting, ${user.name}"
                    
                    // Load profile image if available
                    if (!user.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this@HomeActivity)
                            .load(user.profileImageUrl)
                            .placeholder(R.drawable.ic_profile_circle_placeholder)
                            .circleCrop()
                            .into(binding.profileAvatar)
                    }
                } else {
                    binding.profileGreeting.text = "${binding.profileGreeting.text}, User"
                }
            } catch (e: Exception) {
                binding.profileGreeting.text = "${binding.profileGreeting.text}, User"
            }
        }
    }

    private fun loadAvailableRides() {
        lifecycleScope.launch {
            try {
                val allRides = rideRepository.searchRides("").firstOrNull() ?: emptyList()
                
                // Filter only rides with available seats
                val availableRides = allRides.filter { ride ->
                    ride.availableSeats > 0
                }.take(3) // Show top 3 available rides
                
                // Store currentRides for click handlers
                currentRides = availableRides

                // Display rides or show empty state
                if (availableRides.isEmpty()) {
                    // Hide all ride cards if no rides available
                    binding.rideCardOne.root.visibility = View.GONE
                    binding.rideCardTwo.root.visibility = View.GONE
                    binding.rideCardThree.root.visibility = View.GONE
                } else {
                    // Display available rides
                    availableRides.forEachIndexed { index, ride ->
                        val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
                        val rideTime = try {
                            // Parse date and time to create timestamp
                            val dateTimeStr = "${ride.date} ${ride.time}"
                            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val date = parser.parse(dateTimeStr)
                            date?.let { dateFormat.format(it) } ?: "${ride.date} ${ride.time}"
                        } catch (e: Exception) {
                            "${ride.date} ${ride.time}"
                        }
                        
                        when (index) {
                            0 -> {
                                binding.rideCardOne.root.visibility = View.VISIBLE
                                setRideCard(
                                    binding.rideCardOne,
                                    "${ride.pickupLocation} → ${ride.destination}",
                                    "$rideTime • ${ride.availableSeats} seats left",
                                    "PKR ${ride.cost}"
                                )
                                binding.rideCardOne.root.setOnClickListener {
                                    openRideDetails(ride)
                                }
                            }
                            1 -> {
                                binding.rideCardTwo.root.visibility = View.VISIBLE
                                setRideCard(
                                    binding.rideCardTwo,
                                    "${ride.pickupLocation} → ${ride.destination}",
                                    "$rideTime • ${ride.availableSeats} seats left",
                                    "PKR ${ride.cost}"
                                )
                                binding.rideCardTwo.root.setOnClickListener {
                                    openRideDetails(ride)
                                }
                            }
                            2 -> {
                                binding.rideCardThree.root.visibility = View.VISIBLE
                                setRideCard(
                                    binding.rideCardThree,
                                    "${ride.pickupLocation} → ${ride.destination}",
                                    "$rideTime • ${ride.availableSeats} seats left",
                                    "PKR ${ride.cost}"
                                )
                                binding.rideCardThree.root.setOnClickListener {
                                    openRideDetails(ride)
                                }
                            }
                        }
                    }
                    
                    // Hide unused cards
                    if (availableRides.size < 3) binding.rideCardThree.root.visibility = View.GONE
                    if (availableRides.size < 2) binding.rideCardTwo.root.visibility = View.GONE
                }
            } catch (e: Exception) {
                // On error, hide all ride cards
                binding.rideCardOne.root.visibility = View.GONE
                binding.rideCardTwo.root.visibility = View.GONE
                binding.rideCardThree.root.visibility = View.GONE
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
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
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupListeners() { // Renamed from setupActions()
        // Notification bell click
        binding.notificationIcon.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        
        binding.offerRideButton.setOnClickListener {
            startActivity(Intent(this, OfferRideActivity::class.java))
        }
        binding.findRideButton.setOnClickListener {
            startActivity(Intent(this, FindRideActivity::class.java))
        }
    }

    private fun setRideCard(
        cardBinding: ItemRideCardBinding,
        route: String,
        details: String,
        price: String
    ) {
        cardBinding.rideRoute.text = route
        cardBinding.rideDetails.text = details
        cardBinding.ridePrice.text = price
    }
    
    private fun openRideDetails(ride: Ride) {
        val intent = Intent(this, RideDetailsActivity::class.java)
        intent.putExtra("RIDE_DATA", ride)
        startActivity(intent)
    }
}
