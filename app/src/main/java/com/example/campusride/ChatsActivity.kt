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

        configureItems()
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
            // Navigate to chat detail
            val intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra("CHAT_ID", chat.id)
            intent.putExtra("OTHER_USER_ID", chat.otherUserId)
            intent.putExtra("OTHER_USER_NAME", chat.otherUserName)
            startActivity(intent)
        }
        
        // Note: You'll need to add a RecyclerView in your layout
        // For now, keeping the existing card views
    }

    private fun loadChats() {
        val userId = prefsHelper.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            // Sync chats from server
            chatRepository.syncChatsFromServer(userId)

            // Observe chats from local database
            chatRepository.getChatsByUser(userId).collect { chats ->
                chatAdapter.submitList(chats)
                
                // Display first 4 chats in existing cards for now
                if (chats.isNotEmpty()) {
                    displayChatsInCards(chats.take(4))
                }
            }
        }
    }

    private fun displayChatsInCards(chats: List<com.example.campusride.data.model.Chat>) {
        chats.forEachIndexed { index, chat ->
            when (index) {
                0 -> {
                    binding.chatItem1.chatName.text = chat.otherUserName
                    binding.chatItem1.chatMessage.text = chat.lastMessage ?: "No messages yet"
                    binding.chatItem1.chatTime.text = formatTime(chat.lastMessageTime)
                    if (chat.unreadCount > 0) {
                        binding.chatItem1.chatBadge.visibility = View.VISIBLE
                        binding.chatItem1.chatBadge.text = chat.unreadCount.toString()
                    } else {
                        binding.chatItem1.chatBadge.visibility = View.GONE
                    }
                }
                1 -> {
                    binding.chatItem2.chatName.text = chat.otherUserName
                    binding.chatItem2.chatMessage.text = chat.lastMessage ?: "No messages yet"
                    binding.chatItem2.chatTime.text = formatTime(chat.lastMessageTime)
                    binding.chatItem2.chatBadge.visibility = if (chat.unreadCount > 0) View.VISIBLE else View.GONE
                }
                2 -> {
                    binding.chatItem3.chatName.text = chat.otherUserName
                    binding.chatItem3.chatMessage.text = chat.lastMessage ?: "No messages yet"
                    binding.chatItem3.chatTime.text = formatTime(chat.lastMessageTime)
                    binding.chatItem3.chatBadge.visibility = if (chat.unreadCount > 0) View.VISIBLE else View.GONE
                }
                3 -> {
                    binding.chatItem4.chatName.text = chat.otherUserName
                    binding.chatItem4.chatMessage.text = chat.lastMessage ?: "No messages yet"
                    binding.chatItem4.chatTime.text = formatTime(chat.lastMessageTime)
                    binding.chatItem4.chatBadge.visibility = if (chat.unreadCount > 0) View.VISIBLE else View.GONE
                }
            }
        }
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

    private fun configureItems() {
        binding.chatItem1.chatName.text = getString(R.string.chats_contact_1)
        binding.chatItem1.chatTime.text = getString(R.string.chats_contact_1_time)
        binding.chatItem1.chatMessage.text = getString(R.string.chats_contact_1_message)
        binding.chatItem1.chatBadge.text = getString(R.string.chats_contact_unread)
        binding.chatItem1.root.setOnClickListener {
            startActivity(Intent(this, ChatDetailActivity::class.java))
        }

        binding.chatItem2.chatName.text = getString(R.string.chats_contact_2)
        binding.chatItem2.chatTime.text = getString(R.string.chats_contact_2_time)
        binding.chatItem2.chatMessage.text = getString(R.string.chats_contact_2_message)
        binding.chatItem2.chatBadge.visibility = View.GONE

        binding.chatItem3.chatName.text = getString(R.string.chats_contact_3)
        binding.chatItem3.chatTime.text = getString(R.string.chats_contact_3_time)
        binding.chatItem3.chatMessage.text = getString(R.string.chats_contact_3_message)
        binding.chatItem3.chatBadge.visibility = View.GONE

        binding.chatItem4.chatName.text = getString(R.string.chats_contact_4)
        binding.chatItem4.chatTime.text = getString(R.string.chats_contact_3_time)
        binding.chatItem4.chatMessage.text = getString(R.string.chats_contact_4_message)
        binding.chatItem4.chatBadge.visibility = View.GONE
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

