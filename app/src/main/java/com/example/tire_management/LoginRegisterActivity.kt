package com.example.tire_management

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var etMobileNumber: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPlatePart1: EditText
    private lateinit var spinnerPlateLetter: Spinner
    private lateinit var etPlatePart3: EditText
    private lateinit var etPlatePart4: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvMessage: TextView

    // Database instance
    private lateinit var userDao: UserDao

    // Tag for logging (using const val for compile-time constant)
    private companion object {
        private const val TAG = "LoginRegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        // Initialize UI elements
        etMobileNumber = findViewById(R.id.et_mobile_number)
        etFirstName = findViewById(R.id.et_first_name)
        etLastName = findViewById(R.id.et_last_name)
        etPlatePart1 = findViewById(R.id.et_plate_part1)
        spinnerPlateLetter = findViewById(R.id.spinner_plate_letter)
        etPlatePart3 = findViewById(R.id.et_plate_part3)
        etPlatePart4 = findViewById(R.id.et_plate_part4)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnRegister = findViewById(R.id.btn_register)
        tvMessage = findViewById(R.id.tv_message)

        // Populate the Spinner with Iranian license plate letters
        val letters = resources.getStringArray(R.array.iranian_license_plate_letters)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, letters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPlateLetter.adapter = adapter

        // Optional: Set a listener for spinner item selection if needed for other logic
        spinnerPlateLetter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // You can add logic here if you need to react to letter selection
                Log.d(TAG, "Selected letter: ${parent.getItemAtPosition(position)}")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

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
            tvMessage.text = getString(R.string.invalid_mobile_format)
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
                        tvMessage.text = getString(R.string.login_successful)
                        Log.i(TAG, "Login successful for $mobileNumber")
                        // Navigate to MainActivity upon successful login
                        val intent = Intent(this@LoginRegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Finish LoginRegisterActivity so user can't go back to it with back button
                    } else {
                        tvMessage.text = getString(R.string.invalid_credentials)
                        Log.w(TAG, "Login failed: Invalid credentials for $mobileNumber")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during login database operation: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    tvMessage.text = getString(R.string.error_occurred)
                }
            }
        }
    }

    /**
     * Handles the registration logic when the register button is clicked.
     */
    private fun handleRegister() {
        val mobileNumber = etMobileNumber.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()

        // Get license plate parts and combine them
        val platePart1 = etPlatePart1.text.toString().trim()
        val plateLetter = spinnerPlateLetter.selectedItem?.toString()?.trim() ?: ""
        val platePart3 = etPlatePart3.text.toString().trim()
        val platePart4 = etPlatePart4.text.toString().trim()
        val licensePlate = "$platePart1 $plateLetter $platePart3 - $platePart4" // Combine into format ## X ### - NN

        val password = etPassword.text.toString().trim()

        Log.d(TAG, "Attempting registration for mobile: $mobileNumber")

        // Validate inputs
        if (!isValidIranianMobileNumber(mobileNumber)) {
            tvMessage.text = getString(R.string.invalid_mobile_format)
            Log.w(TAG, "Registration failed: Invalid mobile number format for $mobileNumber")
            return
        }
        if (firstName.isEmpty()) {
            tvMessage.text = getString(R.string.first_name_empty)
            Log.w(TAG, "Registration failed: First name empty for $mobileNumber")
            return
        }
        if (lastName.isEmpty()) {
            tvMessage.text = getString(R.string.last_name_empty)
            Log.w(TAG, "Registration failed: Last name empty for $mobileNumber")
            return
        }
        // Check if any part of the license plate is empty
        if (platePart1.isEmpty() || plateLetter.isEmpty() || platePart3.isEmpty() || platePart4.isEmpty()) {
            tvMessage.text = getString(R.string.license_plate_empty)
            Log.w(TAG, "Registration failed: License plate parts empty for $mobileNumber")
            return
        }
        if (!isValidIranianLicensePlate(licensePlate)) {
            tvMessage.text = getString(R.string.invalid_license_plate_format)
            Log.w(TAG, "Registration failed: Invalid license plate format for $mobileNumber")
            return
        }
        if (password.isEmpty()) {
            tvMessage.text = getString(R.string.password_empty)
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
                        tvMessage.text = getString(R.string.mobile_already_registered)
                        Log.w(TAG, "Registration failed: Mobile number $mobileNumber already exists.")
                    } else {
                        // Register the new user, passing all required parameters
                        val newUser = User(mobileNumber, password, firstName, lastName, licensePlate)
                        userDao.insertUser(newUser)
                        tvMessage.text = getString(R.string.registration_successful)
                        etPassword.text.clear() // Clear password after successful registration
                        Log.i(TAG, "Registration successful for $mobileNumber. Plate: $licensePlate")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during registration database operation: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    tvMessage.text = getString(R.string.error_occurred)
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

    /**
     * Validates if the given string is a valid Iranian license plate number.
     * Format: ## X ### - NN
     * ##: Two digits (0-9)
     * X: One Persian letter (from the predefined list)
     * ###: Three digits (0-9)
     * NN: Two digits (0-9)
     */
    private fun isValidIranianLicensePlate(plate: String): Boolean {
        // The regex now expects spaces between parts and a " - " before the last part.
        // The individual parts' lengths are already constrained by EditText maxLength.
        // The letter is constrained by the Spinner's options.
        val persianLettersRegex = "[\\u0621-\\u0628\\u062A-\\u062B\\u062D-\\u063A\\u0641-\\u0642\\u0644-\\u0646\\u0640-\\u064A\\u0698\\u0686\\u06AF\\u067E]" // Added more ranges for completeness
        val regex = Regex("^\\d{2} $persianLettersRegex \\d{3} - \\d{2}$")
        return plate.matches(regex)
    }
}
