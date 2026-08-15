package com.waterproofing.inventory.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.waterproofing.inventory.data.entity.StockTransactionEntity
import com.waterproofing.inventory.data.model.StockTransactionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: StockTransactionEntity): Long

    @Query("""
        SELECT t.id, t.batch_id as batchId, t.product_id as productId, t.variant_id as variantId, 
               p.name as productName, v.name as variantName, 
               b.batch_number as batchNumber, t.transaction_type as transactionType, 
               t.quantity, t.unit, t.timestamp, t.reason, t.customer_project as customerProject, 
               t.invoice_number as invoiceNumber, t.notes
        FROM stock_transactions t
        JOIN batches b ON t.batch_id = b.id
        JOIN variants v ON t.variant_id = v.id
        JOIN products p ON t.product_id = p.id
        ORDER BY t.timestamp DESC, t.id DESC
    """)
    fun getAllTransactionsWithDetailsFlow(): Flow<List<StockTransactionWithDetails>>

    @Query("""
        SELECT t.id, t.batch_id as batchId, t.product_id as productId, t.variant_id as variantId, 
               p.name as productName, v.name as variantName, 
               b.batch_number as batchNumber, t.transaction_type as transactionType, 
               t.quantity, t.unit, t.timestamp, t.reason, t.customer_project as customerProject, 
               t.invoice_number as invoiceNumber, t.notes
        FROM stock_transactions t
        JOIN batches b ON t.batch_id = b.id
        JOIN variants v ON t.variant_id = v.id
        JOIN products p ON t.product_id = p.id
        WHERE t.product_id = :productId
        ORDER BY t.timestamp DESC, t.id DESC
    """)
    fun getTransactionsWithDetailsForProductFlow(productId: Long): Flow<List<StockTransactionWithDetails>>

    @Query("""
        SELECT t.id, t.batch_id as batchId, t.product_id as productId, t.variant_id as variantId, 
               p.name as productName, v.name as variantName, 
               b.batch_number as batchNumber, t.transaction_type as transactionType, 
               t.quantity, t.unit, t.timestamp, t.reason, t.customer_project as customerProject, 
               t.invoice_number as invoiceNumber, t.notes
        FROM stock_transactions t
        JOIN batches b ON t.batch_id = b.id
        JOIN variants v ON t.variant_id = v.id
        JOIN products p ON t.product_id = p.id
        WHERE t.variant_id = :variantId
        ORDER BY t.timestamp DESC, t.id DESC
    """)
    fun getTransactionsWithDetailsForVariantFlow(variantId: Long): Flow<List<StockTransactionWithDetails>>

    @Query("""
        SELECT t.id, t.batch_id as batchId, t.product_id as productId, t.variant_id as variantId, 
               p.name as productName, v.name as variantName, 
               b.batch_number as batchNumber, t.transaction_type as transactionType, 
               t.quantity, t.unit, t.timestamp, t.reason, t.customer_project as customerProject, 
               t.invoice_number as invoiceNumber, t.notes
        FROM stock_transactions t
        JOIN batches b ON t.batch_id = b.id
        JOIN variants v ON t.variant_id = v.id
        JOIN products p ON t.product_id = p.id
        WHERE t.batch_id = :batchId
        ORDER BY t.timestamp DESC, t.id DESC
    """)
    fun getTransactionsWithDetailsForBatchFlow(batchId: Long): Flow<List<StockTransactionWithDetails>>

    @Query("""
        SELECT t.id, t.batch_id as batchId, t.product_id as productId, t.variant_id as variantId, 
               p.name as productName, v.name as variantName, 
               b.batch_number as batchNumber, t.transaction_type as transactionType, 
               t.quantity, t.unit, t.timestamp, t.reason, t.customer_project as customerProject, 
               t.invoice_number as invoiceNumber, t.notes
        FROM stock_transactions t
        JOIN batches b ON t.batch_id = b.id
        JOIN variants v ON t.variant_id = v.id
        JOIN products p ON t.product_id = p.id
        ORDER BY t.timestamp DESC, t.id DESC
        LIMIT :limit
    """)
    fun getRecentTransactionsWithDetailsFlow(limit: Int): Flow<List<StockTransactionWithDetails>>

    /**
     * Search transactions by product/variant name and optional date range.
     * Pass nameQuery as "%" to match all names.
     * Pass fromTimestamp=0 and toTimestamp=Long.MAX_VALUE to skip date filter.
     * Excludes ADJUSTMENT records.
     */
    @Query("""
        SELECT t.id, t.batch_id as batchId, t.product_id as productId, t.variant_id as variantId, 
               p.name as productName, v.name as variantName, 
               b.batch_number as batchNumber, t.transaction_type as transactionType, 
               t.quantity, t.unit, t.timestamp, t.reason, t.customer_project as customerProject, 
               t.invoice_number as invoiceNumber, t.notes
        FROM stock_transactions t
        JOIN batches b ON t.batch_id = b.id
        JOIN variants v ON t.variant_id = v.id
        JOIN products p ON t.product_id = p.id
        WHERE t.transaction_type != 'ADJUSTMENT'
          AND (p.name LIKE :nameQuery OR v.name LIKE :nameQuery)
          AND t.timestamp >= :fromTimestamp
          AND t.timestamp <= :toTimestamp
        ORDER BY t.timestamp DESC, t.id DESC
    """)
    fun searchTransactionsFlow(
        nameQuery: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): Flow<List<StockTransactionWithDetails>>

    /** Delete all transactions older than the given timestamp (used for 6-month retention). */
    @Query("DELETE FROM stock_transactions WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteTransactionsOlderThan(cutoffTimestamp: Long)
}
