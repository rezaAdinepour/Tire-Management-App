package com.example.tire_management

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The Room database for the application.
 * Defines the database configuration and provides access to DAOs.
 */
@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the AppDatabase.
         * If the instance does not exist, it creates one.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tire_management_database" // Name of your database file
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
