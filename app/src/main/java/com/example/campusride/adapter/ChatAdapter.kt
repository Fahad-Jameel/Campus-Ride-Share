package com.example.campusride.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusride.R
import com.example.campusride.data.model.Chat
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for displaying chats
 */
class ChatAdapter(
    private val onChatClick: (Chat) -> Unit
) : ListAdapter<Chat, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view, onChatClick)
    }
    
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ChatViewHolder(
        itemView: View,
        private val onChatClick: (Chat) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val chatName: TextView = itemView.findViewById(R.id.chatName)
        private val chatMessage: TextView = itemView.findViewById(R.id.chatMessage)
        private val chatTime: TextView = itemView.findViewById(R.id.chatTime)
        private val chatAvatar: ImageView = itemView.findViewById(R.id.chatAvatar)
        private val chatBadge: TextView = itemView.findViewById(R.id.chatBadge)
        
        fun bind(chat: Chat) {
            chatName.text = chat.otherUserName
            chatMessage.text = chat.lastMessage ?: "No messages yet"
            chatTime.text = formatTime(chat.lastMessageTime)
            
            // Show/hide unread badge
            if (chat.unreadCount > 0) {
                chatBadge.visibility = View.VISIBLE
                chatBadge.text = chat.unreadCount.toString()
            } else {
                chatBadge.visibility = View.GONE
            }
            
            // Load user avatar
            if (!chat.otherUserImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(chat.otherUserImageUrl)
                    .placeholder(R.drawable.ic_profile_circle_placeholder)
                    .circleCrop()
                    .into(chatAvatar)
            } else {
                chatAvatar.setImageResource(R.drawable.ic_profile_circle_placeholder)
            }
            
            itemView.setOnClickListener {
                onChatClick(chat)
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
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    dateFormat.format(Date(timestamp))
                }
            }
        }
    }
    
    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
}
