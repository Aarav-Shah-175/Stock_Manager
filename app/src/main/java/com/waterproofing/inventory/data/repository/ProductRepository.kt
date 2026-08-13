package com.waterproofing.inventory.data.repository

import com.waterproofing.inventory.data.dao.ProductDao
import com.waterproofing.inventory.data.entity.ProductEntity
import com.waterproofing.inventory.data.model.ProductWithCategory
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {
    val activeProducts: Flow<List<ProductWithCategory>> = productDao.getActiveProductsWithCategoryFlow()
    val archivedProducts: Flow<List<ProductWithCategory>> = productDao.getArchivedProductsWithCategoryFlow()
    val activeProductCount: Flow<Int> = productDao.getActiveProductCountFlow()

    fun getProductWithCategoryFlow(productId: Long): Flow<ProductWithCategory?> {
        return productDao.getProductWithCategoryFlow(productId)
    }

    suspend fun getProductById(productId: Long): ProductEntity? {
        return productDao.getProductById(productId)
    }

    suspend fun insert(name: String, brand: String, categoryId: Long?, description: String): Long {
        return productDao.insert(
            ProductEntity(
                name = name.trim(),
                brand = brand.trim(),
                categoryId = categoryId,
                description = description.trim()
            )
        )
    }

    suspend fun update(product: ProductEntity) {
        productDao.update(
            product.copy(
                name = product.name.trim(),
                brand = product.brand.trim(),
                description = product.description.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun archiveProduct(productId: Long) {
        productDao.updateArchiveStatus(productId, isArchived = true)
    }

    suspend fun restoreProduct(productId: Long) {
        productDao.updateArchiveStatus(productId, isArchived = false)
    }

    fun searchProducts(query: String): Flow<List<ProductWithCategory>> {
        val formattedQuery = "%$query%"
        return productDao.searchActiveProductsWithCategoryFlow(formattedQuery)
    }
}
