package com.waterproofing.inventory.data.model

data class VariantWithStock(
    val id: Long,
    val productId: Long,
    val name: String,
    val quantityValue: Double,
    val unit: String,
    val minStockThreshold: Double,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val totalStock: Double
)
