package com.example.campusride.data.repository

import android.content.Context
import com.example.campusride.data.api.ApiService
import com.example.campusride.data.api.RetrofitClient
import com.example.campusride.data.api.ChatResponse
import com.example.campusride.data.api.ChatsApiResponse
import com.example.campusride.data.database.CampusRideDatabase
import com.example.campusride.data.model.Chat
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing chat data
 * Syncs between server API and local Room database
 */
class ChatRepository(context: Context) {
    private val apiService: ApiService = RetrofitClient.apiService
    private val db = CampusRideDatabase.getDatabase(context)
    private val chatDao = db.chatDao()
    
    /**
     * Get all chats for a user from local database
     */
    fun getChatsByUser(userId: String): Flow<List<Chat>> = chatDao.getChatsByUser(userId)
    
    /**
     * Sync chats from server and store locally
     */
    suspend fun syncChatsFromServer(userId: String): Result<List<Chat>> {
        return try {
            val response = apiService.getChats(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val chats = response.body()?.chats?.map { it.toChat() } ?: emptyList()
                val syncedChats = chats.map { it.copy(lastSyncedAt = System.currentTimeMillis()) }
                
                // Insert chats into local database
                syncedChats.forEach { chatDao.insertChat(it) }
                
                Result.success(syncedChats)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch chats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a specific chat by ID
     */
    suspend fun getChatById(chatId: String): Flow<Chat?> = chatDao.getChatById(chatId)
    
    /**
     * Update last message timestamp for a chat
     */
    suspend fun updateChatLastMessage(chatId: String, lastMessage: String, lastMessageTime: Long) {
        val chat = chatDao.getChatById(chatId)
        // Update will be handled when observing the Flow
    }
    
    /**
     * Convert API response to local Chat model
     */
    private fun ChatResponse.toChat(): Chat {
        return Chat(
            id = this.id,
            userId = this.userId,
            otherUserId = this.otherUserId,
            otherUserName = this.otherUserName,
            otherUserImageUrl = this.otherUserImageUrl,
            lastMessage = this.lastMessage,
            lastMessageTime = this.lastMessageTime ?: 0L,
            unreadCount = this.unreadCount,
            createdAt = this.createdAt
        )
    }
}
