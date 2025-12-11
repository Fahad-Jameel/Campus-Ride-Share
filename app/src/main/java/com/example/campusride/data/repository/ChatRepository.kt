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
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success == true) {
                    val chats = body.chats?.map { it.toChat() } ?: emptyList()
                    val syncedChats = chats.map { it.copy(lastSyncedAt = System.currentTimeMillis()) }
                    
                    // Insert chats into local database
                    syncedChats.forEach { chatDao.insertChat(it) }
                    
                    Result.success(syncedChats)
                } else {
                    val errorMsg = body?.error ?: "Response body is null or success is false"
                    android.util.Log.e("ChatRepository", "Sync failed: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = try {
                    response.errorBody()?.string() ?: response.message() ?: "Unknown error"
                } catch (e: Exception) {
                    response.message() ?: "HTTP ${response.code()}"
                }
                android.util.Log.e("ChatRepository", "HTTP error: ${response.code()} - $errorMsg")
                Result.failure(Exception("HTTP ${response.code()}: $errorMsg"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Exception in syncChatsFromServer: ${e.message}", e)
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
     * Delete a chat
     */
    suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            val data = mapOf("chat_id" to chatId)
            val response = apiService.deleteChat(data)
            if (response.isSuccessful && response.body()?.success == true) {
                chatDao.deleteChat(chatId)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to delete chat"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
