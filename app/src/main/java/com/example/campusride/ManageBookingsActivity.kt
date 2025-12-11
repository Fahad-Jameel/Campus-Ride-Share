package com.example.campusride

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusride.adapter.BookingRequestAdapter
import com.example.campusride.data.repository.BookingRepository
import com.example.campusride.databinding.ActivityManageBookingsBinding
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.launch

class ManageBookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageBookingsBinding
    private lateinit var bookingRepository: BookingRepository
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var adapter: BookingRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookingRepository = BookingRepository(this)
        prefsHelper = SharedPreferencesHelper(this)

        setupRecyclerView()
        setupListeners()
        loadBookingRequests()
    }

    private fun setupRecyclerView() {
        adapter = BookingRequestAdapter(
            onAccept = { booking -> acceptBooking(booking) },
            onReject = { booking -> rejectBooking(booking) }
        )
        
        binding.bookingsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookingsRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadBookingRequests() {
        val userId = prefsHelper.getUserId()
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val result = bookingRepository.getBookingsByDriver(userId)
                result.onSuccess { bookings ->
                    val pendingBookings = bookings.filter { 
                        it["status"] == "pending" 
                    }
                    
                    if (pendingBookings.isEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.bookingsRecyclerView.visibility = View.GONE
                    } else {
                        adapter.submitList(pendingBookings)
                        binding.bookingsRecyclerView.visibility = View.VISIBLE
                        binding.emptyView.visibility = View.GONE
                    }
                }.onFailure {
                    Toast.makeText(
                        this@ManageBookingsActivity,
                        "Failed to load bookings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ManageBookingsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun acceptBooking(booking: Map<String, Any>) {
        val bookingId = booking["id"] as? String ?: return
        
        lifecycleScope.launch {
            try {
                android.util.Log.d("ManageBookingsActivity", "Accepting booking: $bookingId")
                val result = bookingRepository.updateBookingStatus(bookingId, "accepted")
                result.onSuccess {
                    android.util.Log.d("ManageBookingsActivity", "Booking accepted successfully")
                    Toast.makeText(
                        this@ManageBookingsActivity,
                        "Booking accepted!",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadBookingRequests() // Reload list
                }.onFailure { error ->
                    android.util.Log.e("ManageBookingsActivity", "Failed to accept booking: ${error.message}")
                    Toast.makeText(
                        this@ManageBookingsActivity,
                        "Failed to accept booking: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("ManageBookingsActivity", "Exception accepting booking: ${e.message}", e)
                Toast.makeText(
                    this@ManageBookingsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun rejectBooking(booking: Map<String, Any>) {
        val bookingId = booking["id"] as? String ?: return
        
        // Show dialog to get rejection reason
        val input = android.widget.EditText(this)
        input.hint = "Reason for rejection (optional)"
        
        AlertDialog.Builder(this)
            .setTitle("Reject Booking")
            .setMessage("Please provide a reason for rejection:")
            .setView(input)
            .setPositiveButton("Reject") { _, _ ->
                val reason = input.text.toString()
                performReject(bookingId, reason)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performReject(bookingId: String, reason: String) {
        lifecycleScope.launch {
            try {
                val result = bookingRepository.updateBookingStatus(bookingId, "rejected", reason)
                result.onSuccess {
                    Toast.makeText(
                        this@ManageBookingsActivity,
                        "Booking rejected",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadBookingRequests() // Reload list
                }.onFailure {
                    Toast.makeText(
                        this@ManageBookingsActivity,
                        "Failed to reject booking",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ManageBookingsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
