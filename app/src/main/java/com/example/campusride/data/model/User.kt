package com.example.campusride.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val affiliation: String? = null,
    val verified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)


