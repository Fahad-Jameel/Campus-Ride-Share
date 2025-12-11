package com.example.campusride

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.campusride.data.model.Vehicle
import com.example.campusride.data.repository.ImageRepository
import com.example.campusride.data.repository.VehicleRepository
import com.example.campusride.databinding.ActivityEditVehicleBinding
import com.example.campusride.util.ImagePickerHelper
import com.example.campusride.util.SharedPreferencesHelper
import com.example.campusride.util.ValidationUtils
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class EditVehicleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditVehicleBinding
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var imageRepository: ImageRepository
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var imagePicker: ImagePickerHelper
    
    private var selectedImageUri: Uri? = null
    private var currentVehicle: Vehicle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vehicleRepository = VehicleRepository(this)
        imageRepository = ImageRepository(this)
        prefsHelper = SharedPreferencesHelper(this)
        
        imagePicker = ImagePickerHelper.create(this) { uri ->
            selectedImageUri = uri
            // Display selected image (if ImageView exists in layout)
            // if (binding.vehicleImage != null) {
            //     Glide.with(this).load(uri).into(binding.vehicleImage)
            // }
        }

        binding.backButton.setOnClickListener { finish() }
        binding.revertButton.setOnClickListener { finish() }
        binding.saveChangesButton.setOnClickListener { saveVehicle() }
        binding.deleteButton.setOnClickListener { deleteVehicle() }
        binding.updatePhotoButton.setOnClickListener { imagePicker.pickImage() }

        setupBottomNav()
        loadVehicle()
    }

    private fun loadVehicle() {
        val vehicleId = intent.getStringExtra("VEHICLE_ID")
        if (vehicleId == null) {
            Toast.makeText(this, "Vehicle ID not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userId = prefsHelper.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            // Sync vehicles first
            vehicleRepository.syncVehiclesFromServer(userId)
            
            // Get vehicle by ID
            val vehicle = vehicleRepository.getVehicleById(vehicleId).firstOrNull()
            
            if (vehicle == null) {
                Toast.makeText(this@EditVehicleActivity, "Vehicle not found", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            currentVehicle = vehicle
            
            // Populate form
            binding.makeInput.setText(vehicle.make)
            binding.modelInput.setText(vehicle.model)
            binding.colorInput.setText(vehicle.color)
            binding.yearInput.setText(if (vehicle.year > 0) vehicle.year.toString() else "")
            binding.licensePlateInput.setText(vehicle.licensePlate)
            
            // Update preview
            binding.vehicleName.text = "${vehicle.make} ${vehicle.model}".trim().ifEmpty { "-" }
            val metaParts = mutableListOf<String>()
            if (vehicle.year > 0) metaParts.add(vehicle.year.toString())
            if (!vehicle.color.isNullOrEmpty()) metaParts.add(vehicle.color)
            if (!vehicle.licensePlate.isNullOrEmpty()) metaParts.add(vehicle.licensePlate)
            binding.vehicleMeta.text = if (metaParts.isNotEmpty()) metaParts.joinToString(" • ") else "-"
            
            // Load vehicle image if available (if ImageView exists in layout)
            // if (!vehicle.imageUrl.isNullOrEmpty() && binding.vehicleImage != null) {
            //     Glide.with(this@EditVehicleActivity)
            //         .load(vehicle.imageUrl)
            //         .into(binding.vehicleImage)
            // }
        }
    }

    private fun saveVehicle() {
        val vehicle = currentVehicle ?: return
        
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

        binding.saveChangesButton.isEnabled = false
        binding.saveChangesButton.text = "Saving..."

        lifecycleScope.launch {
            try {
                var vehicleImageUrl = vehicle.imageUrl

                // Upload new image if selected
                if (selectedImageUri != null) {
                    val uploadResult = imageRepository.uploadImage(selectedImageUri!!, "vehicle")
                    uploadResult.onSuccess { firebaseUrl ->
                        vehicleImageUrl = firebaseUrl
                    }.onFailure { error ->
                        Toast.makeText(
                            this@EditVehicleActivity,
                            "Failed to upload image: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Update vehicle
                val updatedVehicle = vehicle.copy(
                    make = make,
                    model = model,
                    year = year,
                    color = color,
                    licensePlate = licensePlate,
                    imageUrl = vehicleImageUrl
                )

                val result = vehicleRepository.updateVehicle(updatedVehicle)
                result.onSuccess {
                    Toast.makeText(this@EditVehicleActivity, "Vehicle updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }.onFailure { error ->
                    Toast.makeText(
                        this@EditVehicleActivity,
                        "Failed to update vehicle: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditVehicleActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.saveChangesButton.isEnabled = true
                binding.saveChangesButton.text = getString(R.string.edit_vehicle_save)
            }
        }
    }

    private fun deleteVehicle() {
        val vehicle = currentVehicle ?: return
        
        AlertDialog.Builder(this)
            .setTitle("Delete Vehicle")
            .setMessage("Are you sure you want to delete this vehicle? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val result = vehicleRepository.deleteVehicle(vehicle)
                        result.onSuccess {
                            Toast.makeText(this@EditVehicleActivity, "Vehicle deleted successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }.onFailure { error ->
                            Toast.makeText(this@EditVehicleActivity, "Failed to delete vehicle: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@EditVehicleActivity, "Error deleting vehicle: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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


