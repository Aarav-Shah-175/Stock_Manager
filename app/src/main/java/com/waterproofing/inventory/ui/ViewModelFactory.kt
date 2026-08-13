package com.waterproofing.inventory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.waterproofing.inventory.data.repository.BatchRepository
import com.waterproofing.inventory.data.repository.CategoryRepository
import com.waterproofing.inventory.data.repository.ProductRepository
import com.waterproofing.inventory.data.repository.SettingsRepository
import com.waterproofing.inventory.data.repository.TransactionRepository
import com.waterproofing.inventory.data.repository.VariantRepository
import com.waterproofing.inventory.ui.dashboard.DashboardViewModel
import com.waterproofing.inventory.ui.more.CategoryViewModel
import com.waterproofing.inventory.ui.more.SettingsViewModel
import com.waterproofing.inventory.ui.products.ProductDetailViewModel
import com.waterproofing.inventory.ui.products.ProductViewModel
import com.waterproofing.inventory.ui.stock.StockViewModel
import com.waterproofing.inventory.ui.variants.VariantViewModel

class ViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val variantRepository: VariantRepository,
    private val batchRepository: BatchRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(productRepository, variantRepository, batchRepository, transactionRepository, settingsRepository) as T
            }
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
            modelClass.isAssignableFrom(StockViewModel::class.java) -> {
                StockViewModel(productRepository, variantRepository, batchRepository, transactionRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(settingsRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
