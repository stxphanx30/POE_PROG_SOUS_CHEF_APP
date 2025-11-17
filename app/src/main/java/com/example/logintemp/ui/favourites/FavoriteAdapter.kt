package com.example.logintemp.ui.favorites

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

class FavoriteAdapter(
    private val onHeartClick: (RecipeEntity, Int) -> Unit
) : ListAdapter<RecipeEntity, FavoriteAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<RecipeEntity>() {
            override fun areItemsTheSame(old: RecipeEntity, new: RecipeEntity) = old.id == new.id
            override fun areContentsTheSame(old: RecipeEntity, new: RecipeEntity) = old == new
        }
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.favMealImage)
        val heart: ImageView = view.findViewById(R.id.favHeartIcon)
        val title: TextView = view.findViewById(R.id.favMealName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_favourite, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val recipe = getItem(position)

        // image
        if (!recipe.imageUri.isNullOrEmpty()) {
            try { holder.img.setImageURI(Uri.parse(recipe.imageUri)) }
            catch (_: Exception) { holder.img.setImageResource(R.drawable.logo) }
        } else holder.img.setImageResource(R.drawable.logo)

        // title
        holder.title.text = recipe.name

        // always red for favorites
        holder.heart.setImageResource(R.drawable.ic_favourite_filled)

        holder.heart.setOnClickListener {
            onHeartClick(recipe, position)
        }
    }
}