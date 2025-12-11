package com.example.campusride

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.campusride.data.model.Vehicle
import com.example.campusride.data.repository.ImageRepository
import com.example.campusride.data.repository.VehicleRepository
import com.example.campusride.databinding.ActivityAddVehicleBinding
import com.example.campusride.util.ImagePickerHelper
import com.example.campusride.util.SharedPreferencesHelper
import com.example.campusride.util.ValidationUtils
import kotlinx.coroutines.launch
import java.util.UUID

class AddVehicleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddVehicleBinding
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var imageRepository: ImageRepository
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var imagePicker: ImagePickerHelper
    
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vehicleRepository = VehicleRepository(this)
        imageRepository = ImageRepository(this)
        prefsHelper = SharedPreferencesHelper(this)
        
        imagePicker = ImagePickerHelper.create(this) { uri ->
            selectedImageUri = uri
            // Display selected image (you'll need an ImageView in your layout)
            // Glide.with(this).load(uri).into(binding.vehicleImage)
        }

        binding.backButton.setOnClickListener { finish() }
        binding.cancelButton.setOnClickListener { finish() }
        binding.saveVehicleButton.setOnClickListener {
            saveVehicle()
        }
        
        // Wire up photo upload button
        binding.addVehiclePhotoButton.setOnClickListener {
            imagePicker.pickImage()
        }

        // Update preview text as user types
        binding.makeInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePreview()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.modelInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePreview()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        setupBottomNav()
    }

    private fun updatePreview() {
        val make = binding.makeInput.text.toString().trim()
        val model = binding.modelInput.text.toString().trim()
        val preview = if (make.isNotEmpty() || model.isNotEmpty()) {
            "$make $model".trim()
        } else {
            "-"
        }
        binding.vehiclePreviewText.text = preview
    }

    private fun saveVehicle() {
        val userId = prefsHelper.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Get form values from UI input fields
        val make = binding.makeInput.text.toString().trim()
        val model = binding.modelInput.text.toString().trim()
        val yearStr = binding.yearInput.text.toString().trim()
        val color = binding.colorInput.text.toString().trim()
        val licensePlate = binding.licensePlateInput.text.toString().trim()

        // Validate inputs
        val makeError = ValidationUtils.getRequiredFieldError(make, "Make")
        if (makeError != null) {
            Toast.makeText(this, makeError, Toast.LENGTH_SHORT).show()
            return
        }

        val modelError = ValidationUtils.getRequiredFieldError(model, "Model")
        if (modelError != null) {
            Toast.makeText(this, modelError, Toast.LENGTH_SHORT).show()
            return
        }

        val year = yearStr.toIntOrNull() ?: 0
        if (!ValidationUtils.isValidYear(year)) {
            Toast.makeText(this, "Please enter a valid year (1900-${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) + 1})", Toast.LENGTH_SHORT).show()
            return
        }

        val colorError = ValidationUtils.getRequiredFieldError(color, "Color")
        if (colorError != null) {
            Toast.makeText(this, colorError, Toast.LENGTH_SHORT).show()
            return
        }

        if (!ValidationUtils.isValidLicensePlate(licensePlate)) {
            Toast.makeText(this, "Please enter a valid license plate", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        binding.saveVehicleButton.isEnabled = false
        binding.saveVehicleButton.text = "Saving..."

        lifecycleScope.launch {
            try {
                var vehicleImageUrl: String? = null

                // Upload image if selected
                if (selectedImageUri != null) {
                    val uploadResult = imageRepository.uploadImage(selectedImageUri!!, "vehicle")
                    uploadResult.onSuccess { firebaseUrl ->
                        vehicleImageUrl = firebaseUrl
                    }.onFailure { error ->
                        Toast.makeText(
                            this@AddVehicleActivity,
                            "Failed to upload image: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Create vehicle object
                val vehicle = Vehicle(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    make = make,
                    model = model,
                    year = year,
                    color = color,
                    licensePlate = licensePlate,
                    imageUrl = vehicleImageUrl,
                    createdAt = System.currentTimeMillis()
                )

                // Save vehicle
                val result = vehicleRepository.createVehicle(vehicle)
                result.onSuccess {
                    Toast.makeText(this@AddVehicleActivity, "Vehicle added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }.onFailure { error ->
                    Toast.makeText(
                        this@AddVehicleActivity,
                        "Failed to add vehicle: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddVehicleActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.saveVehicleButton.isEnabled = true
                binding.saveVehicleButton.text = "Save Vehicle"
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


