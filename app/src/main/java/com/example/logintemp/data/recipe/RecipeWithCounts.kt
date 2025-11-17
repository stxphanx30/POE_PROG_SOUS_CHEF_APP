package com.example.logintemp.data.recipe

import androidx.room.Embedded

data class RecipeWithCounts(
    @Embedded val recipe: RecipeEntity,
    val ingredientCount: Int
)