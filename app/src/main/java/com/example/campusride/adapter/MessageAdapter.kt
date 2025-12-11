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
import com.example.campusride.data.model.Message
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for displaying messages in a chat
 */
class MessageAdapter(
    private val currentUserId: String,
    private val onMessageAction: (Message, String) -> Unit = { _, _ -> }
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {
    
    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }
    
    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view, onMessageAction)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }
    
    class SentMessageViewHolder(
        itemView: View,
        private val onAction: (Message, String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        private val messageImage: ImageView? = itemView.findViewById(R.id.messageImage)
        
        fun bind(message: Message) {
            var displayText = message.text
            if (message.isEdited) {
                displayText += " (edited)"
            }
            if (message.isDeleted) {
                displayText = "This message was deleted"
                messageText.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
            } else {
                messageText.setTextColor(itemView.context.getColor(android.R.color.white))
            }
            
            messageText.text = displayText
            messageTime.text = formatTime(message.timestamp)
            
            // Handle image messages
            if (!message.imageUrl.isNullOrEmpty() && messageImage != null) {
                messageImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.imageUrl)
                    .into(messageImage)
            } else {
                messageImage?.visibility = View.GONE
            }
            
            // Long press to show edit/delete options
            itemView.setOnLongClickListener {
                if (!message.isDeleted) {
                    showMessageOptions(message)
                }
                true
            }
        }
        
        private fun showMessageOptions(message: Message) {
            android.app.AlertDialog.Builder(itemView.context)
                .setItems(arrayOf("Edit", "Delete")) { _, which ->
                    when (which) {
                        0 -> onAction(message, "edit")
                        1 -> onAction(message, "delete")
                    }
                }
                .show()
        }
    }
    
    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        private val messageImage: ImageView? = itemView.findViewById(R.id.messageImage)
        
        fun bind(message: Message) {
            var displayText = message.text
            if (message.isEdited) {
                displayText += " (edited)"
            }
            if (message.isDeleted) {
                displayText = "This message was deleted"
                messageText.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
            } else {
                messageText.setTextColor(itemView.context.getColor(android.R.color.white))
            }
            
            messageText.text = displayText
            messageTime.text = formatTime(message.timestamp)
            
            // Handle image messages
            if (!message.imageUrl.isNullOrEmpty() && messageImage != null) {
                messageImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.imageUrl)
                    .into(messageImage)
            } else {
                messageImage?.visibility = View.GONE
            }
        }
    }
    
    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
