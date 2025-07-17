package com.example.tire_management

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView // Import PhotoView

class ImageDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        val photoView: PhotoView = findViewById(R.id.photo_view)

        // Get the image URL from the Intent
        val imageUrl = intent.getStringExtra("image_url")

        if (!imageUrl.isNullOrEmpty()) {
            // Load the image into PhotoView using Glide
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.rounded_edittext) // Placeholder while loading
                .error(R.drawable.rounded_edittext) // Error image if loading fails
                .into(photoView)
        } else {
            // Handle case where no image URL is provided (e.g., show a default image or message)
            photoView.setImageResource(R.drawable.rounded_edittext) // Show a generic placeholder
        }
    }
}
