package com.waterproofing.inventory.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.waterproofing.inventory.data.dao.AppSettingsDao
import com.waterproofing.inventory.data.dao.BatchDao
import com.waterproofing.inventory.data.dao.CategoryDao
import com.waterproofing.inventory.data.dao.ProductDao
import com.waterproofing.inventory.data.dao.StockTransactionDao
import com.waterproofing.inventory.data.dao.VariantDao
import com.waterproofing.inventory.data.entity.AppSettingsEntity
import com.waterproofing.inventory.data.entity.BatchEntity
import com.waterproofing.inventory.data.entity.CategoryEntity
import com.waterproofing.inventory.data.entity.ProductEntity
import com.waterproofing.inventory.data.entity.StockTransactionEntity
import com.waterproofing.inventory.data.entity.VariantEntity

@Database(
    entities = [
        CategoryEntity::class,
        ProductEntity::class,
        VariantEntity::class,
        BatchEntity::class,
        StockTransactionEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun variantDao(): VariantDao
    abstract fun batchDao(): BatchDao
    abstract fun stockTransactionDao(): StockTransactionDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "waterproofing_inventory_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
