package com.waterproofing.inventory

import android.app.Application
import com.waterproofing.inventory.data.database.AppDatabase
import com.waterproofing.inventory.data.repository.BatchRepository
import com.waterproofing.inventory.data.repository.CategoryRepository
import com.waterproofing.inventory.data.repository.ProductRepository
import com.waterproofing.inventory.data.repository.SettingsRepository
import com.waterproofing.inventory.data.repository.TransactionRepository
import com.waterproofing.inventory.data.repository.VariantRepository
import com.waterproofing.inventory.domain.AutoBackupWorker
import com.waterproofing.inventory.domain.ExpiryNotificationWorker
import com.waterproofing.inventory.domain.TransactionCleanupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InventoryApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val productRepository by lazy { ProductRepository(database.productDao()) }
    val variantRepository by lazy { VariantRepository(database.variantDao()) }
    val batchRepository by lazy { BatchRepository(database.batchDao()) }
    val transactionRepository by lazy { TransactionRepository(database.stockTransactionDao()) }
    val settingsRepository by lazy { SettingsRepository(database.appSettingsDao()) }

    override fun onCreate() {
        super.onCreate()
        // 1. Notification Channels
        ExpiryNotificationWorker.createNotificationChannel(this)
        AutoBackupWorker.createNotificationChannel(this)

        // 2. Schedule Periodic WorkManager Tasks
        ExpiryNotificationWorker.schedule(this)
        TransactionCleanupWorker.schedule(this)
        AutoBackupWorker.schedule(this)

        // 3. Perform immediate cleanup check on app startup
        CoroutineScope(Dispatchers.IO).launch {
            try {
                TransactionCleanupWorker.performCleanup(this@InventoryApplication)
            } catch (_: Exception) {}
        }
    }
}
