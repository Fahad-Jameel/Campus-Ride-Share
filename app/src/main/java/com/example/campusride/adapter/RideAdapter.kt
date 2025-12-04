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
import com.example.campusride.data.model.Ride

/**
 * RecyclerView adapter for displaying rides
 */
class RideAdapter(
    private val onRideClick: (Ride) -> Unit
) : ListAdapter<Ride, RideAdapter.RideViewHolder>(RideDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ride_card, parent, false)
        return RideViewHolder(view, onRideClick)
    }
    
    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class RideViewHolder(
        itemView: View,
        private val onRideClick: (Ride) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val rideRoute: TextView = itemView.findViewById(R.id.rideRoute)
        private val rideDetails: TextView = itemView.findViewById(R.id.rideDetails)
        private val ridePrice: TextView = itemView.findViewById(R.id.ridePrice)
        private val rideAvatar: ImageView = itemView.findViewById(R.id.rideAvatar)
        
        fun bind(ride: Ride) {
            rideRoute.text = "${ride.pickupLocation} → ${ride.destination}"
            rideDetails.text = "${ride.driverName} • ${ride.date} at ${ride.time} • ${ride.availableSeats} seats"
            ridePrice.text = "Rs. ${ride.cost}"
            
            // Load driver image
            if (!ride.driverImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(ride.driverImageUrl)
                    .placeholder(R.drawable.ic_profile_circle_placeholder)
                    .circleCrop()
                    .into(rideAvatar)
            } else {
                rideAvatar.setImageResource(R.drawable.ic_profile_circle_placeholder)
            }
            
            itemView.setOnClickListener {
                onRideClick(ride)
            }
        }
    }
    
    class RideDiffCallback : DiffUtil.ItemCallback<Ride>() {
        override fun areItemsTheSame(oldItem: Ride, newItem: Ride): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Ride, newItem: Ride): Boolean {
            return oldItem == newItem
        }
    }
}
