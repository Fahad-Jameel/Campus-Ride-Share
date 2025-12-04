package com.example.campusride.util

import android.util.Patterns

/**
 * Utility object for input validation
 */
object ValidationUtils {
    
    /**
     * Validate email address
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validate phone number
     * Accepts formats like: +92xxxxxxxxxx, 03xxxxxxxxx, etc.
     */
    fun isValidPhone(phone: String): Boolean {
        if (phone.isEmpty()) return false
        
        // Remove spaces and dashes
        val cleanPhone = phone.replace(" ", "").replace("-", "")
        
        // Check if it matches common patterns
        return cleanPhone.matches(Regex("^(\\+92|0)?3\\d{9}$"))
    }
    
    /**
     * Check if a string is not empty
     */
    fun isNotEmpty(value: String): Boolean {
        return value.trim().isNotEmpty()
    }
    
    /**
     * Validate password strength
     * At least 6 characters
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
    
    /**
     * Validate license plate
     * Simple check for non-empty and reasonable length
     */
    fun isValidLicensePlate(plate: String): Boolean {
        return plate.trim().length in 3..15
    }
    
    /**
     * Validate year (for vehicle)
     * Between 1900 and current year + 1
     */
    fun isValidYear(year: Int): Boolean {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return year in 1900..(currentYear + 1)
    }
    
    /**
     * Validate number of seats
     */
    fun isValidSeats(seats: Int): Boolean {
        return seats in 1..8
    }
    
    /**
     * Validate cost/price
     */
    fun isValidCost(cost: String): Boolean {
        return try {
            val value = cost.toDoubleOrNull()
            value != null && value > 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get error message for email validation
     */
    fun getEmailError(email: String): String? {
        return when {
            email.isEmpty() -> "Email is required"
            !isValidEmail(email) -> "Invalid email address"
            else -> null
        }
    }
    
    /**
     * Get error message for phone validation
     */
    fun getPhoneError(phone: String): String? {
        return when {
            phone.isEmpty() -> null // Phone is optional
            !isValidPhone(phone) -> "Invalid phone number"
            else -> null
        }
    }
    
    /**
     * Get error message for password validation
     */
    fun getPasswordError(password: String): String? {
        return when {
            password.isEmpty() -> "Password is required"
            !isValidPassword(password) -> "Password must be at least 6 characters"
            else -> null
        }
    }
    
    /**
     * Get error message for required field
     */
    fun getRequiredFieldError(value: String, fieldName: String): String? {
        return if (value.trim().isEmpty()) {
            "$fieldName is required"
        } else {
            null
        }
    }
}
