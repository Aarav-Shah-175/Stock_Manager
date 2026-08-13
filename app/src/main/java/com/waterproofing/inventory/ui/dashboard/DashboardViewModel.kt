package com.waterproofing.inventory.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterproofing.inventory.data.model.BatchWithProductInfo
import com.waterproofing.inventory.data.model.LowStockVariant
import com.waterproofing.inventory.data.model.StockTransactionWithDetails
import com.waterproofing.inventory.data.repository.BatchRepository
import com.waterproofing.inventory.data.repository.ProductRepository
import com.waterproofing.inventory.data.repository.TransactionRepository
import com.waterproofing.inventory.data.repository.VariantRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private val WARNING_MS = 90L * 24 * 60 * 60 * 1000 // 90 days

class DashboardViewModel(
    productRepository: ProductRepository,
    variantRepository: VariantRepository,
    batchRepository: BatchRepository,
    transactionRepository: TransactionRepository
) : ViewModel() {

    val activeProductCount: StateFlow<Int> =
        productRepository.activeProductCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeVariantCount: StateFlow<Int> =
        variantRepository.activeVariantCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lowStockVariants: StateFlow<List<LowStockVariant>> =
        variantRepository.lowStockVariants
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiredBatches: StateFlow<List<BatchWithProductInfo>> =
        batchRepository.getExpiredBatches()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiringSoonBatches: StateFlow<List<BatchWithProductInfo>> =
        batchRepository.getExpiringSoonBatches(WARNING_MS)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<StockTransactionWithDetails>> =
        transactionRepository.getRecentTransactions(5)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
