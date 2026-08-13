package com.waterproofing.inventory.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "batches",
    foreignKeys = [
        ForeignKey(
            entity = VariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["variant_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["variant_id"]),
        Index(value = ["batch_number"]),
        Index(value = ["expiry_date"])
    ]
)
data class BatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "variant_id")
    val variantId: Long,
    @ColumnInfo(name = "batch_number")
    val batchNumber: String,
    @ColumnInfo(name = "current_quantity")
    val currentQuantity: Double,
    @ColumnInfo(name = "mfg_date")
    val mfgDate: Long? = null,
    @ColumnInfo(name = "shelf_life_value")
    val shelfLifeValue: Int? = null,
    @ColumnInfo(name = "shelf_life_unit")
    val shelfLifeUnit: String? = null,
    @ColumnInfo(name = "expiry_date")
    val expiryDate: Long,
    @ColumnInfo(name = "purchase_price")
    val purchasePrice: Double? = null,
    val supplier: String? = null,
    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "is_depleted")
    val isDepleted: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
