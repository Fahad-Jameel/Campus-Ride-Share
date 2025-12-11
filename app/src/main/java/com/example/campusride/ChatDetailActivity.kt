package com.example.campusride

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusride.adapter.MessageAdapter
import com.example.campusride.data.model.Message
import com.example.campusride.data.repository.ChatRepository
import com.example.campusride.data.repository.MessageRepository
import com.example.campusride.databinding.ActivityChatDetailBinding
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.launch

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatDetailBinding
    private lateinit var messageRepository: MessageRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var messageAdapter: MessageAdapter
    
    private var chatId: String? = null
    private var otherUserId: String? = null
    private var otherUserName: String? = null
    private var editingMessageId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageRepository = MessageRepository(this)
        chatRepository = ChatRepository(this)
        prefsHelper = SharedPreferencesHelper(this)

        // Get chat info from intent
        chatId = intent.getStringExtra("CHAT_ID")
        otherUserId = intent.getStringExtra("OTHER_USER_ID")
        otherUserName = intent.getStringExtra("OTHER_USER_NAME")

        // Set chat title
        binding.chatTitle.text = otherUserName ?: "Chat"

        binding.backButton.setOnClickListener { finish() }

        binding.sendButton.setOnClickListener {
            if (editingMessageId != null) {
                updateMessage()
            } else {
                sendMessage()
            }
        }

        // End Ride button
        binding.endRideButton.setOnClickListener {
            endRide()
        }

        setupRecyclerView()
        setupBottomNav()
        loadMessages()
    }

    private fun setupRecyclerView() {
        val currentUserId = prefsHelper.getUserId() ?: ""
        messageAdapter = MessageAdapter(currentUserId) { message, action ->
            when (action) {
                "edit" -> startEditingMessage(message)
                "delete" -> deleteMessage(message.id)
            }
        }
        
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatDetailActivity).apply {
                stackFromEnd = true // Start from bottom
            }
            adapter = messageAdapter
        }
    }

    private fun loadMessages() {
        val currentChatId = chatId
        if (currentChatId == null) {
            Toast.makeText(this, "Invalid chat", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            // Sync messages from server
            messageRepository.syncMessagesFromServer(currentChatId)

            // Observe messages from local database
            messageRepository.getMessagesByChat(currentChatId).collect { messages ->
                messageAdapter.submitList(messages)
                
                // Auto-scroll to bottom when new messages arrive
                if (messages.isNotEmpty()) {
                    binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }
        }
    }

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        val currentChatId = chatId
        val currentOtherUserId = otherUserId
        val currentUserId = prefsHelper.getUserId()

        if (currentChatId == null || currentOtherUserId == null || currentUserId == null) {
            Toast.makeText(this, "Unable to send message", Toast.LENGTH_SHORT).show()
            return
        }

        // Clear input immediately for better UX
        binding.messageInput.text?.clear()

        lifecycleScope.launch {
            val result = messageRepository.sendMessage(
                chatId = currentChatId,
                senderId = currentUserId,
                receiverId = currentOtherUserId,
                text = messageText
            )

            result.onFailure { error ->
                Toast.makeText(
                    this@ChatDetailActivity,
                    "Failed to send: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun startEditingMessage(message: Message) {
        editingMessageId = message.id
        binding.messageInput.setText(message.text)
        binding.sendButton.text = "Update"
    }
    
    private fun updateMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        val currentMessageId = editingMessageId
        
        if (messageText.isEmpty() || currentMessageId == null) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.messageInput.text?.clear()
        editingMessageId = null
        binding.sendButton.text = "Send"
        
        lifecycleScope.launch {
            val result = messageRepository.editMessage(currentMessageId, messageText)
            result.onSuccess {
                Toast.makeText(this@ChatDetailActivity, "Message updated", Toast.LENGTH_SHORT).show()
            }.onFailure { error ->
                Toast.makeText(
                    this@ChatDetailActivity,
                    "Failed to update: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun deleteMessage(messageId: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    val result = messageRepository.deleteMessage(messageId)
                    result.onSuccess {
                        Toast.makeText(this@ChatDetailActivity, "Message deleted", Toast.LENGTH_SHORT).show()
                    }.onFailure { error ->
                        Toast.makeText(
                            this@ChatDetailActivity,
                            "Failed to delete: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun endRide() {
        val currentChatId = chatId
        if (currentChatId == null) {
            Toast.makeText(this, "Invalid chat", Toast.LENGTH_SHORT).show()
            return
        }
        
        android.app.AlertDialog.Builder(this)
            .setTitle("End Ride")
            .setMessage("Are you sure you want to end this ride? This will delete the chat and remove it from both users' chat lists.")
            .setPositiveButton("End Ride") { _, _ ->
                lifecycleScope.launch {
                    val result = chatRepository.deleteChat(currentChatId)
                    result.onSuccess {
                        Toast.makeText(this@ChatDetailActivity, "Ride ended. Chat deleted.", Toast.LENGTH_SHORT).show()
                        finish()
                    }.onFailure { error ->
                        Toast.makeText(
                            this@ChatDetailActivity,
                            "Failed to end ride: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
