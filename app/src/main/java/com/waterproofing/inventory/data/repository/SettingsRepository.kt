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

    // --- Automatic Backup Settings ---
    fun getAutoBackupEnabledFlow(): Flow<Boolean> {
        return appSettingsDao.getSettingValueFlow(KEY_AUTO_BACKUP_ENABLED)
            .map { it?.toBooleanStrictOrNull() ?: true }
    }

    suspend fun getAutoBackupEnabled(): Boolean {
        return appSettingsDao.getSettingValueDirect(KEY_AUTO_BACKUP_ENABLED)?.toBooleanStrictOrNull() ?: true
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        appSettingsDao.setSetting(AppSettingsEntity(KEY_AUTO_BACKUP_ENABLED, enabled.toString()))
    }

    fun getAutoBackupTimeFlow(): Flow<String> {
        return appSettingsDao.getSettingValueFlow(KEY_AUTO_BACKUP_TIME)
            .map { it ?: "23:30" }
    }

    suspend fun setAutoBackupTime(timeStr: String) {
        appSettingsDao.setSetting(AppSettingsEntity(KEY_AUTO_BACKUP_TIME, timeStr))
    }

    fun getAutoBackupKeepCountFlow(): Flow<Int> {
        return appSettingsDao.getSettingValueFlow(KEY_AUTO_BACKUP_KEEP_COUNT)
            .map { it?.toIntOrNull() ?: 7 }
    }

    suspend fun getAutoBackupKeepCount(): Int {
        return appSettingsDao.getSettingValueDirect(KEY_AUTO_BACKUP_KEEP_COUNT)?.toIntOrNull() ?: 7
    }

    suspend fun setAutoBackupKeepCount(count: Int) {
        appSettingsDao.setSetting(AppSettingsEntity(KEY_AUTO_BACKUP_KEEP_COUNT, count.toString()))
    }

    fun getLastSuccessfulBackupFlow(): Flow<Long?> {
        return appSettingsDao.getSettingValueFlow(KEY_LAST_SUCCESSFUL_BACKUP)
            .map { it?.toLongOrNull() }
    }

    suspend fun setLastSuccessfulBackup(timestamp: Long) {
        appSettingsDao.setSetting(AppSettingsEntity(KEY_LAST_SUCCESSFUL_BACKUP, timestamp.toString()))
    }

    fun getLastBackupStatusFlow(): Flow<String?> {
        return appSettingsDao.getSettingValueFlow(KEY_LAST_BACKUP_STATUS)
    }

    suspend fun setLastBackupStatus(status: String) {
        appSettingsDao.setSetting(AppSettingsEntity(KEY_LAST_BACKUP_STATUS, status))
    }

    fun getAutoBackupFolderUriFlow(): Flow<String?> {
        return appSettingsDao.getSettingValueFlow(KEY_AUTO_BACKUP_FOLDER_URI)
    }

    suspend fun getAutoBackupFolderUri(): String? {
        return appSettingsDao.getSettingValueDirect(KEY_AUTO_BACKUP_FOLDER_URI)
    }

    suspend fun setAutoBackupFolderUri(uriStr: String?) {
        if (uriStr.isNullOrBlank()) {
            appSettingsDao.deleteSetting(KEY_AUTO_BACKUP_FOLDER_URI)
        } else {
            appSettingsDao.setSetting(AppSettingsEntity(KEY_AUTO_BACKUP_FOLDER_URI, uriStr))
        }
    }

    companion object {
        private const val KEY_EXPIRY_WARNING_DAYS = "expiry_warning_days"
        private const val KEY_COMPANY_NAME = "company_name"
        private const val KEY_DEFAULT_CURRENCY = "default_currency"

        private const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
        private const val KEY_AUTO_BACKUP_TIME = "auto_backup_time"
        private const val KEY_AUTO_BACKUP_KEEP_COUNT = "auto_backup_keep_count"
        private const val KEY_LAST_SUCCESSFUL_BACKUP = "last_successful_backup"
        private const val KEY_LAST_BACKUP_STATUS = "last_backup_status"
        private const val KEY_AUTO_BACKUP_FOLDER_URI = "auto_backup_folder_uri"

        private const val DEFAULT_EXPIRY_WARNING_DAYS = 90
        private const val DEFAULT_CURRENCY = "INR"
    }
}
