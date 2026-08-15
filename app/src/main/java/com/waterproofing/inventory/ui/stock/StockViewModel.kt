package com.waterproofing.inventory.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterproofing.inventory.data.entity.StockTransactionEntity
import com.waterproofing.inventory.data.model.BatchWithProductInfo
import com.waterproofing.inventory.data.model.ProductWithCategory
import com.waterproofing.inventory.data.model.StockTransactionWithDetails
import com.waterproofing.inventory.data.model.VariantWithStock
import com.waterproofing.inventory.data.repository.BatchRepository
import com.waterproofing.inventory.data.repository.ProductRepository
import com.waterproofing.inventory.data.repository.TransactionRepository
import com.waterproofing.inventory.data.repository.VariantRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class StockOperationResult {
    object Success : StockOperationResult()
    data class Error(val message: String) : StockOperationResult()
}

class StockViewModel(
    private val productRepository: ProductRepository,
    private val variantRepository: VariantRepository,
    private val batchRepository: BatchRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    // All active (non-archived) products for dropdowns
    val allProducts: StateFlow<List<ProductWithCategory>> =
        productRepository.activeProducts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected product drives the variant list
    private val _selectedProductId = MutableStateFlow<Long?>(null)
    val selectedProductId: StateFlow<Long?> = _selectedProductId

    @OptIn(ExperimentalCoroutinesApi::class)
    val variantsForProduct: StateFlow<List<VariantWithStock>> = _selectedProductId
        .flatMapLatest { id ->
            if (id != null) variantRepository.getActiveVariantsForProduct(id)
            else MutableStateFlow(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected variant drives the batch list (FEFO sorted)
    private val _selectedVariantId = MutableStateFlow<Long?>(null)
    val selectedVariantId: StateFlow<Long?> = _selectedVariantId

    @OptIn(ExperimentalCoroutinesApi::class)
    val batchesForVariant: StateFlow<List<BatchWithProductInfo>> = _selectedVariantId
        .flatMapLatest { id ->
            if (id != null) batchRepository.getBatchesWithProductInfoByVariant(id)
            else MutableStateFlow(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recent transaction history (global)
    val recentTransactions: StateFlow<List<StockTransactionWithDetails>> =
        transactionRepository.getRecentTransactions(50)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All transactions (for full history screen)
    val allTransactions: StateFlow<List<StockTransactionWithDetails>> =
        transactionRepository.getAllTransactions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transaction History Search & Filter State
    val searchQuery = MutableStateFlow("")
    val fromDateMillis = MutableStateFlow<Long?>(null)
    val toDateMillis = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchedTransactions: StateFlow<List<StockTransactionWithDetails>> = combine(
        searchQuery,
        fromDateMillis,
        toDateMillis
    ) { query, fromMs, toMs ->
        Triple(query, fromMs, toMs)
    }.flatMapLatest { (query, fromMs, toMs) ->
        val namePattern = if (query.isBlank()) "%" else "%${query.trim()}%"
        val start = fromMs ?: 0L
        // Include full day of toMs if specified
        val end = toMs?.let { it + 86_399_999L } ?: Long.MAX_VALUE
        transactionRepository.searchTransactions(namePattern, start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setDateRange(fromMs: Long?, toMs: Long?) {
        fromDateMillis.value = fromMs
        toDateMillis.value = toMs
    }

    fun clearSearchFilters() {
        searchQuery.value = ""
        fromDateMillis.value = null
        toDateMillis.value = null
    }

    fun selectProduct(productId: Long?) {
        _selectedProductId.value = productId
        _selectedVariantId.value = null
    }

    fun selectVariant(variantId: Long?) {
        _selectedVariantId.value = variantId
    }

    // ---- STOCK IN -------------------------------------------------------
    fun stockIn(
        batchId: Long,
        variantId: Long,
        productId: Long,
        quantity: Double,
        unit: String,
        reason: String,
        invoiceNumber: String?,
        notes: String?,
        onResult: (StockOperationResult) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val batch = batchRepository.getBatchById(batchId)
                    ?: return@launch onResult(StockOperationResult.Error("Batch not found."))

                val updated = batch.copy(
                    currentQuantity = batch.currentQuantity + quantity,
                    isDepleted = false,
                    updatedAt = System.currentTimeMillis()
                )
                batchRepository.updateBatch(updated)

                transactionRepository.insert(
                    StockTransactionEntity(
                        batchId = batchId,
                        variantId = variantId,
                        productId = productId,
                        transactionType = "IN",
                        quantity = quantity,
                        unit = unit,
                        reason = reason.ifBlank { "Received" },
                        invoiceNumber = invoiceNumber?.trim()?.takeIf { it.isNotEmpty() },
                        notes = notes?.trim()?.takeIf { it.isNotEmpty() }
                    )
                )
                onResult(StockOperationResult.Success)
            } catch (e: Exception) {
                onResult(StockOperationResult.Error(e.message ?: "Unknown error"))
            }
        }
    }

    // ---- STOCK OUT (FEFO) -----------------------------------------------
    fun stockOut(
        batchId: Long,
        variantId: Long,
        productId: Long,
        quantity: Double,
        unit: String,
        reason: String,
        customerProject: String?,
        invoiceNumber: String?,
        notes: String?,
        onResult: (StockOperationResult) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val batch = batchRepository.getBatchById(batchId)
                    ?: return@launch onResult(StockOperationResult.Error("Batch not found."))

                if (batch.currentQuantity < quantity) {
                    return@launch onResult(
                        StockOperationResult.Error(
                            "Insufficient stock. Available: ${batch.currentQuantity} ${unit}"
                        )
                    )
                }

                val newQty = batch.currentQuantity - quantity
                val updated = batch.copy(
                    currentQuantity = newQty,
                    isDepleted = newQty <= 0.0,
                    updatedAt = System.currentTimeMillis()
                )
                batchRepository.updateBatch(updated)

                transactionRepository.insert(
                    StockTransactionEntity(
                        batchId = batchId,
                        variantId = variantId,
                        productId = productId,
                        transactionType = "OUT",
                        quantity = quantity,
                        unit = unit,
                        reason = reason.ifBlank { "Issued" },
                        customerProject = customerProject?.trim()?.takeIf { it.isNotEmpty() },
                        invoiceNumber = invoiceNumber?.trim()?.takeIf { it.isNotEmpty() },
                        notes = notes?.trim()?.takeIf { it.isNotEmpty() }
                    )
                )
                onResult(StockOperationResult.Success)
            } catch (e: Exception) {
                onResult(StockOperationResult.Error(e.message ?: "Unknown error"))
            }
        }
    }

    // ---- ADJUSTMENT -----------------------------------------------
    fun adjustStock(
        batchId: Long,
        variantId: Long,
        productId: Long,
        newQuantity: Double,
        unit: String,
        reason: String,
        notes: String?,
        onResult: (StockOperationResult) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val batch = batchRepository.getBatchById(batchId)
                    ?: return@launch onResult(StockOperationResult.Error("Batch not found."))

                val delta = newQuantity - batch.currentQuantity
                val updated = batch.copy(
                    currentQuantity = newQuantity,
                    isDepleted = newQuantity <= 0.0,
                    updatedAt = System.currentTimeMillis()
                )
                batchRepository.updateBatch(updated)

                transactionRepository.insert(
                    StockTransactionEntity(
                        batchId = batchId,
                        variantId = variantId,
                        productId = productId,
                        transactionType = "ADJUSTMENT",
                        quantity = delta,
                        unit = unit,
                        reason = reason.ifBlank { "Manual Adjustment" },
                        notes = notes?.trim()?.takeIf { it.isNotEmpty() }
                    )
                )
                onResult(StockOperationResult.Success)
            } catch (e: Exception) {
                onResult(StockOperationResult.Error(e.message ?: "Unknown error"))
            }
        }
    }
}
