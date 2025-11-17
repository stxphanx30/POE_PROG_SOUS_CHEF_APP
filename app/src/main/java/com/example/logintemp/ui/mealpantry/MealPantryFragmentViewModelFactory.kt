package com.example.logintemp.ui.mealpantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.logintemp.data.pantry.PantryRepository

class MealPantryFragmentViewModelFactory(
    private val repo: PantryRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MealPantryFragmentViewModel(repo, userId) as T
    }
}
