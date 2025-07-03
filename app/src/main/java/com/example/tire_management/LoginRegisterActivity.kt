package com.example.tire_management

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginRegisterActivity : AppCompatActivity() {

    // A simple in-memory map to store registered users (mobile number -> password)
    // In a real application, this would be replaced by a database (e.g., Firebase, SQLite)
    private val registeredUsers = mutableMapOf<String, String>()

    private lateinit var etMobileNumber: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        // Initialize UI elements
        etMobileNumber = findViewById(R.id.et_mobile_number)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnRegister = findViewById(R.id.btn_register)
        tvMessage = findViewById(R.id.tv_message)

        // Set click listeners for buttons
        btnLogin.setOnClickListener {
            handleLogin()
        }

        btnRegister.setOnClickListener {
            handleRegister()
        }
    }

    /**
     * Handles the login logic when the login button is clicked.
     */
    private fun handleLogin() {
        val mobileNumber = etMobileNumber.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validate mobile number format
        if (!isValidIranianMobileNumber(mobileNumber)) {
            tvMessage.text = "Invalid Iranian mobile number format."
            return
        }

        // Check if the user exists and the password matches
        if (registeredUsers.containsKey(mobileNumber) && registeredUsers[mobileNumber] == password) {
            tvMessage.text = "Login successful!"
            // Navigate to MainActivity upon successful login
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finish LoginRegisterActivity so user can't go back to it with back button
        } else {
            tvMessage.text = "Invalid mobile number or password."
        }
    }

    /**
     * Handles the registration logic when the register button is clicked.
     */
    private fun handleRegister() {
        val mobileNumber = etMobileNumber.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validate mobile number format
        if (!isValidIranianMobileNumber(mobileNumber)) {
            tvMessage.text = "Invalid Iranian mobile number format."
            return
        }

        // Check if password is empty
        if (password.isEmpty()) {
            tvMessage.text = "Password cannot be empty."
            return
        }

        // Check if the mobile number is already registered
        if (registeredUsers.containsKey(mobileNumber)) {
            tvMessage.text = "This mobile number is already registered. Please login."
        } else {
            // Register the new user
            registeredUsers[mobileNumber] = password
            tvMessage.text = "Registration successful! You can now login."
            // Optionally clear fields or navigate to login
            etPassword.text.clear() // Clear password after successful registration
        }
    }

    /**
     * Validates if the given string is a valid Iranian mobile number.
     * Starts with '09' and is followed by 9 digits (total 11 digits).
     */
    private fun isValidIranianMobileNumber(number: String): Boolean {
        // Regex for Iranian mobile numbers: starts with 09, followed by 9 digits
        // Example: 09123456789
        return number.matches(Regex("^09\\d{9}$"))
    }
}
