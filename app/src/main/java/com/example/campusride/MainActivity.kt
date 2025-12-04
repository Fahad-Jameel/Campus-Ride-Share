package com.example.campusride

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campusride.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val autoNavigateHandler = Handler(Looper.getMainLooper())
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    companion object {
        private const val AUTO_NAVIGATE_DELAY_MS = 3500L
    }
}