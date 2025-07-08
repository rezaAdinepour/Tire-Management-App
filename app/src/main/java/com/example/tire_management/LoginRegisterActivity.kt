package com.example.tire_management

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
    private lateinit var spinnerPlatePart4: Spinner
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
        val ivCompanyLogo: ImageView = findViewById(R.id.iv_company_logo)
        etMobileNumber = findViewById(R.id.et_mobile_number)
        etFirstName = findViewById(R.id.et_first_name)
        etLastName = findViewById(R.id.et_last_name)
        etPlatePart1 = findViewById(R.id.et_plate_part1)
        spinnerPlateLetter = findViewById(R.id.spinner_plate_letter)
        etPlatePart3 = findViewById(R.id.et_plate_part3)
        spinnerPlatePart4 = findViewById(R.id.spinner_plate_part4)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnRegister = findViewById(R.id.btn_register)
        tvMessage = findViewById(R.id.tv_message)

        // Populate the Spinner with Iranian license plate letters
        val letters = resources.getStringArray(R.array.iranian_license_plate_letters)
        val letterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, letters)
        letterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPlateLetter.adapter = letterAdapter

        // Populate the Spinner with Iranian Province Codes
        val provinceCodes = resources.getStringArray(R.array.iranian_province_codes)
        val provinceCodeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provinceCodes)
        provinceCodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPlatePart4.adapter = provinceCodeAdapter

        // Optional: Set a listener for spinner item selection if needed for other logic
        spinnerPlateLetter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                Log.d(TAG, "Selected letter: ${parent.getItemAtPosition(position)}")
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerPlatePart4.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                Log.d(TAG, "Selected province code: ${parent.getItemAtPosition(position)}")
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
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
            tvMessage.setTextColor(resources.getColor(R.color.md_theme_error, theme)) // Set text color to red
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
                        tvMessage.setTextColor(resources.getColor(R.color.md_theme_success, theme)) // Set text color to green
                        Log.i(TAG, "Login successful for ${user.mobileNumber}")
                        // Navigate to MainActivity upon successful login, passing user data
                        val intent = Intent(this@LoginRegisterActivity, MainActivity::class.java)
                        intent.putExtra("user_data", user)
                        startActivity(intent)
                        finish() // Finish LoginRegisterActivity so user can't go back to it with back button
                    } else {
                        tvMessage.text = getString(R.string.invalid_credentials)
                        tvMessage.setTextColor(resources.getColor(R.color.md_theme_error, theme)) // Set text color to red
                        Log.w(TAG, "Login failed: Invalid credentials for $mobileNumber")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during login database operation: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    tvMessage.text = getString(R.string.error_occurred)
                    tvMessage.setTextColor(resources.getColor(R.color.md_theme_error, theme)) // Set text color to red
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

        // Get license plate parts from EditTexts and Spinners
        val platePart1 = etPlatePart1.text.toString().trim()
        val plateLetter = spinnerPlateLetter.selectedItem?.toString()?.trim() ?: ""
        val platePart3 = etPlatePart3.text.toString().trim()
        val platePart4 = spinnerPlatePart4.selectedItem?.toString()?.trim() ?: ""
        val licensePlate = "$platePart1 $plateLetter $platePart3 - $platePart4"

        val password = etPassword.text.toString().trim()

        Log.d(TAG, "Attempting registration for mobile: $mobileNumber")

        // Validate inputs
        if (!isValidIranianMobileNumber(mobileNumber)) {
            tvMessage.text = getString(R.string.invalid_mobile_format)
            tvMessage.setTextColor(resources.getColor(R.color.md_theme_error, theme)) // Set text color to red
            Log.w(TAG, "Registration failed: Invalid mobile number format for $mobileNumber")
            return
        }
        if (firstName.isEmpty()) {
            tvMessage.text = getString(R.string.first_name_empty)
            tvMessage.setTextColor(resources.getColor(R.color.md_theme_error, theme)) // Set text color to red
            return
        }
        if (lastName.isEmpty()) {
            tvMessage.text = getString(R.string.last_name_empty)
            tvMessage.setTextColor(resources.getColor(R.color.md_theme_error, theme)) // Set text color to red
            return
        }
        // Check if any part of the license plate is empty (spinner will always have a selection, so no need for plateLetter.isEmpty() or platePart4.isEmpty())
        if (platePart1.isEmpty() || platePart3.isEmpty()) {
            tvMessage.text = getString(R.string.license_plate_empty)
            tvMessage.setTextColor(resources.getColor(R.color.md_theme_error, theme)) // Set text color to red
            return
        }
        if (!isValidIranianLicensePlate(licensePlate)) {
            tvMessage.text = getString(R.string.invalid_license_plate_format)
            tvMessage.setTextColor(resources.getColor(R.color.md_theme_error, theme)) // Set text color to red
            return
        }
        if (password.isEmpty()) {
            tvMessage.text = getString(R.string.password_empty)
            tvMessage.setTextColor(resources.getColor(R.color.md_theme_error, theme)) // Set text color to red
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
                        tvMessage.setTextColor(resources.getColor(R.color.md_theme_error, theme)) // Set text color to red
                        Log.w(TAG, "Registration failed: Mobile number $mobileNumber already exists.")
                    } else {
                        // Register the new user, passing all required parameters
                        val newUser = User(mobileNumber, password, firstName, lastName, licensePlate)
                        userDao.insertUser(newUser)
                        tvMessage.text = getString(R.string.registration_successful)
                        tvMessage.setTextColor(resources.getColor(R.color.md_theme_success, theme)) // Set text color to green
                        etPassword.text.clear() // Clear password after successful registration
                        Log.i(TAG, "Registration successful for ${newUser.mobileNumber}. Plate: ${newUser.licensePlate}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during registration database operation: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    tvMessage.text = getString(R.string.error_occurred)
                    tvMessage.setTextColor(resources.getColor(R.color.md_theme_error, theme)) // Set text color to red
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
        val persianLettersRegex = "[\\u0621-\\u0628\\u062A-\\u062B\\u062D-\\u063A\\u0641-\\u0642\\u0644-\\u0646\\u0640-\\u064A\\u0698\\u0686\\u06AF\\u067E]"
        val regex = Regex("^\\d{2} $persianLettersRegex \\d{3} - \\d{2}$")
        return plate.matches(regex)
    }
}
