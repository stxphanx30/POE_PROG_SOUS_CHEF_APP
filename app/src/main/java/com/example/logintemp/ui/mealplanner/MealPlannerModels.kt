package com.example.logintemp.ui.mealplanner

data class MealPlanDay(
    val date: String,
    val recipes: List<RecipeMock>
)

data class RecipeMock(
    val title: String,
    val category: String,
    val imageRes: Int
)
