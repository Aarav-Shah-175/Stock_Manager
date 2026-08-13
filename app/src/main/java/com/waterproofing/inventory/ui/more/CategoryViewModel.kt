package com.waterproofing.inventory.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterproofing.inventory.data.entity.CategoryEntity
import com.waterproofing.inventory.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                categoryRepository.insert(name)
            }
        }
    }

    fun updateCategory(category: CategoryEntity, newName: String) {
        viewModelScope.launch {
            if (newName.isNotBlank()) {
                categoryRepository.update(category.copy(name = newName))
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.delete(category)
        }
    }
}
