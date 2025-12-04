package com.example.campusride.data.repository

import android.content.Context
import com.example.campusride.data.api.ApiService
import com.example.campusride.data.api.RetrofitClient
import com.example.campusride.data.api.MessageResponse
import com.example.campusride.data.api.SendMessageRequest
import com.example.campusride.data.database.CampusRideDatabase
import com.example.campusride.data.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing message data
 * Syncs between server API and local Room database
 */
class MessageRepository(context: Context) {
    private val apiService: ApiService = RetrofitClient.apiService
    private val db = CampusRideDatabase.getDatabase(context)
    private val messageDao = db.messageDao()
    
    /**
     * Get all messages for a chat from local database
     */
    fun getMessagesByChat(chatId: String): Flow<List<Message>> = messageDao.getMessagesByChat(chatId)
    
    /**
     * Sync messages from server and store locally
     */
    suspend fun syncMessagesFromServer(chatId: String): Result<List<Message>> {
        return try {
            val response = apiService.getMessages(chatId)
            if (response.isSuccessful && response.body()?.success == true) {
                val messages = response.body()?.messages?.map { it.toMessage() } ?: emptyList()
                val syncedMessages = messages.map { it.copy(lastSyncedAt = System.currentTimeMillis()) }
                
                // Insert messages into local database
                messageDao.insertMessages(syncedMessages)
                
                Result.success(syncedMessages)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch messages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send a new message
     */
    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        text: String,
        imageUrl: String? = null
    ): Result<Message> {
        return try {
            val request = SendMessageRequest(
                chatId = chatId,
                senderId = senderId,
                receiverId = receiverId,
                text = text,
                imageUrl = imageUrl
            )
            
            val response = apiService.sendMessage(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val messageResponse = response.body()?.message
                if (messageResponse != null) {
                    val message = messageResponse.toMessage().copy(lastSyncedAt = System.currentTimeMillis())
                    messageDao.insertMessage(message)
                    Result.success(message)
                } else {
                    Result.failure(Exception("Failed to send message"))
                }
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to send message"))
            }
        } catch (e: Exception) {
            // Save locally even if send fails (for offline support)
            val offlineMessage = Message(
                id = "offline_${System.currentTimeMillis()}",
                chatId = chatId,
                senderId = senderId,
                receiverId = receiverId,
                text = text,
                imageUrl = imageUrl,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            messageDao.insertMessage(offlineMessage)
            Result.failure(e)
        }
    }
    
    /**
     * Mark messages as read
     */
    suspend fun markMessagesAsRead(chatId: String, userId: String) {
        // This would typically call a server endpoint to mark messages as read
        // For now, we'll update locally
        // messageDao.markAsRead(chatId, userId)
    }
    
    /**
     * Convert API response to local Message model
     */
    private fun MessageResponse.toMessage(): Message {
        return Message(
            id = this.id,
            chatId = this.chatId,
            senderId = this.senderId,
            receiverId = this.receiverId,
            text = this.text,
            imageUrl = this.imageUrl,
            isRead = this.isRead,
            timestamp = this.timestamp
        )
    }
}
