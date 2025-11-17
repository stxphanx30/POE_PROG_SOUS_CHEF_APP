package com.example.logintemp.ui.category


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.logintemp.R

import com.example.logintemp.data.MealDBData.Meal

class CategoryMealAdapter(
    private var meals: List<Meal>,
    private val onItemClick: (Meal) -> Unit
) : RecyclerView.Adapter<CategoryMealAdapter.MealViewHolder>() {

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealImage: ImageView = itemView.findViewById(R.id.mealImage)
        val mealName: TextView = itemView.findViewById(R.id.mealName)
        val mealDuration: TextView = itemView.findViewById(R.id.mealDuration)
        val mealIngredients: TextView = itemView.findViewById(R.id.mealIngredients)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.favoriteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]

        holder.mealName.text = meal.strMeal

        // Fake duration since API doesn’t have it
        val duration = (15..40).random()
        holder.mealDuration.text = "$duration min"

        // Ingredient count = not available in filter API → default 8–15
        holder.mealIngredients.text = "Ingredients: ${(7..15).random()}"

        Glide.with(holder.itemView.context)
            .load(meal.strMealThumb)
            .centerCrop()
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.mealImage)

        holder.itemView.setOnClickListener {
            onItemClick(meal)
        }

        holder.favoriteIcon.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Added to favorites!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = meals.size

    fun updateMeals(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }
}
