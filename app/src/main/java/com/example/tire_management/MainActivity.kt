package com.example.tire_management

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvDashboardMobile: TextView
    private lateinit var tvDashboardName: TextView
    private lateinit var tvDashboardLicensePlate: TextView
    private lateinit var btnTires: Button
    private lateinit var btnTireRings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDashboardMobile = findViewById(R.id.tv_dashboard_mobile)
        tvDashboardName = findViewById(R.id.tv_dashboard_name)
        tvDashboardLicensePlate = findViewById(R.id.tv_dashboard_license_plate)
        btnTires = findViewById(R.id.btn_tires)
        btnTireRings = findViewById(R.id.btn_tire_rings)

        // Retrieve user data passed from LoginRegisterActivity
        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("user_data", User::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("user_data") as? User
        }

        user?.let {
            tvDashboardMobile.text = getString(R.string.dashboard_mobile, it.mobileNumber)
            tvDashboardName.text = getString(R.string.dashboard_name, it.firstName, it.lastName)
            tvDashboardLicensePlate.text = getString(R.string.dashboard_license_plate, it.licensePlate)
        }

        // Set click listeners for product selection buttons
        btnTires.setOnClickListener {
            val intent = Intent(this, TireSelectionActivity::class.java)
            startActivity(intent)
        }

        btnTireRings.setOnClickListener {
            val intent = Intent(this, TireRingSelectionActivity::class.java)
            startActivity(intent)
        }
    }
}
