package com.example.logintemp.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.logintemp.R
import com.example.logintemp.data.MealDBData.Meal

class CategoryMealAdapter(
    private var items: List<Meal>,
    private val onClick: (Meal) -> Unit
) : RecyclerView.Adapter<CategoryMealAdapter.MealViewHolder>() {

    // map mealId -> ingredient count (optionnel)
    var ingredientCounts: Map<String, Int> = emptyMap()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    // map mealId -> real cook time (nullable)
    var minutesMap: Map<String, Int?> = emptyMap()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealImage: ImageView = itemView.findViewById(R.id.mealImage)
        val mealName: TextView = itemView.findViewById(R.id.mealName)
        val mealDuration: TextView = itemView.findViewById(R.id.mealDuration)
        val mealIngredients: TextView = itemView.findViewById(R.id.mealIngredients)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_meal, parent, false)
        return MealViewHolder(v)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = items[position]
        holder.mealName.text = meal.strMeal ?: "Unknown"

        // ingredients count if available
        val cnt = ingredientCounts[meal.idMeal]
        holder.mealIngredients.text = if (cnt != null) "$cnt ingredients" else ""

        // Use real minutes if present in minutesMap
        val minutes = minutesMap[meal.idMeal]
        if (minutes != null) {
            holder.mealDuration.text = "$minutes min"
        } else {
            holder.mealDuration.text = "" // no estimate, keep empty
        }

        // Glide image
        val url = meal.strMealThumb
        if (!url.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.logo)
                .into(holder.mealImage)
        } else {
            holder.mealImage.setImageResource(R.drawable.logo)
        }

        holder.itemView.setOnClickListener { onClick(meal) }
    }

    override fun getItemCount(): Int = items.size

    fun updateMeals(newList: List<Meal>) {
        items = newList
        notifyDataSetChanged()
    }
}