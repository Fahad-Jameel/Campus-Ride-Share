package com.example.campusride.data.dao

import androidx.room.*
import com.example.campusride.data.model.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats WHERE userId = :userId OR otherUserId = :userId ORDER BY lastMessageTime DESC")
    fun getChatsForUser(userId: String): Flow<List<Chat>>
    
    // Alias for repository compatibility
    @Query("SELECT * FROM chats WHERE userId = :userId OR otherUserId = :userId ORDER BY lastMessageTime DESC")
    fun getChatsByUser(userId: String): Flow<List<Chat>>
    
    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun getChatById(chatId: String): Flow<Chat?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<Chat>)
    
    @Update
    suspend fun updateChat(chat: Chat)
    
    @Delete
    suspend fun deleteChat(chat: Chat)
}

