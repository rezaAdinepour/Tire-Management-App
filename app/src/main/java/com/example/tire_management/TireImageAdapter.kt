package com.example.tire_management

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * RecyclerView Adapter for displaying a list of tire images.
 */
class TireImageAdapter(private var imageList: List<String>) :
    RecyclerView.Adapter<TireImageAdapter.ImageViewHolder>() {

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
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.rounded_edittext) // Placeholder while loading
            .error(R.drawable.rounded_edittext) // Error image if loading fails
            .into(holder.imageView)
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
        notifyDataSetChanged() // Notifies the adapter that the data has changed
    }
}
