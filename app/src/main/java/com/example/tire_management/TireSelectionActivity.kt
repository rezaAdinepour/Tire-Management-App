package com.example.tire_management

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager // Import for LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView // Import for RecyclerView
import com.bumptech.glide.Glide // Still needed for the adapter
import java.util.Random

class TireSelectionActivity : AppCompatActivity() {

    private lateinit var spinnerTireSize: Spinner
    private lateinit var spinnerTireBrand: Spinner
    private lateinit var recyclerViewTireImages: RecyclerView // Changed to RecyclerView
    private lateinit var btnBuyTire: Button
    private lateinit var tvPurchaseNotification: TextView

    private lateinit var tireImageAdapter: TireImageAdapter // New adapter instance

    private var selectedTireSize: String = ""
    private var selectedTireBrand: String = ""

    // Tag for logging
    private companion object {
        private const val TAG = "TireSelectionActivity"
    }

    // Map to store image URLs for each brand
    // The first URL in the list for each brand should be its logo.
    private val brandImages: Map<String, List<String>> = mapOf(
        "افی پلاس" to listOf(
            "https://drive.google.com/uc?export=download&id=1HsOPl_0ZoTSYWt-9QYuNFJ1KBEOrGCoH", // effiplus.png (Logo)
            "https://drive.google.com/uc?export=download&id=16nHLsvS6D8QmFejs_vbw1JfWOkpdRcxT", // H606.png
            "https://drive.google.com/uc?export=download&id=1-bqIEIkA1pJm7x4dMH9w7v4YfpLJiNN9", // M698.png
            "https://drive.google.com/uc?export=download&id=1U99r_whdPYdj1BrbppoN-8EhWdc2mXEV", // q689.png
            "https://drive.google.com/uc?export=download&id=1LoVctEPLKXbnxyPf_OKr9C3eebvjNigq", // R107.png
            "https://drive.google.com/uc?export=download&id=1pzHsPZ4o1sz95yZvoxN5YhuA2ePttiXH"  // R109.png
        ),
        "گرندستون" to listOf(
            "https://drive.google.com/uc?export=download&id=1GHDlaz-iA4-3BDfw7a8JfIFRT7Gd_9HK", // grandstone.png (Logo)
            "https://drive.google.com/uc?export=download&id=1xscWjpDpBmfby3KeboP6dfbf5A8c9ZXP", // GG258.png
            "https://drive.google.com/uc?export=download&id=17hRYgaeUhhpTCBrQRlDl6NFZpG1rhuwy", // GT168.png
            "https://drive.google.com/uc?export=download&id=1epX2UlHpnC029P3Zq7WlJbe5Ij7Spwnt", // GT169.png
            "https://drive.google.com/uc?export=download&id=1Cy2WRLFnaiUJFSIHCS1tuyYKILUiChGS", // GT178.png
            "https://drive.google.com/uc?export=download&id=1fMF0ld4Vb3sGnBNswY8eGWtKsdbWeUCb", // GT199.png
            "https://drive.google.com/uc?export=download&id=1touLiTcJ8YvHtH3aUNdl6fZzn3mKCWWQ", // GT266.png
            "https://drive.google.com/uc?export=download&id=1bsS4PrZNYUPhcbX7eJry-VIkt5098wEa"  // GT268.png
        )
        // Add other brands and their image URLs here as needed
    )

    // Default image for brands without specific images
    private val defaultNoImage: String = "https://drive.google.com/uc?export=download&id=1mm8op2iNmwINvU1qdXMhgcjHV4mAzBUV" // no_image.jpeg

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tire_selection)

        // Initialize UI elements
        spinnerTireSize = findViewById(R.id.spinner_tire_size)
        spinnerTireBrand = findViewById(R.id.spinner_tire_brand)
        recyclerViewTireImages = findViewById(R.id.recycler_view_tire_images) // Initialize RecyclerView
        btnBuyTire = findViewById(R.id.btn_buy_tire)
        tvPurchaseNotification = findViewById(R.id.tv_purchase_notification)

        // Setup RecyclerView
        recyclerViewTireImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        tireImageAdapter = TireImageAdapter(emptyList()) // Initialize adapter with empty list
        recyclerViewTireImages.adapter = tireImageAdapter

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
     * Uses actual image URLs from Google Drive.
     */
    private fun updateTireImages() {
        // Hide previous notification when selections change
        tvPurchaseNotification.visibility = View.GONE

        val imagesForBrand = brandImages[selectedTireBrand]
        val imagesToDisplay = mutableListOf<String>()

        if (imagesForBrand != null && imagesForBrand.isNotEmpty()) {
            imagesToDisplay.addAll(imagesForBrand)
        } else {
            // If no specific images for the brand, use the default placeholder
            imagesToDisplay.add(defaultNoImage)
        }

        // Update the RecyclerView adapter with the new list of images
        tireImageAdapter.updateImages(imagesToDisplay)
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
