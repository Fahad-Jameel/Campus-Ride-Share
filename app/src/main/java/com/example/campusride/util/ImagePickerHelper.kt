package com.example.campusride.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/**
 * Helper class for picking images from gallery
 * Uses AndroidX Activity Result API
 */
class ImagePickerHelper(
    private val activity: AppCompatActivity,
    private val onImagePicked: (Uri) -> Unit
) {
    
    private var imagePickerLauncher: ActivityResultLauncher<Intent>
    
    init {
        // Register image picker launcher
        imagePickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let { onImagePicked(it) }
            }
        }
    }
    
    /**
     * Open gallery to pick an image
     */
    fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }
    
    companion object {
        /**
         * Create an ImagePickerHelper for an activity
         */
        fun create(
            activity: AppCompatActivity,
            onImagePicked: (Uri) -> Unit
        ): ImagePickerHelper {
            return ImagePickerHelper(activity, onImagePicked)
        }
    }
}
