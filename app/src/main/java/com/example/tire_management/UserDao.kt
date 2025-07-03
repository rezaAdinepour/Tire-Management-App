package com.example.tire_management

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) for the User entity.
 * Provides methods to interact with the 'users' table.
 */
@Dao
interface UserDao {
    /**
     * Inserts a new user into the database.
     * If a user with the same mobileNumber already exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    /**
     * Retrieves a user by their mobile number.
     * @param mobileNumber The mobile number of the user to retrieve.
     * @return The User object if found, otherwise null.
     */
    @Query("SELECT * FROM users WHERE mobileNumber = :mobileNumber LIMIT 1")
    suspend fun getUserByMobileNumber(mobileNumber: String): User?
}
