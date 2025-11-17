package com.example.logintemp.ui.mealpantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logintemp.data.pantry.PantryItem
import com.example.logintemp.data.pantry.PantryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MealPantryFragmentViewModel(private val repo: PantryRepository, private val userId: Int) : ViewModel() {

    private val _haveItems = MutableStateFlow<List<PantryItem>>(emptyList())
    val haveItems: StateFlow<List<PantryItem>> = _haveItems

    private val _toGetItems = MutableStateFlow<List<PantryItem>>(emptyList())
    val toGetItems: StateFlow<List<PantryItem>> = _toGetItems

    init {
        viewModelScope.launch {
            repo.getPantryItems(userId).collectLatest { items ->
                _haveItems.value = items.filter { it.category == "Have" }
                _toGetItems.value = items.filter { it.category == "To Get" }
            }
        }
    }

    fun addItem(name: String, category: String) {
        viewModelScope.launch { repo.addItem(userId, name, category) }
    }

    fun clearCategory(category: String) {
        viewModelScope.launch { repo.clearCategory(userId, category) }
    }
}
