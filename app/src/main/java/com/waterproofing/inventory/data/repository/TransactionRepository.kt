package com.waterproofing.inventory.data.repository

import com.waterproofing.inventory.data.dao.StockTransactionDao
import com.waterproofing.inventory.data.entity.StockTransactionEntity
import com.waterproofing.inventory.data.model.StockTransactionWithDetails
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: StockTransactionDao) {

    fun getAllTransactions(): Flow<List<StockTransactionWithDetails>> =
        transactionDao.getAllTransactionsWithDetailsFlow()

    fun getTransactionsForProduct(productId: Long): Flow<List<StockTransactionWithDetails>> =
        transactionDao.getTransactionsWithDetailsForProductFlow(productId)

    fun getTransactionsForVariant(variantId: Long): Flow<List<StockTransactionWithDetails>> =
        transactionDao.getTransactionsWithDetailsForVariantFlow(variantId)

    fun getTransactionsForBatch(batchId: Long): Flow<List<StockTransactionWithDetails>> =
        transactionDao.getTransactionsWithDetailsForBatchFlow(batchId)

    fun getRecentTransactions(limit: Int = 20): Flow<List<StockTransactionWithDetails>> =
        transactionDao.getRecentTransactionsWithDetailsFlow(limit)

    fun searchTransactions(
        nameQuery: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): Flow<List<StockTransactionWithDetails>> =
        transactionDao.searchTransactionsFlow(nameQuery, fromTimestamp, toTimestamp)

    suspend fun insert(transaction: StockTransactionEntity): Long =
        transactionDao.insert(transaction)

    /** Delete transactions older than [cutoffMs]. Does NOT touch batch quantities. */
    suspend fun deleteOldTransactions(cutoffMs: Long) {
        transactionDao.deleteTransactionsOlderThan(cutoffMs)
    }
}
