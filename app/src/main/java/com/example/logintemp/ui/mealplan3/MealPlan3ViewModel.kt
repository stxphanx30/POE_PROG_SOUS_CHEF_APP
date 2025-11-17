package com.example.logintemp.ui.mealplan3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.logintemp.data.mealplan3.MealPlanRepository
import com.example.logintemp.data.mealplan3.MealPlanEntity
import com.example.logintemp.data.recipe.RecipeDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealPlan3ViewModel(
    private val recipeDao: RecipeDao,
    private val repo: MealPlanRepository
) : ViewModel() {

    /** Used by the picker: returns (id, name) of all recipes. */
    suspend fun loadRecipes(): List<RecipeDao.RecipeIdName> = withContext(Dispatchers.IO) {
        recipeDao.listRecipeIdAndName()
    }

    /** Save a MealPlan row. */
    suspend fun savePlan(recipeId: Long, dateEpoch: Long) = withContext(Dispatchers.IO) {
        repo.addMealPlan(MealPlanEntity(recipeId = recipeId, planDateEpoch = dateEpoch))
    }
}

class MealPlan3VMFactory(
    private val recipeDao: RecipeDao,
    private val repo: MealPlanRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealPlan3ViewModel::class.java)) {
            return MealPlan3ViewModel(recipeDao, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
