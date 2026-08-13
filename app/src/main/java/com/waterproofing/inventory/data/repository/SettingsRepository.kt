package com.waterproofing.inventory.data.repository

import com.waterproofing.inventory.data.dao.AppSettingsDao
import com.waterproofing.inventory.data.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val appSettingsDao: AppSettingsDao) {

    fun getExpiryWarningDaysFlow(): Flow<Int> {
        return appSettingsDao.getSettingValueFlow(KEY_EXPIRY_WARNING_DAYS)
            .map { it?.toIntOrNull() ?: DEFAULT_EXPIRY_WARNING_DAYS }
    }

    suspend fun getExpiryWarningDays(): Int {
        return appSettingsDao.getSettingValueDirect(KEY_EXPIRY_WARNING_DAYS)?.toIntOrNull()
            ?: DEFAULT_EXPIRY_WARNING_DAYS
    }

    suspend fun setExpiryWarningDays(days: Int) {
        appSettingsDao.setSetting(AppSettingsEntity(KEY_EXPIRY_WARNING_DAYS, days.toString()))
    }

    fun getCompanyNameFlow(): Flow<String> {
        return appSettingsDao.getSettingValueFlow(KEY_COMPANY_NAME)
            .map { it ?: "" }
    }

    suspend fun getCompanyName(): String {
        return appSettingsDao.getSettingValueDirect(KEY_COMPANY_NAME) ?: ""
    }

    suspend fun setCompanyName(name: String) {
        appSettingsDao.setSetting(AppSettingsEntity(KEY_COMPANY_NAME, name.trim()))
    }

    fun getDefaultCurrencyFlow(): Flow<String> {
        return appSettingsDao.getSettingValueFlow(KEY_DEFAULT_CURRENCY)
            .map { it ?: DEFAULT_CURRENCY }
    }

    suspend fun getDefaultCurrency(): String {
        return appSettingsDao.getSettingValueDirect(KEY_DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    }

    suspend fun setDefaultCurrency(currency: String) {
        appSettingsDao.setSetting(AppSettingsEntity(KEY_DEFAULT_CURRENCY, currency.trim()))
    }

    companion object {
        private const val KEY_EXPIRY_WARNING_DAYS = "expiry_warning_days"
        private const val KEY_COMPANY_NAME = "company_name"
        private const val KEY_DEFAULT_CURRENCY = "default_currency"

        private const val DEFAULT_EXPIRY_WARNING_DAYS = 90
        private const val DEFAULT_CURRENCY = "INR"
    }
}
