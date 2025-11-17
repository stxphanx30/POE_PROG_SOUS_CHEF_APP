package com.example.logintemp.data.pantry

class PantryRepository(private val dao: PantryDao) {

    fun getPantryItems(userId: Int) = dao.getAllItems(userId)

    suspend fun addItem(userId: Int, name: String, category: String) {
        dao.insertItem(PantryItem(userId = userId, name = name, category = category))
    }

    suspend fun clearCategory(userId: Int, category: String) {
        dao.clearCategory(userId, category)
    }
}
