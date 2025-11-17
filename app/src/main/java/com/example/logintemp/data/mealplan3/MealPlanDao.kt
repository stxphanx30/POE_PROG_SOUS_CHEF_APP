package com.example.logintemp.data.mealplan3

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MealPlanDao {

        @Insert
        suspend fun insert(row: MealPlanEntity): Long

        @Query("""  
        SELECT 
            mp.id AS id,
            mp.planDateEpoch AS planDateEpoch,
            r.id AS recipeId,
            r.name AS name,
            r.category AS category,
            r.image_uri AS imageUri,
            r.cook_time_minutes AS cookTimeMinutes
        FROM meal_plans mp
        INNER JOIN recipes r ON r.id = mp.recipeId
        ORDER BY mp.planDateEpoch ASC, mp.id ASC
    """)
        suspend fun getAllPlansWithRecipe(): List<MealPlanWithRecipe>
}