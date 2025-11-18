package com.example.logintemp.data.recipe

class RecipeRepository(private val dao: RecipeDao) {

    fun getRecipes(userId: Int) = dao.getRecipesWithCounts(userId)

    suspend fun saveRecipe(
        userId: Int,
        title: String,
        category: String,
        cookTimeMinutes: Int?,
        imageUri: String?,
        ingredients: List<Pair<String, String>>,
        steps: List<String>
    ): Long {

        val recipe = RecipeEntity(
            userId = userId,
            name = title,
            category = category,
            cookTimeMinutes = cookTimeMinutes,
            imageUri = imageUri,
            isFavorite = false   // always false on creation
        )

        return dao.insertRecipeWithChildren(recipe, ingredients, steps)
    }

    suspend fun toggleFavorite(recipeId: Long, fav: Boolean) {
        dao.updateFavorite(recipeId, fav)
    }

    fun getFavoriteRecipesFlow() = dao.getFavoriteRecipesFlow()

    suspend fun getFavoriteRecipes() = dao.getFavoriteRecipes()
    // in RecipeRepository.kt (add these suspending methods)
    suspend fun getById(id: Long): RecipeEntity? {
        return dao.getById(id)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun getIngredientsFor(recipeId: Long): List<IngredientEntity> {
        return dao.getIngredientsForOnce(recipeId)
    }

    suspend fun getStepsFor(recipeId: Long): List<StepEntity> {
        return dao.getStepsForOnce(recipeId)
    }
}