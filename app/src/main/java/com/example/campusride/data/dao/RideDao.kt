package com.example.campusride.data.dao

import androidx.room.*
import com.example.campusride.data.model.Ride
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {
    @Query("SELECT * FROM rides ORDER BY createdAt DESC")
    fun getAllRides(): Flow<List<Ride>>
    
    @Query("SELECT * FROM rides WHERE id = :rideId")
    fun getRideById(rideId: String): Flow<Ride?>
    
    @Query("SELECT * FROM rides WHERE driverId = :driverId ORDER BY createdAt DESC")
    fun getRidesByDriver(driverId: String): Flow<List<Ride>>
    
    @Query("SELECT * FROM rides WHERE pickupLocation LIKE '%' || :query || '%' OR destination LIKE '%' || :query || '%'")
    fun searchRides(query: String): Flow<List<Ride>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: Ride)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRides(rides: List<Ride>)
    
    @Update
    suspend fun updateRide(ride: Ride)
    
    @Delete
    suspend fun deleteRide(ride: Ride)
    
    @Query("DELETE FROM rides")
    suspend fun deleteAllRides()
}


