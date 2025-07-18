package com.example.tire_management

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable // Import Serializable

/**
 * Represents a User entity in the database.
 * The mobileNumber is used as the primary key.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val mobileNumber: String, // User's mobile number, acting as the unique username
    val passwordHash: String, // Storing password as plain text for now, but should be hashed in production
    val firstName: String,
    val lastName: String,
    val licensePlate: String
) : Serializable // Implement Serializable
