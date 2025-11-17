package com.example.logintemp.ui.add_recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.logintemp.data.recipe.RecipeRepository

class CreateRecipeViewModel(private val repo: RecipeRepository) : ViewModel() {
    var title: String? = null
    var category: String? = null
    var cookTimeMinutes: Int? = null
    var imageUri: String? = null
    val ingredients = mutableListOf<Pair<String,String>>()
    val steps = mutableListOf<String>()

    fun clear() {
        title = null
        category = null
        imageUri = null
        ingredients.clear()
        steps.clear()
    }

    suspend fun save(userId: Int): Long {
        return repo.saveRecipe(
            userId = userId,
            title = title.orEmpty(),
            category = category.orEmpty(),
            cookTimeMinutes=cookTimeMinutes,
                    imageUri = imageUri,
            ingredients = ingredients.toList(),
            steps = steps.toList()
        )
    }
}

class CreateRecipeVMFactory(private val repo: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateRecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateRecipeViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
