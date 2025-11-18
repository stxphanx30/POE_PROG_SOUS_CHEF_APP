package com.example.logintemp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.logintemp.data.mealplan3.MealPlanDao
import com.example.logintemp.data.mealplan3.MealPlanEntity
import com.example.logintemp.data.pantry.PantryDao
import com.example.logintemp.data.pantry.PantryItem
import com.example.logintemp.data.recipe.IngredientEntity
import com.example.logintemp.data.recipe.RecipeDao
import com.example.logintemp.data.recipe.RecipeEntity
import com.example.logintemp.data.recipe.StepEntity
import com.example.logintemp.data.user.User
import com.example.logintemp.data.notification.NotificationDao
import com.example.logintemp.data.notification.NotificationEntity
import com.example.logintemp.data.user.UserDao

@Database(
    entities = [
        User::class,
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        MealPlanEntity::class,
        PantryItem::class,
        NotificationEntity::class,
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun userDao(): UserDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun pantryDao(): PantryDao
    abstract fun notificationDao(): NotificationDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "souschef_db"
                )
                    .fallbackToDestructiveMigration() // OK en projet scolaire
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}