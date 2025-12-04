package com.example.campusride.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Helper class for uploading and downloading images to/from Firebase Realtime Database
 * Images are converted to base64 strings for storage
 */
class FirebaseImageHelper(private val context: Context) {
    
    private val database: FirebaseDatabase = Firebase.database
    private val imagesRef = database.getReference("images")
    
    /**
     * Upload an image to Firebase Realtime Database
     * @param imageUri URI of the image to upload
     * @param imageType Type of image (profile, vehicle, ride, general)
     * @return Firebase reference path that can be stored in MySQL
     */
    suspend fun uploadImage(imageUri: Uri, imageType: String): Result<String> {
        return try {
            // Read and compress image
            val bitmap = getBitmapFromUri(imageUri)
            val base64String = bitmapToBase64(bitmap)
            
            // Generate unique ID
            val imageId = UUID.randomUUID().toString()
            val imagePath = "$imageType/$imageId"
            
            // Create image data object
            val imageData = mapOf(
                "data" to base64String,
                "type" to imageType,
                "timestamp" to System.currentTimeMillis()
            )
            
            // Upload to Firebase
            imagesRef.child(imagePath).setValue(imageData).await()
            
            // Return the Firebase path
            Result.success(imagePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download an image from Firebase Realtime Database
     * @param imagePath Firebase reference path
     * @return Bitmap of the downloaded image
     */
    suspend fun downloadImage(imagePath: String): Result<Bitmap> {
        return try {
            val snapshot = imagesRef.child(imagePath).get().await()
            val base64String = snapshot.child("data").getValue(String::class.java)
                ?: throw Exception("Image data not found")
            
            val bitmap = base64ToBitmap(base64String)
            Result.success(bitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get Firebase URL for an image path (for storing in MySQL)
     * This returns a reference path that can be used to retrieve the image
     */
    fun getImageUrl(imagePath: String): String {
        return "firebase://$imagePath"
    }
    
    /**
     * Extract Firebase path from a Firebase URL
     */
    fun getPathFromUrl(url: String): String? {
        return if (url.startsWith("firebase://")) {
            url.removePrefix("firebase://")
        } else null
    }
    
    /**
     * Delete an image from Firebase
     */
    suspend fun deleteImage(imagePath: String): Result<Unit> {
        return try {
            imagesRef.child(imagePath).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private helper methods
    
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Could not open image")
        return BitmapFactory.decodeStream(inputStream)
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        // Compress bitmap to reduce size
        val maxSize = 800
        val scale = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height, 1f)
        
        val scaledBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }
        
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    
    private fun base64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
