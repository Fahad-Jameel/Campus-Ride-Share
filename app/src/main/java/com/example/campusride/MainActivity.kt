package com.example.campusride

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.campusride.data.api.RetrofitClient
import com.example.campusride.databinding.ActivityMainBinding
import com.example.campusride.util.SharedPreferencesHelper
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsHelper: SharedPreferencesHelper
    private val autoNavigateHandler = Handler(Looper.getMainLooper())
    private var hasNavigated = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            registerFCMToken()
        } else {
            Toast.makeText(this, "Notification permission denied. You might miss important updates.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsHelper = SharedPreferencesHelper(this)

        // Check if user is logged in
        checkLoginStatus()
        
        // Request notification permission (Android 13+)
        requestNotificationPermission()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.continueButton.setOnClickListener {
            navigateToLogin()
        }

        autoNavigateHandler.postDelayed({
            navigateToLogin()
        }, AUTO_NAVIGATE_DELAY_MS)
    }

    override fun onDestroy() {
        autoNavigateHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun navigateToLogin() {
        if (hasNavigated) return
        hasNavigated = true
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    
    private fun checkLoginStatus() {
        // Navigate directly if already logged in
        if (prefsHelper.isLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    registerFCMToken()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show explanation and request permission
                    Toast.makeText(
                        this, 
                        "Enable notifications to receive ride booking updates",
                        Toast.LENGTH_LONG
                    ).show()
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request permission directly
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android 12 and below, permission is granted automatically
            registerFCMToken()
        }
    }
    
    private fun registerFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            
            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, "FCM Token: $token")
            
            // Save token locally
            prefsHelper.saveFCMToken(token)
            
            // Send token to server if user is logged in
            val userId = prefsHelper.getUserId()
            if (!userId.isNullOrEmpty()) {
                sendTokenToServer(userId, token)
            }
        }
    }
    
    private fun sendTokenToServer(userId: String, token: String) {
        lifecycleScope.launch {
            try {
                val data = mapOf(
                    "user_id" to userId,
                    "fcm_token" to token
                )
                val response = RetrofitClient.apiService.saveFCMToken(data)
                if (response.isSuccessful) {
                    Log.d(TAG, "FCM token saved to server")
                } else {
                    Log.e(TAG, "Failed to save FCM token: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving FCM token: ${e.message}")
            }
        }
    }

    companion object {
        private const val AUTO_NAVIGATE_DELAY_MS = 3500L
        private const val TAG = "MainActivity"
    }
}