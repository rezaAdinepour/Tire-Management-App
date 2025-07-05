package com.example.tire_management

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide // Import Glide

class TireSelectionActivity : AppCompatActivity() {

    private lateinit var spinnerTireSize: Spinner
    private lateinit var spinnerTireBrand: Spinner
    private lateinit var ivTireImage1: ImageView
    private lateinit var ivTireImage2: ImageView
    private lateinit var ivTireImage3: ImageView
    private lateinit var btnBuyTire: Button
    private lateinit var tvPurchaseNotification: TextView

    private var selectedTireSize: String = ""
    private var selectedTireBrand: String = ""

    // Tag for logging
    private companion object {
        private const val TAG = "TireSelectionActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tire_selection)

        // Initialize UI elements
        spinnerTireSize = findViewById(R.id.spinner_tire_size)
        spinnerTireBrand = findViewById(R.id.spinner_tire_brand)
        ivTireImage1 = findViewById(R.id.iv_tire_image1)
        ivTireImage2 = findViewById(R.id.iv_tire_image2)
        ivTireImage3 = findViewById(R.id.iv_tire_image3)
        btnBuyTire = findViewById(R.id.btn_buy_tire)
        tvPurchaseNotification = findViewById(R.id.tv_purchase_notification)

        // Populate Tire Size Spinner
        val tireSizes = resources.getStringArray(R.array.tire_sizes)
        val sizeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tireSizes)
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTireSize.adapter = sizeAdapter

        // Populate Tire Brand Spinner
        val tireBrands = resources.getStringArray(R.array.tire_brands)
        val brandAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tireBrands)
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTireBrand.adapter = brandAdapter

        // Set initial selections and update images
        // Ensure there's a default selection if the arrays are not empty
        selectedTireSize = tireSizes.firstOrNull() ?: ""
        selectedTireBrand = tireBrands.firstOrNull() ?: ""
        updateTireImages()

        // Set listeners for spinners
        spinnerTireSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTireSize = parent.getItemAtPosition(position).toString()
                updateTireImages()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case where nothing is selected (e.g., set a default or show a message)
                selectedTireSize = ""
                updateTireImages()
            }
        }

        spinnerTireBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTireBrand = parent.getItemAtPosition(position).toString()
                updateTireImages()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case where nothing is selected
                selectedTireBrand = ""
                updateTireImages()
            }
        }

        // Set click listener for Buy button
        btnBuyTire.setOnClickListener {
            showPurchaseNotification()
        }
    }

    /**
     * Updates the displayed tire images based on selected size and brand using Glide.
     * Uses placeholder images for demonstration.
     */
    private fun updateTireImages() {
        // Hide previous notification when selections change
        tvPurchaseNotification.visibility = View.GONE

        // Generate dynamic placeholder URLs based on selection
        // In a real app, these would be actual image URLs from your server or local resources
        val imageUrl1 = "https://placehold.co/300x200/4CAF50/FFFFFF?text=${selectedTireBrand}+${selectedTireSize.replace("/", "-")}-1"
        val imageUrl2 = "https://placehold.co/300x200/2196F3/FFFFFF?text=${selectedTireBrand}+${selectedTireSize.replace("/", "-")}-2"
        val imageUrl3 = "https://placehold.co/300x200/FF9800/FFFFFF?text=${selectedTireBrand}+${selectedTireSize.replace("/", "-")}-3"

        // Load images using Glide
        Glide.with(this)
            .load(imageUrl1)
            .placeholder(R.drawable.rounded_edittext) // A simple placeholder while loading
            .error(R.drawable.rounded_edittext) // An error image if loading fails
            .into(ivTireImage1)

        Glide.with(this)
            .load(imageUrl2)
            .placeholder(R.drawable.rounded_edittext)
            .error(R.drawable.rounded_edittext)
            .into(ivTireImage2)

        Glide.with(this)
            .load(imageUrl3)
            .placeholder(R.drawable.rounded_edittext)
            .error(R.drawable.rounded_edittext)
            .into(ivTireImage3)
    }

    /**
     * Displays a purchase notification with dynamic details.
     */
    private fun showPurchaseNotification() {
        val random = java.util.Random()
        // Generate a random Iranian mobile number (09XXXXXXXXX)
        val phoneNumber = "09" + (100000000 + random.nextInt(900000000)).toString()

        val message = getString(R.string.purchase_notification_message, selectedTireBrand, selectedTireSize, phoneNumber)
        tvPurchaseNotification.text = message
        tvPurchaseNotification.visibility = View.VISIBLE // Make notification visible
    }
}
