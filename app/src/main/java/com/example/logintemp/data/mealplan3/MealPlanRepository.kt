package com.example.logintemp.data.mealplan3

class MealPlanRepository(private val dao: MealPlanDao) {
    suspend fun addMealPlan(entity: MealPlanEntity): Long = dao.insert(entity)
}