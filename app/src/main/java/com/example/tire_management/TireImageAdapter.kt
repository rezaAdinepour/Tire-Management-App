package com.example.tire_management

import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import android.util.Log

/**
 * RecyclerView Adapter for displaying a list of tire images.
 */
class TireImageAdapter(private var imageList: List<String>) :
    RecyclerView.Adapter<TireImageAdapter.ImageViewHolder>() {

    private companion object {
        private const val TAG = "TireImageAdapter"
    }

    /**
     * ViewHolder for each image item in the RecyclerView.
     */
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_tire_single_image)
    }

    /**
     * Creates new ViewHolders for RecyclerView items.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item_layout, parent, false)
        return ImageViewHolder(view)
    }

    /**
     * Binds data to an existing ViewHolder.
     */
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageList[position]
        Log.d(TAG, "Attempting to load image: $imageUrl")

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.rounded_edittext)
            .error(R.drawable.rounded_edittext)
            .into(holder.imageView)

        // Set OnClickListener to open ImageDetailActivity
        holder.imageView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ImageDetailActivity::class.java).apply {
                putExtra("image_url", imageUrl) // Pass the image URL
            }
            context.startActivity(intent)
        }
    }

    /**
     * Returns the total number of items in the data set.
     */
    override fun getItemCount(): Int {
        return imageList.size
    }

    /**
     * Updates the data set of the adapter and notifies the RecyclerView to refresh.
     */
    fun updateImages(newImageList: List<String>) {
        imageList = newImageList
        notifyDataSetChanged()
        Log.d(TAG, "Image list updated. New size: ${imageList.size}")
    }
}
