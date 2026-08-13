package com.waterproofing.inventory

import android.app.Application
import com.waterproofing.inventory.data.database.AppDatabase
import com.waterproofing.inventory.data.repository.BatchRepository
import com.waterproofing.inventory.data.repository.CategoryRepository
import com.waterproofing.inventory.data.repository.ProductRepository
import com.waterproofing.inventory.data.repository.TransactionRepository
import com.waterproofing.inventory.data.repository.VariantRepository

class InventoryApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val productRepository by lazy { ProductRepository(database.productDao()) }
    val variantRepository by lazy { VariantRepository(database.variantDao()) }
    val batchRepository by lazy { BatchRepository(database.batchDao()) }
    val transactionRepository by lazy { TransactionRepository(database.stockTransactionDao()) }
}
