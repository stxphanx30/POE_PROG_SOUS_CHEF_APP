package com.example.logintemp.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.logintemp.R
import com.example.logintemp.data.MealDBData.Meal

class SearchResultsAdapter(
    private var meals: List<Meal>,
    private val onClick: (Meal) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgMeal: ImageView = view.findViewById(R.id.imgMeal)
        val tvMealName: TextView = view.findViewById(R.id.tvMealName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meal = meals[position]
        holder.tvMealName.text = meal.strMeal
        Glide.with(holder.itemView)
            .load(meal.strMealThumb)
            .into(holder.imgMeal)

        holder.itemView.setOnClickListener { onClick(meal) }
    }

    override fun getItemCount() = meals.size

    fun updateResults(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }
}