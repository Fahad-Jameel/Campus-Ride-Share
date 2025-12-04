package com.example.campusride.data.repository

import android.content.Context
import com.example.campusride.data.api.ApiService
import com.example.campusride.data.api.RetrofitClient
import com.example.campusride.data.api.*
import com.example.campusride.data.database.CampusRideDatabase
import com.example.campusride.data.model.Ride
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RideRepository(context: Context) {
    private val apiService: ApiService = RetrofitClient.apiService
    private val db = CampusRideDatabase.getDatabase(context)
    private val rideDao = db.rideDao()
    
    fun getAllRides(): Flow<List<Ride>> = rideDao.getAllRides()
    
    fun searchRides(query: String): Flow<List<Ride>> = rideDao.searchRides(query)
    
    suspend fun syncRidesFromServer(
        pickup: String? = null,
        destination: String? = null,
        date: String? = null,
        search: String? = null
    ): Result<List<Ride>> {
        return try {
            val response = apiService.getRides(pickup, destination, date, search)
            if (response.isSuccessful && response.body()?.success == true) {
                val rides = response.body()?.rides?.map { it.toRide() } ?: emptyList()
                val syncedRides = rides.map { it.copy(lastSyncedAt = System.currentTimeMillis()) }
                rideDao.insertRides(syncedRides)
                Result.success(syncedRides)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch rides"))
            }
        } catch (e: Exception) {
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
        return Ride(
            id = this.id,
            driverId = this.driverId,
            driverName = this.driverName,
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
            preferences = this.preferences,
            createdAt = this.createdAt
        )
    }
}

