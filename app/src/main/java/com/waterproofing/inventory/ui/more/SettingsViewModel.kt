package com.waterproofing.inventory.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterproofing.inventory.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val expiryWarningDays: StateFlow<Int> =
        settingsRepository.getExpiryWarningDaysFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 90)

    val companyName: StateFlow<String> =
        settingsRepository.getCompanyNameFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val defaultCurrency: StateFlow<String> =
        settingsRepository.getDefaultCurrencyFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "INR")

    fun setExpiryWarningDays(days: Int) {
        viewModelScope.launch {
            settingsRepository.setExpiryWarningDays(days)
        }
    }

    fun setCompanyName(name: String) {
        viewModelScope.launch {
            settingsRepository.setCompanyName(name)
        }
    }

    fun setDefaultCurrency(currency: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultCurrency(currency)
        }
    }
}
