package com.example.tire_management

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Using a Handler to delay the transition to the next activity
        Handler(Looper.getMainLooper()).postDelayed({
            // Start the LoginRegisterActivity
            val intent = Intent(this, LoginRegisterActivity::class.java)
            startActivity(intent)
            // Close this activity so the user can't go back to it
            finish()
        }, SPLASH_TIME_OUT)
    }
}
