package com.example.campusride.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for showing date and time pickers
 */
object DateTimePickerHelper {
    
    /**
     * Show a date picker dialog
     * @param context Context
     * @param onDateSelected Callback with selected date in "yyyy-MM-dd" format
     */
    fun showDatePicker(
        context: Context,
        initialDate: Calendar = Calendar.getInstance(),
        onDateSelected: (String, Calendar) -> Unit
    ) {
        val year = initialDate.get(Calendar.YEAR)
        val month = initialDate.get(Calendar.MONTH)
        val day = initialDate.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val calendar = Calendar.getInstance()
                calendar.set(selectedYear, selectedMonth, selectedDay)
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(calendar.time)
                
                onDateSelected(formattedDate, calendar)
            },
            year,
            month,
            day
        )
        
        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    /**
     * Show a time picker dialog
     * @param context Context
     * @param onTimeSelected Callback with selected time in "HH:mm" format
     */
    fun showTimePicker(
        context: Context,
        initialTime: Calendar = Calendar.getInstance(),
        onTimeSelected: (String, Calendar) -> Unit
    ) {
        val hour = initialTime.get(Calendar.HOUR_OF_DAY)
        val minute = initialTime.get(Calendar.MINUTE)
        
        val timePickerDialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormat.format(calendar.time)
                
                onTimeSelected(formattedTime, calendar)
            },
            hour,
            minute,
            true // 24-hour format
        )
        
        timePickerDialog.show()
    }
    
    /**
     * Format a date string for display
     * @param dateString Date in "yyyy-MM-dd" format
     * @return Formatted date like "Dec 04, 2025"
     */
    fun formatDateForDisplay(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            if (date != null) {
                outputFormat.format(date)
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }
    
    /**
     * Format a time string for display
     * @param timeString Time in "HH:mm" format
     * @return Formatted time like "02:30 PM"
     */
    fun formatTimeForDisplay(timeString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val time = inputFormat.parse(timeString)
            if (time != null) {
                outputFormat.format(time)
            } else {
                timeString
            }
        } catch (e: Exception) {
            timeString
        }
    }
    
    /**
     * Get current date in "yyyy-MM-dd" format
     */
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    /**
     * Get current time in "HH:mm" format
     */
    fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(Date())
    }
}
