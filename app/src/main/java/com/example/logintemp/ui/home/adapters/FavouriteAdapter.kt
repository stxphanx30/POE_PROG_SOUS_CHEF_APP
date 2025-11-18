package com.example.logintemp.ui.home.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.logintemp.R
import com.example.logintemp.data.recipe.RecipeEntity

/**
 * Adapter simple pour afficher les cartes favorites.
 *
 * onFavClick => (recipe) -> caller decides toggle/persist
 * onImageClick => (recipe) -> caller decides navigation (Favorites list or RecipeDetail)
 */
class FavouriteAdapter(
    private val onFavClick: (recipe: RecipeEntity) -> Unit,
    private val onImageClick: (recipe: RecipeEntity) -> Unit
) : ListAdapter<RecipeEntity, FavouriteAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RecipeEntity>() {
            override fun areItemsTheSame(oldItem: RecipeEntity, newItem: RecipeEntity): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: RecipeEntity, newItem: RecipeEntity): Boolean =
                oldItem == newItem
        }
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val img = view.findViewById<ImageView>(R.id.favMealImage)
        private val heart = view.findViewById<ImageView>(R.id.favHeartIcon)
        private val title = view.findViewById<TextView>(R.id.favMealName)

        fun bind(item: RecipeEntity) {
            title.text = item.name ?: ""

            // load image: prefer URI (content uri saved locally) else fallback to drawable/logo
            val src = item.imageUri // change field if different
            if (!src.isNullOrEmpty()) {
                try {
                    img.setImageURI(Uri.parse(src))
                } catch (e: Exception) {
                    img.setImageResource(R.drawable.logo)
                }
            } else {
                img.setImageResource(R.drawable.logo)
            }

            // heart icon state - adapt resource names if needed
            heart.setImageResource(if (item.isFavorite) R.drawable.ic_favourite_filled else R.drawable.heart_filled)

            // heart click -> toggle
            heart.setOnClickListener {
                onFavClick(item)
            }

            // image click -> callback (navigate)
            img.setOnClickListener {
                onImageClick(item)
            }

            // optional: make title clickable too
            title.setOnClickListener {
                onImageClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_favourite, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}