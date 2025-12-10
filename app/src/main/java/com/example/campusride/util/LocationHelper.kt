package com.example.campusride.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        
        fun hasLocationPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
        
        fun requestLocationPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun getCurrentLocation(onSuccess: (Location) -> Unit, onFailure: () -> Unit) {
        if (!hasLocationPermission(context)) {
            onFailure()
            return
        }

        try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onSuccess(location)
                } else {
                    onFailure()
                }
            }.addOnFailureListener {
                onFailure()
            }
        } catch (e: SecurityException) {
            onFailure()
        }
    }

    fun getLastKnownLocation(onSuccess: (Location) -> Unit, onFailure: () -> Unit) {
        if (!hasLocationPermission(context)) {
            onFailure()
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        onSuccess(location)
                    } else {
                        // If no last location, try to get current location
                        getCurrentLocation(onSuccess, onFailure)
                    }
                }
                .addOnFailureListener {
                    onFailure()
                }
        } catch (e: SecurityException) {
            onFailure()
        }
    }
}
