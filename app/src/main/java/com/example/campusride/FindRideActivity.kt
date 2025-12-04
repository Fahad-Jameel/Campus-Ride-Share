package com.example.campusride

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusride.adapter.RideAdapter
import com.example.campusride.data.repository.RideRepository
import com.example.campusride.databinding.ActivityFindRideBinding
import kotlinx.coroutines.launch

class FindRideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindRideBinding
    private lateinit var rideRepository: RideRepository
    private lateinit var rideAdapter: RideAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindRideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rideRepository = RideRepository(this)

        binding.backButton.setOnClickListener { finish() }

        binding.searchRidesButton.setOnClickListener {
            performSearch()
        }

        binding.sosButton.setOnClickListener {
            // TODO: Implement SOS functionality
            Toast.makeText(this, "SOS feature coming soon", Toast.LENGTH_SHORT).show()
        }

        setupRecyclerView()
        setupBottomNav()
        loadRides()
    }

    private fun setupRecyclerView() {
        rideAdapter = RideAdapter { ride ->
            // Handle ride click - navigate to ride details
            Toast.makeText(this, "Selected ride: ${ride.pickupLocation} → ${ride.destination}", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to ride details activity
        }
        
        // Note: You'll need to add a RecyclerView to the layout
        // For now, we'll load rides and they can be displayed when RecyclerView is added
    }

    private fun loadRides() {
        lifecycleScope.launch {
            // Sync rides from server
            rideRepository.syncRidesFromServer()
            
            // Observe rides from local database
            rideRepository.getAllRides().collect { rides ->
                rideAdapter.submitList(rides)
                
                // Display first 3 rides in the existing cards for now
                if (rides.isNotEmpty()) {
                    displayRidesInCards(rides.take(3))
                }
            }
        }
    }

    private fun performSearch() {
        lifecycleScope.launch {
            try {
                // Get search query (you can add EditText fields for these)
                val pickup = "" // Get from input field
                val destination = "" // Get from input field
                val date = "" // Get from date picker
                
                // Sync rides with search parameters
                val result = rideRepository.syncRidesFromServer(
                    pickup = pickup.ifEmpty { null },
                    destination = destination.ifEmpty { null },
                    date = date.ifEmpty { null }
                )
                
                result.onSuccess {
                    Toast.makeText(this@FindRideActivity, "Found ${it.size} rides", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Toast.makeText(this@FindRideActivity, "Search failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayRidesInCards(rides: List<com.example.campusride.data.model.Ride>) {
        // Display rides in existing card views
        rides.forEachIndexed { index, ride ->
            when (index) {
                0 -> {
                    binding.findRideCard1.rideRoute.text = "${ride.pickupLocation} → ${ride.destination}"
                    binding.findRideCard1.rideDetails.text = "${ride.driverName} • ${ride.date} at ${ride.time} • ${ride.availableSeats} seats"
                    binding.findRideCard1.ridePrice.text = "Rs. ${ride.cost}"
                }
                1 -> {
                    binding.findRideCard2.rideRoute.text = "${ride.pickupLocation} → ${ride.destination}"
                    binding.findRideCard2.rideDetails.text = "${ride.driverName} • ${ride.date} at ${ride.time} • ${ride.availableSeats} seats"
                    binding.findRideCard2.ridePrice.text = "Rs. ${ride.cost}"
                }
                2 -> {
                    binding.findRideCard3.rideRoute.text = "${ride.pickupLocation} → ${ride.destination}"
                    binding.findRideCard3.rideDetails.text = "${ride.driverName} • ${ride.date} at ${ride.time} • ${ride.availableSeats} seats"
                    binding.findRideCard3.ridePrice.text = "Rs. ${ride.cost}"
                }
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_find
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
                R.id.nav_find -> true
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
