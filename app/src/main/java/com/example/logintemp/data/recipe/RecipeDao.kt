package com.example.logintemp.data.recipe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    /* ---------------- INSERTS ---------------- */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(rows: List<IngredientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(rows: List<StepEntity>)

    @Transaction
    suspend fun insertRecipeWithChildren(
        recipe: RecipeEntity,
        ingredients: List<Pair<String, String>>,
        steps: List<String>
    ): Long {
        val recipeId = insertRecipe(recipe)

        if (ingredients.isNotEmpty()) {
            val ingRows = ingredients.mapIndexed { idx, (name, amount) ->
                IngredientEntity(
                    recipeId = recipeId,
                    name = name,
                    amount = amount,
                    order = idx
                )
            }
            insertIngredients(ingRows)
        }

        if (steps.isNotEmpty()) {
            val stepRows = steps.mapIndexed { idx, text ->
                StepEntity(
                    recipeId = recipeId,
                    text = text,
                    order = idx
                )
            }
            insertSteps(stepRows)
        }

        return recipeId
    }

    /* ---------------- QUERIES ---------------- */

    @Query("""
        SELECT r.*, COUNT(i.id) AS ingredientCount
        FROM recipes r
        LEFT JOIN ingredients i ON i.recipeId = r.id
        WHERE r.user_id = :userId
        GROUP BY r.id
        ORDER BY r.createdAt DESC
    """)
    fun getRecipesWithCounts(userId: Int): Flow<List<RecipeWithCounts>>

    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId ORDER BY 'order' ASC")
    fun getIngredientsFor(recipeId: Long): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM steps WHERE recipeId = :recipeId ORDER BY 'order' ASC")
    fun getStepsFor(recipeId: Long): Flow<List<StepEntity>>

    @Query("SELECT * FROM recipes WHERE user_id = :userId ORDER BY createdAt DESC")
    suspend fun getAllRecipesForUser(userId: Int): List<RecipeEntity>

    @Query("SELECT id, name FROM recipes ORDER BY name ASC")
    suspend fun listRecipeIdAndName(): List<RecipeIdName>
    // RecipeDao
    @Query("SELECT * FROM Recipes WHERE id = :id")
    suspend fun getById(id: Long): RecipeEntity?

    @Query("DELETE FROM Recipes WHERE id = :id")
    suspend fun deleteById(id: Long)

    // suspend versions to fetch lists once (not Flow)
    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId ORDER BY 'order' ASC")
    suspend fun getIngredientsForOnce(recipeId: Long): List<IngredientEntity>

    @Query("SELECT * FROM steps WHERE recipeId = :recipeId ORDER BY 'order' ASC")
    suspend fun getStepsForOnce(recipeId: Long): List<StepEntity>

// If you also have Flow versions, keep them (Flow<List<...>>) if used elsewhere.
    /* ---------- FAVORITES MANAGEMENT ---------- */

    @Query("UPDATE recipes SET is_favorite = :fav WHERE id = :recipeId")
    suspend fun updateFavorite(recipeId: Long, fav: Boolean)

    @Query("SELECT * FROM recipes WHERE is_favorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteRecipesFlow(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE is_favorite = 1 ORDER BY createdAt DESC")
    suspend fun getFavoriteRecipes(): List<RecipeEntity>

    /* Small helper class */
    data class RecipeIdName(
        val id: Long,
        val name: String
    )
}