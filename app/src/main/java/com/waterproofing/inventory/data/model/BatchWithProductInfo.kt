package com.waterproofing.inventory.data.model

data class BatchWithProductInfo(
    val id: Long,
    val variantId: Long,
    val productId: Long,
    val productName: String,
    val productBrand: String,
    val variantName: String,
    val unit: String,
    val batchNumber: String,
    val currentQuantity: Double,
    val mfgDate: Long?,
    val shelfLifeValue: Int?,
    val shelfLifeUnit: String?,
    val expiryDate: Long,
    val purchasePrice: Double?,
    val supplier: String?,
    val invoiceNumber: String?,
    val notes: String?,
    val isDepleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
