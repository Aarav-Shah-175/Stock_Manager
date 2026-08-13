package com.waterproofing.inventory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.waterproofing.inventory.data.repository.BatchRepository
import com.waterproofing.inventory.data.repository.CategoryRepository
import com.waterproofing.inventory.data.repository.ProductRepository
import com.waterproofing.inventory.data.repository.VariantRepository
import com.waterproofing.inventory.ui.more.CategoryViewModel
import com.waterproofing.inventory.ui.products.ProductDetailViewModel
import com.waterproofing.inventory.ui.products.ProductViewModel
import com.waterproofing.inventory.ui.variants.VariantViewModel

class ViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val variantRepository: VariantRepository,
    private val batchRepository: BatchRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CategoryViewModel::class.java) -> {
                CategoryViewModel(categoryRepository) as T
            }
            modelClass.isAssignableFrom(ProductViewModel::class.java) -> {
                ProductViewModel(productRepository, categoryRepository) as T
            }
            modelClass.isAssignableFrom(ProductDetailViewModel::class.java) -> {
                ProductDetailViewModel(productRepository, variantRepository) as T
            }
            modelClass.isAssignableFrom(VariantViewModel::class.java) -> {
                VariantViewModel(variantRepository, batchRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
