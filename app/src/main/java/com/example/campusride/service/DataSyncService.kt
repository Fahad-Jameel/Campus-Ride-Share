package com.example.campusride.service

import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.example.campusride.data.repository.*
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataSyncService(private val context: Context) {
    private val userRepository = UserRepository(context)
    private val rideRepository = RideRepository(context)
    private val vehicleRepository = VehicleRepository(context)
    private val prefsHelper = SharedPreferencesHelper(context)
    
    fun syncAllData(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Sync user data
                userRepository.syncUserFromServer(userId)
                
                // Sync rides
                rideRepository.syncRidesFromServer()
                
                // Sync vehicles
                vehicleRepository.syncVehiclesFromServer(userId)
                
                prefsHelper.saveLastSyncTime(System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun syncRides(pickup: String? = null, destination: String? = null, date: String? = null, search: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                rideRepository.syncRidesFromServer(pickup, destination, date, search)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}


