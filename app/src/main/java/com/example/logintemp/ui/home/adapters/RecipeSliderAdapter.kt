package com.example.logintemp.ui.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.logintemp.R
import com.example.logintemp.data.MealDBData.Meal

class RecipeSliderAdapter(
    private var meals: List<Meal>,
    private val onItemClick: (Meal) -> Unit // make it non-null to enforce handling
) : RecyclerView.Adapter<RecipeSliderAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealImage: ImageView = itemView.findViewById(R.id.sliderMealImage)
        val mealName: TextView = itemView.findViewById(R.id.sliderMealName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_slider, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val meal = meals[position]

        holder.mealName.text = meal.strMeal

        // Load meal image with Glide
        Glide.with(holder.itemView.context)
            .load(meal.strMealThumb)
            .centerCrop()
            .placeholder(R.drawable.ic_placeholder) // fallback image
            .into(holder.mealImage)

        // Handle click → pass meal back
        holder.itemView.setOnClickListener {
            onItemClick(meal)
        }
    }

    override fun getItemCount(): Int = meals.size

    fun updateMeals(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }
}
