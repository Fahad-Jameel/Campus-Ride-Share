package com.example.campusride

import android.content.Intent
import android.view.View
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusride.adapter.ChatAdapter
import com.example.campusride.data.repository.ChatRepository
import com.example.campusride.databinding.ActivityChatsBinding
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.launch

class ChatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatsBinding
    private lateinit var chatRepository: ChatRepository
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatRepository = ChatRepository(this)
        prefsHelper = SharedPreferencesHelper(this)

        binding.backButton.setOnClickListener { finish() }

        // Hide all placeholder cards initially
        hideAllChatCards()
        
        setupRecyclerView()
        setupBottomNav()
        loadChats()

        binding.newChatButton.setOnClickListener {
            // TODO: Navigate to new chat screen or user selection
            Toast.makeText(this, "New chat feature coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.chatSosButton.setOnClickListener {
            // TODO: SOS action
            Toast.makeText(this, "SOS feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter { chat ->
            openChatDetail(chat)
        }
        
        // Note: RecyclerView is not in layout, using card views instead
        // Adapter is kept for future use if RecyclerView is added
    }

    private fun loadChats() {
        val userId = prefsHelper.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                // Sync chats from server first
                val syncResult = chatRepository.syncChatsFromServer(userId)
                syncResult.onFailure { error ->
                    // Log error but continue to load from local database
                    android.util.Log.e("ChatsActivity", "Sync failed: ${error.message}")
                }

                // Observe chats from local database
                chatRepository.getChatsByUser(userId).collect { chats ->
                    try {
                        // Update adapter if it exists (for future RecyclerView use)
                        if (::chatAdapter.isInitialized) {
                            chatAdapter.submitList(chats)
                        }
                        
                        // Display chats in cards (only real data, no placeholders)
                        if (chats.isNotEmpty()) {
                            displayChatsInCards(chats.take(4))
                        } else {
                            // Hide all cards if no chats
                            hideAllChatCards()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatsActivity", "Error displaying chats: ${e.message}", e)
                        // Hide all cards on error
                        hideAllChatCards()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatsActivity", "Error loading chats: ${e.message}", e)
                // Don't show toast for empty chats, only for actual errors
                if (e.message?.contains("Database") == true || e.message?.contains("connection") == true) {
                    Toast.makeText(this@ChatsActivity, "Connection error. Please check your internet.", Toast.LENGTH_SHORT).show()
                }
                hideAllChatCards()
            }
        }
    }

    private fun hideAllChatCards() {
        try {
            binding.chatItem1.root.visibility = View.GONE
            binding.chatItem2.root.visibility = View.GONE
            binding.chatItem3.root.visibility = View.GONE
            binding.chatItem4.root.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun displayChatsInCards(chats: List<com.example.campusride.data.model.Chat>) {
        // Hide all cards first
        hideAllChatCards()
        
        // Show only real chats
        chats.forEachIndexed { index, chat ->
            try {
                when (index) {
                    0 -> {
                        binding.chatItem1.root.visibility = View.VISIBLE
                        binding.chatItem1.chatName.text = chat.otherUserName
                        binding.chatItem1.chatMessage.text = chat.lastMessage ?: "No messages yet"
                        binding.chatItem1.chatTime.text = formatTime(chat.lastMessageTime ?: 0L)
                        if (chat.unreadCount > 0) {
                            binding.chatItem1.chatBadge.visibility = View.VISIBLE
                            binding.chatItem1.chatBadge.text = chat.unreadCount.toString()
                        } else {
                            binding.chatItem1.chatBadge.visibility = View.GONE
                        }
                        binding.chatItem1.root.setOnClickListener {
                            openChatDetail(chat)
                        }
                    }
                    1 -> {
                        binding.chatItem2.root.visibility = View.VISIBLE
                        binding.chatItem2.chatName.text = chat.otherUserName
                        binding.chatItem2.chatMessage.text = chat.lastMessage ?: "No messages yet"
                        binding.chatItem2.chatTime.text = formatTime(chat.lastMessageTime ?: 0L)
                        if (chat.unreadCount > 0) {
                            binding.chatItem2.chatBadge.visibility = View.VISIBLE
                            binding.chatItem2.chatBadge.text = chat.unreadCount.toString()
                        } else {
                            binding.chatItem2.chatBadge.visibility = View.GONE
                        }
                        binding.chatItem2.root.setOnClickListener {
                            openChatDetail(chat)
                        }
                    }
                    2 -> {
                        binding.chatItem3.root.visibility = View.VISIBLE
                        binding.chatItem3.chatName.text = chat.otherUserName
                        binding.chatItem3.chatMessage.text = chat.lastMessage ?: "No messages yet"
                        binding.chatItem3.chatTime.text = formatTime(chat.lastMessageTime ?: 0L)
                        if (chat.unreadCount > 0) {
                            binding.chatItem3.chatBadge.visibility = View.VISIBLE
                            binding.chatItem3.chatBadge.text = chat.unreadCount.toString()
                        } else {
                            binding.chatItem3.chatBadge.visibility = View.GONE
                        }
                        binding.chatItem3.root.setOnClickListener {
                            openChatDetail(chat)
                        }
                    }
                    3 -> {
                        binding.chatItem4.root.visibility = View.VISIBLE
                        binding.chatItem4.chatName.text = chat.otherUserName
                        binding.chatItem4.chatMessage.text = chat.lastMessage ?: "No messages yet"
                        binding.chatItem4.chatTime.text = formatTime(chat.lastMessageTime ?: 0L)
                        if (chat.unreadCount > 0) {
                            binding.chatItem4.chatBadge.visibility = View.VISIBLE
                            binding.chatItem4.chatBadge.text = chat.unreadCount.toString()
                        } else {
                            binding.chatItem4.chatBadge.visibility = View.GONE
                        }
                        binding.chatItem4.root.setOnClickListener {
                            openChatDetail(chat)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun openChatDetail(chat: com.example.campusride.data.model.Chat) {
        val intent = Intent(this, ChatDetailActivity::class.java)
        intent.putExtra("CHAT_ID", chat.id)
        intent.putExtra("OTHER_USER_ID", chat.otherUserId)
        intent.putExtra("OTHER_USER_NAME", chat.otherUserName)
        startActivity(intent)
    }

    private fun formatTime(timestamp: Long): String {
        if (timestamp == 0L) return ""
        
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> {
                val dateFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                dateFormat.format(java.util.Date(timestamp))
            }
        }
    }


    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_chat
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_offer -> {
                    startActivity(Intent(this, OfferRideActivity::class.java))
                    true
                }
                R.id.nav_find -> {
                    startActivity(Intent(this, FindRideActivity::class.java))
                    true
                }
                R.id.nav_chat -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}

