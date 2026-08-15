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

    val autoBackupEnabled: StateFlow<Boolean> =
        settingsRepository.getAutoBackupEnabledFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val backupTime: StateFlow<String> =
        settingsRepository.getAutoBackupTimeFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "23:30")

    val keepBackups: StateFlow<Int> =
        settingsRepository.getAutoBackupKeepCountFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)

    val lastSuccessfulBackup: StateFlow<Long?> =
        settingsRepository.getLastSuccessfulBackupFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val lastBackupStatus: StateFlow<String?> =
        settingsRepository.getLastBackupStatusFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val autoBackupFolderUri: StateFlow<String?> =
        settingsRepository.getAutoBackupFolderUriFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoBackupEnabled(enabled)
        }
    }

    fun setBackupTime(time: String) {
        viewModelScope.launch {
            settingsRepository.setAutoBackupTime(time)
        }
    }

    fun setKeepBackups(count: Int) {
        viewModelScope.launch {
            settingsRepository.setAutoBackupKeepCount(count)
        }
    }

    fun setAutoBackupFolderUri(uriStr: String?) {
        viewModelScope.launch {
            settingsRepository.setAutoBackupFolderUri(uriStr)
        }
    }
}
