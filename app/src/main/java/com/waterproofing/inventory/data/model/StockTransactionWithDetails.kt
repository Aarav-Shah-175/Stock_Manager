package com.waterproofing.inventory.data.model

data class StockTransactionWithDetails(
    val id: Long,
    val batchId: Long,
    val productId: Long,
    val variantId: Long,
    val productName: String,
    val productBrand: String,
    val variantName: String,
    val batchNumber: String,
    val transactionType: String,
    val quantity: Double,
    val unit: String,
    val timestamp: Long,
    val reason: String,
    val customerProject: String?,
    val invoiceNumber: String?,
    val notes: String?
)
