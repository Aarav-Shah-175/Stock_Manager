package com.waterproofing.inventory.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.waterproofing.inventory.data.entity.ProductEntity
import com.waterproofing.inventory.data.model.ProductWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Query("""
        SELECT p.id, p.name, p.brand, p.category_id as categoryId, c.name as categoryName, 
               p.description, p.is_archived as isArchived, p.created_at as createdAt, p.updated_at as updatedAt 
        FROM products p 
        LEFT JOIN categories c ON p.category_id = c.id 
        WHERE p.is_archived = 0 
        ORDER BY p.name ASC
    """)
    fun getActiveProductsWithCategoryFlow(): Flow<List<ProductWithCategory>>

    @Query("""
        SELECT p.id, p.name, p.brand, p.category_id as categoryId, c.name as categoryName, 
               p.description, p.is_archived as isArchived, p.created_at as createdAt, p.updated_at as updatedAt 
        FROM products p 
        LEFT JOIN categories c ON p.category_id = c.id 
        WHERE p.is_archived = 1 
        ORDER BY p.name ASC
    """)
    fun getArchivedProductsWithCategoryFlow(): Flow<List<ProductWithCategory>>

    @Query("""
        SELECT p.id, p.name, p.brand, p.category_id as categoryId, c.name as categoryName, 
               p.description, p.is_archived as isArchived, p.created_at as createdAt, p.updated_at as updatedAt 
        FROM products p 
        LEFT JOIN categories c ON p.category_id = c.id 
        WHERE p.id = :productId
    """)
    fun getProductWithCategoryFlow(productId: Long): Flow<ProductWithCategory?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("UPDATE products SET is_archived = :isArchived, updated_at = :timestamp WHERE id = :productId")
    suspend fun updateArchiveStatus(productId: Long, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM products WHERE is_archived = 0")
    fun getActiveProductCountFlow(): Flow<Int>

    @Query("""
        SELECT DISTINCT p.id, p.name, p.brand, p.category_id as categoryId, c.name as categoryName, 
               p.description, p.is_archived as isArchived, p.created_at as createdAt, p.updated_at as updatedAt 
        FROM products p 
        LEFT JOIN categories c ON p.category_id = c.id 
        LEFT JOIN variants v ON v.product_id = p.id AND v.is_archived = 0
        LEFT JOIN batches b ON b.variant_id = v.id
        WHERE p.is_archived = 0 
          AND (p.name LIKE :query 
               OR p.brand LIKE :query 
               OR p.description LIKE :query
               OR c.name LIKE :query 
               OR v.name LIKE :query 
               OR v.sku LIKE :query 
               OR b.batch_number LIKE :query)
        ORDER BY p.name ASC
    """)
    fun searchActiveProductsWithCategoryFlow(query: String): Flow<List<ProductWithCategory>>
}
