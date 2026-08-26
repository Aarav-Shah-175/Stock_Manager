package com.waterproofing.inventory.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.waterproofing.inventory.data.entity.BatchEntity
import com.waterproofing.inventory.data.model.BatchWithProductInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(batch: BatchEntity): Long

    @Query("DELETE FROM batches WHERE id = :batchId")
    suspend fun deleteBatch(batchId: Long)

    @Update
    suspend fun update(batch: BatchEntity)

    @Query("SELECT * FROM batches WHERE id = :id")
    suspend fun getBatchById(id: Long): BatchEntity?

    @Query("""
        SELECT b.id, b.variant_id as variantId, p.id as productId, p.name as productName, 
               v.name as variantName, v.unit as unit, 
               b.batch_number as batchNumber, b.current_quantity as currentQuantity, 
               b.mfg_date as mfgDate, b.shelf_life_value as shelfLifeValue, 
               b.shelf_life_unit as shelfLifeUnit, b.expiry_date as expiryDate, 
               b.purchase_price as purchasePrice, b.supplier, b.invoice_number as invoiceNumber, 
               b.notes, b.is_depleted as isDepleted, b.created_at as createdAt, b.updated_at as updatedAt
        FROM batches b
        JOIN variants v ON b.variant_id = v.id
        JOIN products p ON v.product_id = p.id
        WHERE b.id = :id
    """)
    fun getBatchWithProductInfoByIdFlow(id: Long): Flow<BatchWithProductInfo?>

    @Query("SELECT * FROM batches WHERE variant_id = :variantId ORDER BY expiry_date ASC")
    fun getBatchesByVariantFlow(variantId: Long): Flow<List<BatchEntity>>

    @Query("""
        SELECT b.id, b.variant_id as variantId, p.id as productId, p.name as productName, 
               v.name as variantName, v.unit as unit, 
               b.batch_number as batchNumber, b.current_quantity as currentQuantity, 
               b.mfg_date as mfgDate, b.shelf_life_value as shelfLifeValue, 
               b.shelf_life_unit as shelfLifeUnit, b.expiry_date as expiryDate, 
               b.purchase_price as purchasePrice, b.supplier, b.invoice_number as invoiceNumber, 
               b.notes, b.is_depleted as isDepleted, b.created_at as createdAt, b.updated_at as updatedAt
        FROM batches b
        JOIN variants v ON b.variant_id = v.id
        JOIN products p ON v.product_id = p.id
        WHERE b.variant_id = :variantId
        ORDER BY b.expiry_date ASC
    """)
    fun getBatchesWithProductInfoByVariantFlow(variantId: Long): Flow<List<BatchWithProductInfo>>

    @Query("""
        SELECT b.id, b.variant_id as variantId, p.id as productId, p.name as productName, 
               v.name as variantName, v.unit as unit, 
               b.batch_number as batchNumber, b.current_quantity as currentQuantity, 
               b.mfg_date as mfgDate, b.shelf_life_value as shelfLifeValue, 
               b.shelf_life_unit as shelfLifeUnit, b.expiry_date as expiryDate, 
               b.purchase_price as purchasePrice, b.supplier, b.invoice_number as invoiceNumber, 
               b.notes, b.is_depleted as isDepleted, b.created_at as createdAt, b.updated_at as updatedAt
        FROM batches b
        JOIN variants v ON b.variant_id = v.id
        JOIN products p ON v.product_id = p.id
        WHERE b.expiry_date < :now AND b.current_quantity > 0 AND p.is_archived = 0 AND v.is_archived = 0
        ORDER BY b.expiry_date ASC
    """)
    fun getExpiredBatchesFlow(now: Long): Flow<List<BatchWithProductInfo>>

    @Query("""
        SELECT b.id, b.variant_id as variantId, p.id as productId, p.name as productName, 
               v.name as variantName, v.unit as unit, 
               b.batch_number as batchNumber, b.current_quantity as currentQuantity, 
               b.mfg_date as mfgDate, b.shelf_life_value as shelfLifeValue, 
               b.shelf_life_unit as shelfLifeUnit, b.expiry_date as expiryDate, 
               b.purchase_price as purchasePrice, b.supplier, b.invoice_number as invoiceNumber, 
               b.notes, b.is_depleted as isDepleted, b.created_at as createdAt, b.updated_at as updatedAt
        FROM batches b
        JOIN variants v ON b.variant_id = v.id
        JOIN products p ON v.product_id = p.id
        WHERE b.expiry_date >= :now AND b.expiry_date <= :threshold AND b.current_quantity > 0 AND p.is_archived = 0 AND v.is_archived = 0
        ORDER BY b.expiry_date ASC
    """)
    fun getExpiringSoonBatchesFlow(now: Long, threshold: Long): Flow<List<BatchWithProductInfo>>

    @Query("""
        SELECT COUNT(*) 
        FROM batches b
        JOIN variants v ON b.variant_id = v.id
        JOIN products p ON v.product_id = p.id
        WHERE b.current_quantity > 0 AND p.is_archived = 0 AND v.is_archived = 0
    """)
    fun getActiveBatchCountFlow(): Flow<Int>

    @Query("""
        SELECT COUNT(*) 
        FROM batches b
        JOIN variants v ON b.variant_id = v.id
        JOIN products p ON v.product_id = p.id
        WHERE b.expiry_date < :now AND b.current_quantity > 0 AND p.is_archived = 0 AND v.is_archived = 0
    """)
    fun getExpiredBatchCountFlow(now: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*) 
        FROM batches b
        JOIN variants v ON b.variant_id = v.id
        JOIN products p ON v.product_id = p.id
        WHERE b.expiry_date >= :now AND b.expiry_date <= :threshold AND b.current_quantity > 0 AND p.is_archived = 0 AND v.is_archived = 0
    """)
    fun getExpiringSoonBatchCountFlow(now: Long, threshold: Long): Flow<Int>

    /** One-shot (non-Flow) query used by the background notification worker. */
    @Query("""
        SELECT b.id, b.variant_id as variantId, p.id as productId, p.name as productName, 
               v.name as variantName, v.unit as unit, 
               b.batch_number as batchNumber, b.current_quantity as currentQuantity, 
               b.mfg_date as mfgDate, b.shelf_life_value as shelfLifeValue, 
               b.shelf_life_unit as shelfLifeUnit, b.expiry_date as expiryDate, 
               b.purchase_price as purchasePrice, b.supplier, b.invoice_number as invoiceNumber, 
               b.notes, b.is_depleted as isDepleted, b.created_at as createdAt, b.updated_at as updatedAt
        FROM batches b
        JOIN variants v ON b.variant_id = v.id
        JOIN products p ON v.product_id = p.id
        WHERE b.expiry_date >= :now AND b.expiry_date <= :threshold AND b.current_quantity > 0 AND p.is_archived = 0 AND v.is_archived = 0
        ORDER BY b.expiry_date ASC
    """)
    suspend fun getExpiringSoonBatchesOnce(now: Long, threshold: Long): List<BatchWithProductInfo>
}
