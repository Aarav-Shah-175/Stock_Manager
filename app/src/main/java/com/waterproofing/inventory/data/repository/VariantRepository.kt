package com.waterproofing.inventory.data.repository

import com.waterproofing.inventory.data.dao.VariantDao
import com.waterproofing.inventory.data.entity.VariantEntity
import com.waterproofing.inventory.data.model.LowStockVariant
import com.waterproofing.inventory.data.model.VariantWithStock
import kotlinx.coroutines.flow.Flow

class VariantRepository(private val variantDao: VariantDao) {
    val lowStockVariants: Flow<List<LowStockVariant>> = variantDao.getLowStockVariantsFlow()
    val activeVariantCount: Flow<Int> = variantDao.getActiveVariantCountFlow()

    fun getActiveVariantsForProduct(productId: Long): Flow<List<VariantWithStock>> {
        return variantDao.getActiveVariantsByProductFlow(productId)
    }

    fun getVariantWithStock(variantId: Long): Flow<VariantWithStock?> {
        return variantDao.getVariantWithStockFlow(variantId)
    }

    suspend fun getVariantById(id: Long): VariantEntity? {
        return variantDao.getVariantById(id)
    }

    suspend fun insert(
        productId: Long,
        name: String,
        quantityValue: Double,
        unit: String,
        minStockThreshold: Double
    ): Long {
        return variantDao.insert(
            VariantEntity(
                productId = productId,
                name = name.trim(),
                quantityValue = quantityValue,
                unit = unit.trim(),
                minStockThreshold = minStockThreshold
            )
        )
    }

    suspend fun update(variant: VariantEntity) {
        variantDao.update(
            variant.copy(
                name = variant.name.trim(),
                unit = variant.unit.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun archiveVariant(variantId: Long) {
        variantDao.updateArchiveStatus(variantId, isArchived = true)
    }

    suspend fun restoreVariant(variantId: Long) {
        variantDao.updateArchiveStatus(variantId, isArchived = false)
    }

    suspend fun deleteVariant(variantId: Long) {
        variantDao.deleteVariant(variantId)
    }
}
