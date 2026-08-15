package com.waterproofing.inventory.data.model

data class ProductWithCategory(
    val id: Long,
    val name: String,
    val categoryId: Long?,
    val categoryName: String?,
    val description: String,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
