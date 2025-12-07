package com.example.campusride.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.campusride.data.database.StringListConverter

@Entity(tableName = "rides")
@TypeConverters(StringListConverter::class)
data class Ride(
    @PrimaryKey
    val id: String,
    val driverId: String,
    val driverName: String,
    val driverImageUrl: String? = null,
    val pickupLocation: String,
    val destination: String,
    val date: String,
    val time: String,
    val availableSeats: Int,
    val totalSeats: Int,
    val cost: String,
    val vehicleId: String? = null,
    val vehicleModel: String? = null,
    val preferences: List<String> = emptyList(), // e.g., "Music", "Food", "No Smoking"
    val expiryTime: Long? = null, // Time when ride offer expires
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

