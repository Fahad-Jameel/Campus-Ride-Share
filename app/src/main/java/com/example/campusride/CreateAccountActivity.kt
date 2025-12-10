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
import com.example.campusride.databinding.ActivityCreateAccountBinding
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.launch

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAccountBinding
    private lateinit var userRepository: UserRepository
    private lateinit var prefsHelper: SharedPreferencesHelper
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(this)
        prefsHelper = SharedPreferencesHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.signInLink.setOnClickListener {
            finish()
        }

        binding.createAccountButton.setOnClickListener {
            performRegistration()
        }

        binding.passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordField(binding.passwordInput, binding.passwordToggle, isPasswordVisible)
        }

        binding.confirmPasswordToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordField(binding.confirmPasswordInput, binding.confirmPasswordToggle, isConfirmPasswordVisible)
        }
    }

    private fun togglePasswordField(
        input: android.widget.EditText,
        toggleView: android.widget.TextView,
        visible: Boolean
    ) {
        if (visible) {
            input.transformationMethod = null
            toggleView.text = getString(R.string.login_toggle_hide)
        } else {
            input.transformationMethod = PasswordTransformationMethod.getInstance()
            toggleView.text = getString(R.string.login_toggle_show)
        }
        input.setSelection(input.text?.length ?: 0)
    }
    
    private fun performRegistration() {
        val name = binding.fullNameInput.text?.toString()?.trim() ?: ""
        val email = binding.universityEmailInput.text?.toString()?.trim() ?: ""
        val phone = "" // Using empty for now - will add field later
        val affiliation = "" // Using empty for now - will add field later  
        val password = binding.passwordInput.text?.toString() ?: ""
        val confirmPassword = binding.confirmPasswordInput.text?.toString() ?: ""
        
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.createAccountButton.isEnabled = false
        binding.createAccountButton.text = "Creating account..."
        
        lifecycleScope.launch {
            val result = userRepository.register(
                email = email,
                password = password,
                name = name,
                phone = if (phone.isEmpty()) null else phone,
                affiliation = if (affiliation.isEmpty()) null else affiliation
            )
            result.onSuccess { user ->
                prefsHelper.saveUserId(user.id)
                prefsHelper.saveUserEmail(user.email)
                prefsHelper.setLoggedIn(true)
                
                // Send FCM token to server
                val fcmToken = prefsHelper.getFCMToken()
                if (!fcmToken.isNullOrEmpty()) {
                    try {
                        val apiService = com.example.campusride.data.api.RetrofitClient.apiService
                        val data = mapOf(
                            "userId" to user.id,
                            "fcmToken" to fcmToken
                        )
                        apiService.saveFCMToken(data)
                    } catch (e: Exception) {
                        // Silently fail - token will be sent on next app start
                    }
                }
                
                Toast.makeText(this@CreateAccountActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@CreateAccountActivity, HomeActivity::class.java))
                finish()
            }.onFailure { error ->
                binding.createAccountButton.isEnabled = true
                binding.createAccountButton.text = getString(R.string.create_account_button)
                Toast.makeText(this@CreateAccountActivity, "Registration failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

