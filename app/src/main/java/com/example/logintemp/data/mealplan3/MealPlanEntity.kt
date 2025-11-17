package com.example.logintemp.data.mealplan3

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.logintemp.data.recipe.RecipeEntity  // make sure this import matches your package!

@Entity(
    tableName = "meal_plans",
    foreignKeys = [ForeignKey(
        entity = RecipeEntity::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("recipeId"), Index("planDateEpoch")]
)
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeId: Long,
    val planDateEpoch: Long
)
