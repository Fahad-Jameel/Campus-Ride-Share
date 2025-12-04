package com.example.campusride

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campusride.databinding.ActivityHomeBinding
import com.example.campusride.databinding.ItemRideCardBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNavigation()
        setupActions()
        setupSuggestedRides()
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

    private fun setupActions() {
        binding.offerRideButton.setOnClickListener {
            startActivity(Intent(this, OfferRideActivity::class.java))
        }
        binding.findRideButton.setOnClickListener {
            startActivity(Intent(this, FindRideActivity::class.java))
        }
    }

    private fun setupSuggestedRides() {
        setRideCard(
            binding.rideCardOne,
            getString(R.string.home_suggested_route_one),
            getString(R.string.home_suggested_route_time_today),
            getString(R.string.home_price_format, "4")
        )
        setRideCard(
            binding.rideCardTwo,
            getString(R.string.home_suggested_route_two),
            getString(R.string.home_suggested_route_time_today_one),
            getString(R.string.home_price_format, "3")
        )
        setRideCard(
            binding.rideCardThree,
            getString(R.string.home_suggested_route_three),
            getString(R.string.home_suggested_route_time_tomorrow),
            getString(R.string.home_price_format, "2")
        )
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
}

