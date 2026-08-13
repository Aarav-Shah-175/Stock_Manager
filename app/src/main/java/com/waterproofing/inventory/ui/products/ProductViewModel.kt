package com.waterproofing.inventory.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterproofing.inventory.data.entity.CategoryEntity
import com.waterproofing.inventory.data.entity.ProductEntity
import com.waterproofing.inventory.data.model.ProductWithCategory
import com.waterproofing.inventory.data.repository.CategoryRepository
import com.waterproofing.inventory.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedProducts: StateFlow<List<ProductWithCategory>> = productRepository.archivedProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<ProductWithCategory>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                productRepository.activeProducts
            } else {
                productRepository.searchProducts(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun addProduct(name: String, brand: String, categoryId: Long?, description: String) {
        viewModelScope.launch {
            if (name.isNotBlank() && brand.isNotBlank()) {
                productRepository.insert(name, brand, categoryId, description)
            }
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            if (product.name.isNotBlank() && product.brand.isNotBlank()) {
                productRepository.update(product)
            }
        }
    }

    fun archiveProduct(productId: Long) {
        viewModelScope.launch {
            productRepository.archiveProduct(productId)
        }
    }

    fun restoreProduct(productId: Long) {
        viewModelScope.launch {
            productRepository.restoreProduct(productId)
        }
    }
}
