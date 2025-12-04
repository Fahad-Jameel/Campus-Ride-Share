package com.example.campusride

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.campusride.data.model.User
import com.example.campusride.data.repository.ImageRepository
import com.example.campusride.data.repository.UserRepository
import com.example.campusride.databinding.ActivityEditProfileBinding
import com.example.campusride.util.ImagePickerHelper
import com.example.campusride.util.SharedPreferencesHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var userRepository: UserRepository
    private lateinit var imageRepository: ImageRepository
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var imagePicker: ImagePickerHelper
    
    private var selectedImageUri: Uri? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(this)
        imageRepository = ImageRepository(this)
        prefsHelper = SharedPreferencesHelper(this)
        
        imagePicker = ImagePickerHelper.create(this) { uri ->
            selectedImageUri = uri
            // Display selected image
            Glide.with(this)
                .load(uri)
                .circleCrop()
                    // TODO: Uncomment when profileEditAvatar is added to layout
                    // .into(binding.profileEditAvatar)
        }

        binding.backButton.setOnClickListener { finish() }
        
        binding.saveButton.setOnClickListener {
            saveProfile()
        }
        
        binding.saveChangesButton.setOnClickListener {
            saveProfile()
        }
        
        binding.cancelButton.setOnClickListener { finish() }
        
        binding.changePhotoButton.setOnClickListener {
            imagePicker.pickImage()
        }
        
        binding.removePhotoButton.setOnClickListener {
            selectedImageUri = null
            // TODO: Uncomment when profileEditAvatar is added to layout
            // binding.profileEditAvatar.setImageResource(R.drawable.ic_profile_circle_placeholder)
        }
        
        binding.addVehicleButton.setOnClickListener {
            startActivity(Intent(this, AddVehicleActivity::class.java))
        }
        
        binding.editVehicleButton.setOnClickListener {
            startActivity(Intent(this, EditVehicleActivity::class.java))
        }

        setupBottomNav()
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        val userId = prefsHelper.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            // Get user data
            val userFlow = userRepository.getUserById(userId)
            val user = userFlow.first()
            
            if (user != null) {
                currentUser = user
                
                // Populate form with current data
                binding.nameInput.setText(user.name)
                binding.phoneInput.setText(user.phone ?: "")
                // binding.affiliationInput.setText(user.affiliation ?: "")
                
                // Load current profile image
                if (!user.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this@EditProfileActivity)
                        .load(user.profileImageUrl)
                        .placeholder(R.drawable.ic_profile_circle_placeholder)
                        .circleCrop()
                        // TODO: Uncomment when profileEditAvatar is added to layout
                        // .into(binding.profileEditAvatar)
                }
            }
        }
    }

    private fun saveProfile() {
        val user = currentUser
        if (user == null) {
            Toast.makeText(this, "Error: User data not loaded", Toast.LENGTH_SHORT).show()
            return
        }

        // Get updated values from form
        val name = binding.nameInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()
        val affiliation = user.affiliation // Using existing value for now

        // Show loading
        binding.saveButton.isEnabled = false
        binding.saveChangesButton.isEnabled = false
        binding.saveButton.text = "Saving..."

        lifecycleScope.launch {
            try {
                var profileImageUrl = user.profileImageUrl

                // Upload new image if selected
                if (selectedImageUri != null) {
                    val uploadResult = imageRepository.uploadImage(selectedImageUri!!, "profile")
                    uploadResult.onSuccess { firebaseUrl ->
                        profileImageUrl = firebaseUrl
                    }.onFailure { error ->
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Failed to upload image: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Update user profile
                val updatedUser = user.copy(
                    name = name,
                    phone = phone,
                    affiliation = affiliation,
                    profileImageUrl = profileImageUrl
                )

                val result = userRepository.updateUser(updatedUser)
                result.onSuccess {
                    Toast.makeText(this@EditProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }.onFailure { error ->
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Failed to update profile: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.saveButton.isEnabled = true
                binding.saveChangesButton.isEnabled = true
                binding.saveButton.text = "Save"
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_profile
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_offer -> {
                    startActivity(Intent(this, OfferRideActivity::class.java))
                    true
                }
                R.id.nav_find -> {
                    startActivity(Intent(this, FindRideActivity::class.java))
                    true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatsActivity::class.java))
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}


