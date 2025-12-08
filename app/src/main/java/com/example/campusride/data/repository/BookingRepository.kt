package com.example.campusride.data.repository

import android.content.Context
import com.example.campusride.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookingRepository(private val context: Context) {

    private val apiService = RetrofitClient.apiService

    suspend fun createBooking(bookingData: Map<String, String>): Result<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createBooking(bookingData)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to create booking: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getBookingsByPassenger(passengerId: String): Result<List<Map<String, Any>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBookingsByPassenger(passengerId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to get bookings"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String, reason: String = ""): Result<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            try {
                val data = if (reason.isNotEmpty()) {
                    mapOf("status" to status, "rejection_reason" to reason)
                } else {
                    mapOf("status" to status)
                }
                val response = apiService.updateBookingStatus(bookingId, data)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to update booking"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun checkPendingBooking(passengerId: String, rideId: String): Result<Map<String, Any>?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBookingsByPassenger(passengerId)
                if (response.isSuccessful && response.body() != null) {
                    // Find booking for this specific ride
                    val bookings = response.body()!!
                    val existingBooking = bookings.find { booking ->
                        booking["ride_id"] == rideId && 
                        (booking["status"] == "pending" || booking["status"] == "accepted")
                    }
                    Result.success(existingBooking)
                } else {
                    Result.success(null)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getBookingsByDriver(driverId: String): Result<List<Map<String, Any>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBookingsByDriver(driverId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to get bookings"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
