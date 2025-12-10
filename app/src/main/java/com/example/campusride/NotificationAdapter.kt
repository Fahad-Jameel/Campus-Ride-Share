package com.example.campusride

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusride.data.api.NotificationResponse
import com.example.campusride.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val onItemClick: (NotificationResponse) -> Unit
) : ListAdapter<NotificationResponse, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(
        private val binding: ItemNotificationBinding,
        private val onItemClick: (NotificationResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationResponse) {
            binding.notificationTitle.text = notification.title
            binding.notificationMessage.text = notification.message
            
            // Format timestamp
            val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
            val date = Date(notification.createdAt)
            binding.notificationTime.text = dateFormat.format(date)
            
            // Show unread indicator
            if (notification.isRead) {
                binding.unreadIndicator.visibility = View.GONE
            } else {
                binding.unreadIndicator.visibility = View.VISIBLE
            }
            
            // Set click listener
            binding.root.setOnClickListener {
                onItemClick(notification)
            }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationResponse>() {
        override fun areItemsTheSame(oldItem: NotificationResponse, newItem: NotificationResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationResponse, newItem: NotificationResponse): Boolean {
            return oldItem == newItem
        }
    }
}

