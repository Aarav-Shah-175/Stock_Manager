package com.waterproofing.inventory.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_transactions",
    foreignKeys = [
        ForeignKey(
            entity = BatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["batch_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = VariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["variant_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["batch_id"]),
        Index(value = ["product_id"]),
        Index(value = ["variant_id"]),
        Index(value = ["timestamp"])
    ]
)
data class StockTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "batch_id")
    val batchId: Long,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    @ColumnInfo(name = "variant_id")
    val variantId: Long,
    @ColumnInfo(name = "transaction_type")
    val transactionType: String, // "IN", "OUT", "ADJUSTMENT"
    val quantity: Double,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String, // e.g. "Received", "Sale", "Used", "Damaged", "Expired", "Adjustment"
    @ColumnInfo(name = "customer_project")
    val customerProject: String? = null,
    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
