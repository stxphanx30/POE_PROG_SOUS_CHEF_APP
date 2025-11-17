package com.example.logintemp.data.mealplan3

import androidx.room.ColumnInfo

data class MealPlanWithRecipe(
    val id: Long,
    val planDateEpoch: Long,
    val recipeId: Long,
    val name: String,
    val category: String,
    val imageUri: String?,
    val cookTimeMinutes: Int?
)