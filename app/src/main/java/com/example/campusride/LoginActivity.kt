package com.example.campusride

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.campusride.data.repository.UserRepository
import com.example.campusride.databinding.ActivityLoginBinding
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userRepository: UserRepository
    private lateinit var prefsHelper: SharedPreferencesHelper
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(this)
        prefsHelper = SharedPreferencesHelper(this)

        // Check if already logged in
        if (prefsHelper.isLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.createAccount.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        binding.signInButton.setOnClickListener {
            performLogin()
        }

        binding.passwordToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.forgotPassword.setOnClickListener {
            // TODO: open forgot password flow
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun performLogin() {
        val email = binding.emailInput.text?.toString()?.trim() ?: ""
        val password = binding.passwordInput.text?.toString() ?: ""
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.signInButton.isEnabled = false
        binding.signInButton.text = "Signing in..."
        
        lifecycleScope.launch {
            val result = userRepository.login(email, password)
            result.onSuccess { user ->
                prefsHelper.saveUserId(user.id)
                prefsHelper.saveUserEmail(user.email)
                prefsHelper.setLoggedIn(true)
                
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()
            }.onFailure { error ->
                binding.signInButton.isEnabled = true
                binding.signInButton.text = getString(R.string.login_sign_in)
                Toast.makeText(this@LoginActivity, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun togglePasswordVisibility() {
        val input = binding.passwordInput
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            input.transformationMethod = null
            binding.passwordToggle.text = getString(R.string.login_toggle_hide)
        } else {
            input.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.passwordToggle.text = getString(R.string.login_toggle_show)
        }
        input.setSelection(input.text?.length ?: 0)
    }
}

