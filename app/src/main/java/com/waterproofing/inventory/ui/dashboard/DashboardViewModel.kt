package com.waterproofing.inventory.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterproofing.inventory.data.model.BatchWithProductInfo
import com.waterproofing.inventory.data.model.LowStockVariant
import com.waterproofing.inventory.data.model.StockTransactionWithDetails
import com.waterproofing.inventory.data.repository.BatchRepository
import com.waterproofing.inventory.data.repository.ProductRepository
import com.waterproofing.inventory.data.repository.SettingsRepository
import com.waterproofing.inventory.data.repository.TransactionRepository
import com.waterproofing.inventory.data.repository.VariantRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    productRepository: ProductRepository,
    variantRepository: VariantRepository,
    private val batchRepository: BatchRepository,
    transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository
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

    val expiryWarningDays: StateFlow<Int> =
        settingsRepository.getExpiryWarningDaysFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 90)

    val companyName: StateFlow<String> =
        settingsRepository.getCompanyNameFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    @OptIn(ExperimentalCoroutinesApi::class)
    val expiringSoonBatches: StateFlow<List<BatchWithProductInfo>> =
        settingsRepository.getExpiryWarningDaysFlow()
            .flatMapLatest { days ->
                val warningMs = days.toLong() * 24L * 60L * 60L * 1000L
                batchRepository.getExpiringSoonBatches(warningMs)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<StockTransactionWithDetails>> =
        transactionRepository.getRecentTransactions(5)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
