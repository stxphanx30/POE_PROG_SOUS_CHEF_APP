package com.example.logintemp.api

import com.example.logintemp.data.MealDBData.CategoryResponse
import com.example.logintemp.data.MealDBData.MealResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MealDBApi {
    // Get random meals
    @GET("random.php")
    fun getRandomMeal(): Call<MealResponse>

    // Get meals by category
    @GET("filter.php")
    fun getMealsByCategory(@Query("c") category: String): Call<MealResponse>

    // List all categories
    @GET("categories.php")
    fun getCategories(): Call<CategoryResponse>

    // Get meal details by ID
    @GET("lookup.php")
    fun getMealDetails(@Query("i") mealId: String): Call<MealResponse>

    @GET("search.php")
    fun searchMeals(@Query("s") query: String): Call<MealResponse>


}
