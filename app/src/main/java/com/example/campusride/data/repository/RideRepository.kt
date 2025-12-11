package com.example.campusride.data.repository

import android.content.Context
import com.example.campusride.data.api.ApiService
import com.example.campusride.data.api.RetrofitClient
import com.example.campusride.data.api.*
import com.example.campusride.data.database.CampusRideDatabase
import com.example.campusride.data.model.Ride
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class RideRepository(context: Context) {
    private val apiService: ApiService = RetrofitClient.apiService
    private val db = CampusRideDatabase.getDatabase(context)
    private val rideDao = db.rideDao()
    
    fun getAllRides(): Flow<List<Ride>> = rideDao.getAllRides().map { 
        // Temporarily disable expiry filtering to debug - just sort by latest
        it.sortedByDescending { it.createdAt }
    }
    
    fun searchRides(query: String): Flow<List<Ride>> = rideDao.searchRides(query).map { 
        // Temporarily disable expiry filtering to debug - just sort by latest
        it.sortedByDescending { it.createdAt }
    }
    
    private fun filterExpiredRides(rides: List<Ride>): List<Ride> {
        val currentTime = System.currentTimeMillis()
        return rides.filter { ride ->
            // Check if ride has expired - ride is valid if its date+time is in the future
            // But be lenient - if parsing fails, include the ride
            try {
                val rideDateTime = getRideDateTime(ride)
                // Only filter if we successfully parsed and it's clearly in the past
                if (rideDateTime == Long.MAX_VALUE) {
                    true // Include if parsing failed
                } else {
                    rideDateTime > currentTime
                }
            } catch (e: Exception) {
                true // Include if any error occurs
            }
        }.sortedByDescending { it.createdAt } // Show latest first
    }
    
    private fun getRideDateTime(ride: Ride): Long {
        return try {
            // If expiryTime is set, use it
            if (ride.expiryTime != null) {
                return ride.expiryTime!!
            }
            // Otherwise parse date and time to create timestamp
            // Handle both "HH:mm" and "HH:mm:ss" formats
            val timeStr = if (ride.time.length > 5) {
                ride.time.substring(0, 5) // Take only HH:mm part
            } else {
                ride.time
            }
            val dateTimeStr = "${ride.date} $timeStr"
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            val date = parser.parse(dateTimeStr)
            date?.time ?: Long.MAX_VALUE // If parsing fails, don't filter it out
        } catch (e: Exception) {
            Long.MAX_VALUE // If parsing fails, don't filter it out
        }
    }
    
    suspend fun syncRidesFromServer(
        pickup: String? = null,
        destination: String? = null,
        date: String? = null,
        search: String? = null
    ): Result<List<Ride>> {
        return try {
            android.util.Log.d("RideRepository", "Starting syncRidesFromServer with params: pickup=$pickup, destination=$destination, date=$date, search=$search")
            val response = apiService.getRides(pickup, destination, date, search)
            android.util.Log.d("RideRepository", "API response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("RideRepository", "Response body: success=${body?.success}, rides count=${body?.rides?.size ?: 0}, error=${body?.error}")
                
                if (body != null && body.success == true) {
                    val rides = body.rides?.map { it.toRide() } ?: emptyList()
                    android.util.Log.d("RideRepository", "Converted ${rides.size} rides from API response")
                    
                    if (rides.isNotEmpty()) {
                        val syncedRides = rides.map { it.copy(lastSyncedAt = System.currentTimeMillis()) }
                        try {
                            rideDao.insertRides(syncedRides)
                            // Verify insertion by checking count
                            val count = rideDao.getAllRides().firstOrNull()?.size ?: 0
                            android.util.Log.d("RideRepository", "Synced ${rides.size} rides from server and saved to local DB. Total rides in DB: $count")
                            Result.success(syncedRides)
                        } catch (e: Exception) {
                            android.util.Log.e("RideRepository", "Error inserting rides to database: ${e.message}", e)
                            e.printStackTrace()
                            Result.failure(e)
                        }
                    } else {
                        android.util.Log.w("RideRepository", "No rides returned from server (empty list). This might mean there are no rides in the database.")
                        // Still return success with empty list - this is not an error, just no data
                        Result.success(emptyList())
                    }
                } else {
                    val errorMsg = body?.error ?: "Response body is null or success is false"
                    android.util.Log.e("RideRepository", "Sync failed: $errorMsg, body: $body")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = try {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("RideRepository", "Error body: $errorBody")
                    errorBody ?: response.message() ?: "Unknown error"
                } catch (e: Exception) {
                    android.util.Log.e("RideRepository", "Error reading error body: ${e.message}", e)
                    response.message() ?: "HTTP ${response.code()}"
                }
                android.util.Log.e("RideRepository", "HTTP error: ${response.code()} - $errorMsg")
                Result.failure(Exception("HTTP ${response.code()}: $errorMsg"))
            }
        } catch (e: Exception) {
            android.util.Log.e("RideRepository", "Exception in syncRidesFromServer: ${e.message}", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun createRide(ride: Ride): Result<Ride> {
        return try {
            val response = apiService.createRide(CreateRideRequest(
                driverId = ride.driverId,
                pickupLocation = ride.pickupLocation,
                destination = ride.destination,
                date = ride.date,
                time = ride.time,
                availableSeats = ride.availableSeats,
                totalSeats = ride.totalSeats,
                cost = ride.cost,
                vehicleId = ride.vehicleId,
                vehicleModel = ride.vehicleModel,
                preferences = ride.preferences
            ))
            if (response.isSuccessful && response.body()?.success == true) {
                val rideResponse = response.body()?.ride
                if (rideResponse != null) {
                    val createdRide = rideResponse.toRide().copy(lastSyncedAt = System.currentTimeMillis())
                    rideDao.insertRide(createdRide)
                    Result.success(createdRide)
                } else {
                    Result.failure(Exception("Failed to create ride"))
                }
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to create ride"))
            }
        } catch (e: Exception) {
            // Save locally even if sync fails
            rideDao.insertRide(ride)
            Result.failure(e)
        }
    }
    
    private fun RideResponse.toRide(): Ride {
        return try {
            Ride(
                id = this.id,
                driverId = this.driverId,
                driverName = this.driverName ?: "Unknown",
                driverImageUrl = this.driverImageUrl,
                pickupLocation = this.pickupLocation,
                destination = this.destination,
                date = this.date,
                time = this.time,
                availableSeats = this.availableSeats,
                totalSeats = this.totalSeats,
                cost = this.cost,
                vehicleId = this.vehicleId,
                vehicleModel = this.vehicleModel,
                preferences = this.preferences ?: emptyList(),
                expiryTime = null,
                createdAt = this.createdAt,
                lastSyncedAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            android.util.Log.e("RideRepository", "Error converting RideResponse to Ride: ${e.message}", e)
            throw e
        }
    }
}

