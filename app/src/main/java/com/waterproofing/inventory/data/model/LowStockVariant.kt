package com.waterproofing.inventory.data.model

data class LowStockVariant(
    val variantId: Long,
    val productId: Long,
    val productName: String,
    val productBrand: String,
    val variantName: String,
    val currentStock: Double,
    val minStockThreshold: Double,
    val unit: String
)
