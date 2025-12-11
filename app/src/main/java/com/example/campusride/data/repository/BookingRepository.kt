package com.example.campusride.data.repository

import android.content.Context
import android.util.Log
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
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // Check if the response indicates success
                        val success = body["success"] as? Boolean ?: false
                        if (success) {
                            Result.success(body)
                        } else {
                            val errorMsg = body["message"] as? String ?: "Failed to update booking"
                            Log.e("BookingRepository", "Update failed: $errorMsg")
                            Result.failure(Exception(errorMsg))
                        }
                    } else {
                        Log.e("BookingRepository", "Response body is null")
                        Result.failure(Exception("No response from server"))
                    }
                } else {
                    val errorBody = try {
                        response.errorBody()?.string() ?: response.message()
                    } catch (e: Exception) {
                        response.message()
                    }
                    Log.e("BookingRepository", "HTTP error: ${response.code()} - $errorBody")
                    Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("BookingRepository", "Exception in updateBookingStatus: ${e.message}", e)
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
