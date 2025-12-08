package com.example.campusride.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusride.databinding.ItemBookingRequestBinding

class BookingRequestAdapter(
    private val onAccept: (Map<String, Any>) -> Unit,
    private val onReject: (Map<String, Any>) -> Unit
) : ListAdapter<Map<String, Any>, BookingRequestAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding, onAccept, onReject)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookingViewHolder(
        private val binding: ItemBookingRequestBinding,
        private val onAccept: (Map<String, Any>) -> Unit,
        private val onReject: (Map<String, Any>) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Map<String, Any>) {
            // Extract booking details
            val passengerName = booking["passenger_name"] as? String ?: "Unknown"
            val passengerEmail = booking["passenger_email"] as? String ?: ""
            val seatsRequested = booking["seats_requested"]?.toString() ?: "1"
            val stopLocation = booking["stop_location"] as? String ?: ""
            val pickupLocation = booking["pickup_location"] as? String ?: ""
            val destination = booking["destination"] as? String ?: ""
            
            // Set data
            binding.passengerName.text = passengerName
            binding.passengerEmail.text = passengerEmail
            binding.seatsRequested.text = "$seatsRequested seat(s)"
            binding.rideRoute.text = "$pickupLocation → $destination"
            
            if (stopLocation.isNotEmpty()) {
                binding.stopLocation.text = "Stop: $stopLocation"
            } else {
                binding.stopLocation.text = "No stop requested"
            }
            
            // Set click listeners
            binding.acceptButton.setOnClickListener {
                onAccept(booking)
            }
            
            binding.rejectButton.setOnClickListener {
                onReject(booking)
            }
        }
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<Map<String, Any>>() {
        override fun areItemsTheSame(
            oldItem: Map<String, Any>,
            newItem: Map<String, Any>
        ): Boolean {
            return oldItem["id"] == newItem["id"]
        }

        override fun areContentsTheSame(
            oldItem: Map<String, Any>,
            newItem: Map<String, Any>
        ): Boolean {
            return oldItem == newItem
        }
    }
}
