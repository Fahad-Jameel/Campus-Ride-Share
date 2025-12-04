package com.example.campusride.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey
    val id: String,
    val userId: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserImageUrl: String? = null,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

