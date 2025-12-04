package com.example.campusride.data.repository

import android.content.Context
import com.example.campusride.data.api.ApiService
import com.example.campusride.data.api.RetrofitClient
import com.example.campusride.data.api.*
import com.example.campusride.data.database.CampusRideDatabase
import com.example.campusride.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

class VehicleRepository(context: Context) {
    private val apiService: ApiService = RetrofitClient.apiService
    private val db = CampusRideDatabase.getDatabase(context)
    private val vehicleDao = db.vehicleDao()
    
    fun getVehiclesByUser(userId: String): Flow<List<Vehicle>> = vehicleDao.getVehiclesByUser(userId)
    
    suspend fun syncVehiclesFromServer(userId: String): Result<List<Vehicle>> {
        return try {
            val response = apiService.getVehicles(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val vehicles = response.body()?.vehicles?.map { it.toVehicle() } ?: emptyList()
                val syncedVehicles = vehicles.map { it.copy(lastSyncedAt = System.currentTimeMillis()) }
                syncedVehicles.forEach { vehicleDao.insertVehicle(it) }
                Result.success(syncedVehicles)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch vehicles"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createVehicle(vehicle: Vehicle): Result<Vehicle> {
        return try {
            val response = apiService.createVehicle(CreateVehicleRequest(
                userId = vehicle.userId,
                make = vehicle.make,
                model = vehicle.model,
                year = vehicle.year,
                color = vehicle.color,
                licensePlate = vehicle.licensePlate,
                imageUrl = vehicle.imageUrl
            ))
            if (response.isSuccessful && response.body()?.success == true) {
                val vehicleResponse = response.body()?.vehicle
                if (vehicleResponse != null) {
                    val createdVehicle = vehicleResponse.toVehicle().copy(lastSyncedAt = System.currentTimeMillis())
                    vehicleDao.insertVehicle(createdVehicle)
                    Result.success(createdVehicle)
                } else {
                    Result.failure(Exception("Failed to create vehicle"))
                }
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to create vehicle"))
            }
        } catch (e: Exception) {
            // Save locally even if sync fails
            vehicleDao.insertVehicle(vehicle)
            Result.failure(e)
        }
    }
    
    suspend fun updateVehicle(vehicle: Vehicle): Result<Vehicle> {
        return try {
            val response = apiService.updateVehicle(UpdateVehicleRequest(
                id = vehicle.id,
                make = vehicle.make,
                model = vehicle.model,
                year = vehicle.year,
                color = vehicle.color,
                licensePlate = vehicle.licensePlate,
                imageUrl = vehicle.imageUrl
            ))
            if (response.isSuccessful && response.body()?.success == true) {
                val vehicleResponse = response.body()?.vehicle
                if (vehicleResponse != null) {
                    val updatedVehicle = vehicleResponse.toVehicle().copy(lastSyncedAt = System.currentTimeMillis())
                    vehicleDao.insertVehicle(updatedVehicle)
                    Result.success(updatedVehicle)
                } else {
                    Result.failure(Exception("Update failed"))
                }
            } else {
                Result.failure(Exception(response.body()?.error ?: "Update failed"))
            }
        } catch (e: Exception) {
            // Save locally even if sync fails
            vehicleDao.insertVehicle(vehicle)
            Result.failure(e)
        }
    }
    
    private fun VehicleResponse.toVehicle(): Vehicle {
        return Vehicle(
            id = this.id,
            userId = this.userId,
            make = this.make,
            model = this.model,
            year = this.year,
            color = this.color,
            licensePlate = this.licensePlate,
            imageUrl = this.imageUrl,
            createdAt = this.createdAt
        )
    }
}

