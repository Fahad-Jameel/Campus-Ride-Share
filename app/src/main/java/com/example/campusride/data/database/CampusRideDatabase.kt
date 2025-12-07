package com.example.campusride.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.campusride.data.dao.*
import com.example.campusride.data.model.*

@Database(
    entities = [User::class, Ride::class, Vehicle::class, Chat::class, Message::class],
    version = 2,
    exportSchema = false
)
@androidx.room.TypeConverters(StringListConverter::class)
abstract class CampusRideDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun rideDao(): RideDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    
    companion object {
        @Volatile
        private var INSTANCE: CampusRideDatabase? = null
        
        fun getDatabase(context: Context): CampusRideDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CampusRideDatabase::class.java,
                    "campus_ride_database"
                )
                .fallbackToDestructiveMigration() // Handle schema changes by recreating database
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

