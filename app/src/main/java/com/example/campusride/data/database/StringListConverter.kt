package com.example.campusride.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringListConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value == null || value.isEmpty()) {
            return emptyList()
        }
        return try {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun toString(value: List<String>?): String {
        if (value == null || value.isEmpty()) {
            return "[]"
        }
        return try {
            gson.toJson(value)
        } catch (e: Exception) {
            "[]"
        }
    }
}


