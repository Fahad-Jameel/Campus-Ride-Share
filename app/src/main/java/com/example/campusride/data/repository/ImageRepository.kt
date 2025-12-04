package com.example.campusride.data.repository

import android.content.Context
import android.net.Uri
import com.example.campusride.util.FirebaseImageHelper

/**
 * Repository for managing image uploads and downloads
 * Uses Firebase Realtime Database for base64 storage
 */
class ImageRepository(context: Context) {
    private val firebaseHelper = FirebaseImageHelper(context)
    
    /**
     * Upload an image to Firebase Realtime Database
     * @param imageUri URI of the image to upload
     * @param type Type of image (profile, vehicle, ride, general)
     * @return Firebase URL string to be stored in MySQL database
     */
    suspend fun uploadImage(imageUri: Uri, type: String): Result<String> {
        return try {
            val result = firebaseHelper.uploadImage(imageUri, type)
            result.onSuccess { imagePath ->
                // Return Firebase URL to store in MySQL
                val firebaseUrl = firebaseHelper.getImageUrl(imagePath)
                return Result.success(firebaseUrl)
            }
            result.onFailure { error ->
                return Result.failure(error)
            }
            Result.failure(Exception("Upload failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get the Firebase helper for direct access if needed
     */
    fun getFirebaseHelper(): FirebaseImageHelper = firebaseHelper
}

