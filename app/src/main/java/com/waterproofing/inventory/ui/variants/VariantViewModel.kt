package com.waterproofing.inventory.ui.variants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterproofing.inventory.data.entity.BatchEntity
import com.waterproofing.inventory.data.model.BatchWithProductInfo
import com.waterproofing.inventory.data.model.VariantWithStock
import com.waterproofing.inventory.data.repository.BatchRepository
import com.waterproofing.inventory.data.repository.VariantRepository
import com.waterproofing.inventory.domain.ExpiryCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VariantViewModel(
    private val variantRepository: VariantRepository,
    private val batchRepository: BatchRepository
) : ViewModel() {

    private val variantIdState = MutableStateFlow<Long?>(null)

    fun setVariantId(id: Long) {
        variantIdState.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val variant: StateFlow<VariantWithStock?> = variantIdState
        .flatMapLatest { id ->
            if (id != null) {
                variantRepository.getVariantWithStock(id)
            } else {
                MutableStateFlow(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val batches: StateFlow<List<BatchWithProductInfo>> = variantIdState
        .flatMapLatest { id ->
            if (id != null) {
                batchRepository.getBatchesWithProductInfoByVariant(id)
            } else {
                MutableStateFlow(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBatch(
        batchNumber: String,
        currentQuantity: Double,
        mfgDate: Long?,
        shelfLifeValue: Int?,
        shelfLifeUnit: String?,
        expiryDate: Long,
        purchasePrice: Double?,
        supplier: String?,
        invoiceNumber: String?,
        notes: String?
    ) {
        val vid = variantIdState.value ?: return
        viewModelScope.launch {
            val batch = BatchEntity(
                variantId = vid,
                batchNumber = batchNumber.trim(),
                currentQuantity = currentQuantity,
                mfgDate = mfgDate,
                shelfLifeValue = shelfLifeValue,
                shelfLifeUnit = shelfLifeUnit,
                expiryDate = expiryDate,
                purchasePrice = purchasePrice,
                supplier = supplier?.trim()?.takeIf { it.isNotEmpty() },
                invoiceNumber = invoiceNumber?.trim()?.takeIf { it.isNotEmpty() },
                notes = notes?.trim()?.takeIf { it.isNotEmpty() },
                isDepleted = currentQuantity <= 0.0
            )
            batchRepository.insertBatch(batch)
        }
    }

    fun updateBatch(
        batchId: Long,
        batchNumber: String,
        currentQuantity: Double,
        mfgDate: Long?,
        shelfLifeValue: Int?,
        shelfLifeUnit: String?,
        expiryDate: Long,
        purchasePrice: Double?,
        supplier: String?,
        invoiceNumber: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val existing = batchRepository.getBatchById(batchId) ?: return@launch
            val updated = existing.copy(
                batchNumber = batchNumber.trim(),
                currentQuantity = currentQuantity,
                mfgDate = mfgDate,
                shelfLifeValue = shelfLifeValue,
                shelfLifeUnit = shelfLifeUnit,
                expiryDate = expiryDate,
                purchasePrice = purchasePrice,
                supplier = supplier?.trim()?.takeIf { it.isNotEmpty() },
                invoiceNumber = invoiceNumber?.trim()?.takeIf { it.isNotEmpty() },
                notes = notes?.trim()?.takeIf { it.isNotEmpty() },
                isDepleted = currentQuantity <= 0.0,
                updatedAt = System.currentTimeMillis()
            )
            batchRepository.updateBatch(updated)
        }
    }
}
