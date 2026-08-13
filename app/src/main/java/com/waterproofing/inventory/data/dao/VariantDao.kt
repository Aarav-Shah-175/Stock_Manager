package com.waterproofing.inventory.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.waterproofing.inventory.data.entity.VariantEntity
import com.waterproofing.inventory.data.model.LowStockVariant
import com.waterproofing.inventory.data.model.VariantWithStock
import kotlinx.coroutines.flow.Flow

@Dao
interface VariantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(variant: VariantEntity): Long

    @Update
    suspend fun update(variant: VariantEntity)

    @Query("""
        SELECT v.id, v.product_id as productId, v.name, v.quantity_value as quantityValue, v.unit, 
               v.sku, v.min_stock_threshold as minStockThreshold, v.is_archived as isArchived, 
               v.created_at as createdAt, v.updated_at as updatedAt, 
               COALESCE(SUM(b.current_quantity), 0.0) as totalStock 
        FROM variants v 
        LEFT JOIN batches b ON v.id = b.variant_id
        WHERE v.product_id = :productId AND v.is_archived = 0 
        GROUP BY v.id 
        ORDER BY v.name ASC
    """)
    fun getActiveVariantsByProductFlow(productId: Long): Flow<List<VariantWithStock>>

    @Query("""
        SELECT v.id, v.product_id as productId, v.name, v.quantity_value as quantityValue, v.unit, 
               v.sku, v.min_stock_threshold as minStockThreshold, v.is_archived as isArchived, 
               v.created_at as createdAt, v.updated_at as updatedAt, 
               COALESCE(SUM(b.current_quantity), 0.0) as totalStock 
        FROM variants v 
        LEFT JOIN batches b ON v.id = b.variant_id
        WHERE v.id = :variantId
    """)
    fun getVariantWithStockFlow(variantId: Long): Flow<VariantWithStock?>

    @Query("SELECT * FROM variants WHERE id = :id")
    suspend fun getVariantById(id: Long): VariantEntity?

    @Query("UPDATE variants SET is_archived = :isArchived, updated_at = :timestamp WHERE id = :variantId")
    suspend fun updateArchiveStatus(variantId: Long, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("""
        SELECT v.id as variantId, p.id as productId, p.name as productName, p.brand as productBrand, 
               v.name as variantName, COALESCE(SUM(b.current_quantity), 0.0) as currentStock, 
               v.min_stock_threshold as minStockThreshold, v.unit as unit
        FROM variants v
        JOIN products p ON v.product_id = p.id
        LEFT JOIN batches b ON v.id = b.variant_id
        WHERE v.is_archived = 0 AND p.is_archived = 0
        GROUP BY v.id
        HAVING currentStock < v.min_stock_threshold
        ORDER BY p.name ASC, v.name ASC
    """)
    fun getLowStockVariantsFlow(): Flow<List<LowStockVariant>>

    @Query("SELECT COUNT(*) FROM variants WHERE is_archived = 0")
    fun getActiveVariantCountFlow(): Flow<Int>
}
