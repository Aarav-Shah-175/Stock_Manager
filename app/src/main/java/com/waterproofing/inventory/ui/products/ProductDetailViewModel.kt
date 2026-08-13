package com.waterproofing.inventory.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterproofing.inventory.data.entity.VariantEntity
import com.waterproofing.inventory.data.model.ProductWithCategory
import com.waterproofing.inventory.data.model.VariantWithStock
import com.waterproofing.inventory.data.repository.ProductRepository
import com.waterproofing.inventory.data.repository.VariantRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val productRepository: ProductRepository,
    private val variantRepository: VariantRepository
) : ViewModel() {

    private val productIdState = MutableStateFlow<Long?>(null)

    fun setProductId(id: Long) {
        productIdState.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val product: StateFlow<ProductWithCategory?> = productIdState
        .flatMapLatest { id ->
            if (id != null) {
                productRepository.getProductWithCategoryFlow(id)
            } else {
                MutableStateFlow(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val variants: StateFlow<List<VariantWithStock>> = productIdState
        .flatMapLatest { id ->
            if (id != null) {
                variantRepository.getActiveVariantsForProduct(id)
            } else {
                MutableStateFlow(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addVariant(name: String, quantityValue: Double, unit: String, minStockThreshold: Double) {
        val pid = productIdState.value ?: return
        viewModelScope.launch {
            if (name.isNotBlank() && unit.isNotBlank()) {
                variantRepository.insert(pid, name, quantityValue, unit, minStockThreshold)
            }
        }
    }

    fun updateVariant(variant: VariantEntity) {
        viewModelScope.launch {
            if (variant.name.isNotBlank() && variant.unit.isNotBlank()) {
                variantRepository.update(variant)
            }
        }
    }

    fun archiveVariant(variantId: Long) {
        viewModelScope.launch {
            variantRepository.archiveVariant(variantId)
        }
    }

    fun deleteProduct(onSuccess: () -> Unit) {
        val pid = productIdState.value ?: return
        viewModelScope.launch {
            productRepository.deleteProduct(pid)
            onSuccess()
        }
    }
}
