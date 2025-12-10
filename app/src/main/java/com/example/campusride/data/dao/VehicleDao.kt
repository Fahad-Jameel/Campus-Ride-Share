package com.example.campusride.data.dao

import androidx.room.*
import com.example.campusride.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE userId = :userId ORDER BY createdAt DESC")
    fun getVehiclesByUser(userId: String): Flow<List<Vehicle>>
    
    @Query("SELECT * FROM vehicles WHERE id = :vehicleId")
    fun getVehicleById(vehicleId: String): Flow<Vehicle?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)
    
    @Update
    suspend fun updateVehicle(vehicle: Vehicle)
    
    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)
}


