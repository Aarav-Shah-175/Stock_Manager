package com.waterproofing.inventory.data.repository

import com.waterproofing.inventory.data.dao.BatchDao
import com.waterproofing.inventory.data.entity.BatchEntity
import com.waterproofing.inventory.data.model.BatchWithProductInfo
import kotlinx.coroutines.flow.Flow

class BatchRepository(private val batchDao: BatchDao) {

    fun getBatchesByVariant(variantId: Long): Flow<List<BatchEntity>> =
        batchDao.getBatchesByVariantFlow(variantId)

    fun getBatchesWithProductInfoByVariant(variantId: Long): Flow<List<BatchWithProductInfo>> =
        batchDao.getBatchesWithProductInfoByVariantFlow(variantId)

    fun getBatchWithProductInfoById(batchId: Long): Flow<BatchWithProductInfo?> =
        batchDao.getBatchWithProductInfoByIdFlow(batchId)

    fun getExpiredBatches(now: Long = System.currentTimeMillis()): Flow<List<BatchWithProductInfo>> =
        batchDao.getExpiredBatchesFlow(now)

    fun getExpiringSoonBatches(warningMs: Long, now: Long = System.currentTimeMillis()): Flow<List<BatchWithProductInfo>> =
        batchDao.getExpiringSoonBatchesFlow(now, now + warningMs)

    fun getActiveBatchCount(): Flow<Int> = batchDao.getActiveBatchCountFlow()

    fun getExpiredBatchCount(now: Long = System.currentTimeMillis()): Flow<Int> =
        batchDao.getExpiredBatchCountFlow(now)

    fun getExpiringSoonBatchCount(warningMs: Long, now: Long = System.currentTimeMillis()): Flow<Int> =
        batchDao.getExpiringSoonBatchCountFlow(now, now + warningMs)

    suspend fun getBatchById(id: Long): BatchEntity? = batchDao.getBatchById(id)

    suspend fun insertBatch(batch: BatchEntity): Long = batchDao.insert(batch)

    suspend fun updateBatch(batch: BatchEntity) = batchDao.update(batch.copy(
        updatedAt = System.currentTimeMillis()
    ))
}
