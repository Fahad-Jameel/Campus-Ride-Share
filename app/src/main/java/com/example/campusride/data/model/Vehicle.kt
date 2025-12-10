package com.example.campusride.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey
    val id: String,
    val userId: String,
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val licensePlate: String,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)


