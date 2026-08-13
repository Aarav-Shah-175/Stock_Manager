package com.waterproofing.inventory.data.repository

import com.waterproofing.inventory.data.dao.CategoryDao
import com.waterproofing.inventory.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()

    suspend fun insert(name: String): Long {
        return categoryDao.insert(CategoryEntity(name = name.trim()))
    }

    suspend fun update(category: CategoryEntity) {
        categoryDao.update(category.copy(
            name = category.name.trim(),
            updatedAt = System.currentTimeMillis()
        ))
    }

    suspend fun delete(category: CategoryEntity) {
        categoryDao.delete(category)
    }
}
