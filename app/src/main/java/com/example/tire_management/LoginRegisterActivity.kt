package com.example.tire_management

import android.content.Intent
import android.os.Bundle
import android.util.Log // Import for logging
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var etMobileNumber: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvMessage: TextView

    // Database instance
    private lateinit var userDao: UserDao

    // Tag for logging
    private val TAG = "LoginRegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        // Initialize UI elements
        etMobileNumber = findViewById(R.id.et_mobile_number)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnRegister = findViewById(R.id.btn_register)
        tvMessage = findViewById(R.id.tv_message)

        // Initialize database DAO
        userDao = AppDatabase.getDatabase(applicationContext).userDao()
        Log.d(TAG, "Database DAO initialized.")

        // Set click listeners for buttons
        btnLogin.setOnClickListener {
            Log.d(TAG, "Login button clicked.")
            handleLogin()
        }

        btnRegister.setOnClickListener {
            Log.d(TAG, "Register button clicked.")
            handleRegister()
        }
    }

    /**
     * Handles the login logic when the login button is clicked.
     */
    private fun handleLogin() {
        val mobileNumber = etMobileNumber.text.toString().trim()
        val password = etPassword.text.toString().trim()

        Log.d(TAG, "Attempting login for mobile: $mobileNumber")

        // Validate mobile number format
        if (!isValidIranianMobileNumber(mobileNumber)) {
            tvMessage.text = "Invalid Iranian mobile number format."
            Log.w(TAG, "Login failed: Invalid mobile number format for $mobileNumber")
            return
        }

        // Perform database operation on a background thread using coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = userDao.getUserByMobileNumber(mobileNumber)
                Log.d(TAG, "User lookup result for $mobileNumber: ${user?.mobileNumber ?: "null"}")

                withContext(Dispatchers.Main) {
                    if (user != null && user.passwordHash == password) {
                        tvMessage.text = "Login successful!"
                        Log.i(TAG, "Login successful for $mobileNumber")
                        // Navigate to MainActivity upon successful login
                        val intent = Intent(this@LoginRegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Finish LoginRegisterActivity so user can't go back to it with back button
                    } else {
                        tvMessage.text = "Invalid mobile number or password."
                        Log.w(TAG, "Login failed: Invalid credentials for $mobileNumber")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during login database operation: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    tvMessage.text = "An error occurred during login. Please try again."
                }
            }
        }
    }

    /**
     * Handles the registration logic when the register button is clicked.
     */
    private fun handleRegister() {
        val mobileNumber = etMobileNumber.text.toString().trim()
        val password = etPassword.text.toString().trim()

        Log.d(TAG, "Attempting registration for mobile: $mobileNumber")

        // Validate mobile number format
        if (!isValidIranianMobileNumber(mobileNumber)) {
            tvMessage.text = "Invalid Iranian mobile number format."
            Log.w(TAG, "Registration failed: Invalid mobile number format for $mobileNumber")
            return
        }

        // Check if password is empty
        if (password.isEmpty()) {
            tvMessage.text = "Password cannot be empty."
            Log.w(TAG, "Registration failed: Password empty for $mobileNumber")
            return
        }

        // Perform database operation on a background thread using coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingUser = userDao.getUserByMobileNumber(mobileNumber)
                Log.d(TAG, "Existing user check for $mobileNumber: ${existingUser?.mobileNumber ?: "null"}")

                withContext(Dispatchers.Main) {
                    if (existingUser != null) {
                        tvMessage.text = "This mobile number is already registered. Please login."
                        Log.w(TAG, "Registration failed: Mobile number $mobileNumber already exists.")
                    } else {
                        // Register the new user
                        val newUser = User(mobileNumber, password)
                        userDao.insertUser(newUser)
                        tvMessage.text = "Registration successful! You can now login."
                        etPassword.text.clear() // Clear password after successful registration
                        Log.i(TAG, "Registration successful for $mobileNumber")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during registration database operation: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    tvMessage.text = "An error occurred during registration. Please try again."
                }
            }
        }
    }

    /**
     * Validates if the given string is a valid Iranian mobile number.
     * Starts with '09' and is followed by 9 digits (total 11 digits).
     */
    private fun isValidIranianMobileNumber(number: String): Boolean {
        return number.matches(Regex("^09\\d{9}$"))
    }
}
