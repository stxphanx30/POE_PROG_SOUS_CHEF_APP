package com.example.logintemp.ui.mealplanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.logintemp.R

class MealPlannerViewModel : ViewModel() {

    private val _days = MutableLiveData<List<MealPlanDay>>()
    val days: LiveData<List<MealPlanDay>> = _days

    init {
        // Mock data for now
        _days.value = listOf(
            MealPlanDay(
                date = "Monday, Oct 6",
                recipes = listOf(
                    RecipeMock("Chicken Biryani", "Dinner", R.drawable.sample_biryani),
                    RecipeMock("Omelette", "Breakfast", R.drawable.sample_pizza)
                )
            ),
            MealPlanDay(
                date = "Tuesday, Oct 7",
                recipes = listOf(
                    RecipeMock("Chocolate Cake", "Dessert", R.drawable.sample_cake),
                    RecipeMock("Salmon Bowl", "Lunch", R.drawable.sample_pizza)
                )
            ),
            MealPlanDay(
                date = "Wednesday, Oct 8",
                recipes = listOf(
                    RecipeMock("Beef Tacos", "Lunch", R.drawable.sample_biryani)
                )
            )
        )
    }
}
