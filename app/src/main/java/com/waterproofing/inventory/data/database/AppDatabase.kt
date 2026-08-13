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
    version = 2,
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

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // 1. Create temporary new table without SKU
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `variants_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `product_id` INTEGER NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `quantity_value` REAL NOT NULL, 
                        `unit` TEXT NOT NULL, 
                        `min_stock_threshold` REAL NOT NULL DEFAULT 0.0, 
                        `is_archived` INTEGER NOT NULL DEFAULT 0, 
                        `created_at` INTEGER NOT NULL DEFAULT 0, 
                        `updated_at` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`product_id`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                
                // 2. Copy data from variants to variants_new
                db.execSQL("""
                    INSERT INTO `variants_new` (`id`, `product_id`, `name`, `quantity_value`, `unit`, `min_stock_threshold`, `is_archived`, `created_at`, `updated_at`)
                    SELECT `id`, `product_id`, `name`, `quantity_value`, `unit`, `min_stock_threshold`, `is_archived`, `created_at`, `updated_at`
                    FROM `variants`
                """)
                
                // 3. Drop old variants table
                db.execSQL("DROP TABLE `variants`")
                
                // 4. Rename variants_new to variants
                db.execSQL("ALTER TABLE `variants_new` RENAME TO `variants`")
                
                // 5. Recreate product_id index
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_variants_product_id` ON `variants` (`product_id`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "waterproofing_inventory_db"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
