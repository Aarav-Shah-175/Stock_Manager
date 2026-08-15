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
    version = 3,
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

        /** v1 → v2: Remove SKU column from variants (temp-table strategy). */
        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
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
                db.execSQL("""
                    INSERT INTO `variants_new` (`id`, `product_id`, `name`, `quantity_value`, `unit`, `min_stock_threshold`, `is_archived`, `created_at`, `updated_at`)
                    SELECT `id`, `product_id`, `name`, `quantity_value`, `unit`, `min_stock_threshold`, `is_archived`, `created_at`, `updated_at`
                    FROM `variants`
                """)
                db.execSQL("DROP TABLE `variants`")
                db.execSQL("ALTER TABLE `variants_new` RENAME TO `variants`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_variants_product_id` ON `variants` (`product_id`)")
            }
        }

        /** v2 → v3: Remove `brand` column from products (temp-table strategy). */
        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // 1. Create new products table without brand
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `products_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `category_id` INTEGER,
                        `description` TEXT NOT NULL DEFAULT '',
                        `is_archived` INTEGER NOT NULL DEFAULT 0,
                        `created_at` INTEGER NOT NULL DEFAULT 0,
                        `updated_at` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """)
                // 2. Copy all existing rows, dropping brand data
                db.execSQL("""
                    INSERT INTO `products_new` (`id`, `name`, `category_id`, `description`, `is_archived`, `created_at`, `updated_at`)
                    SELECT `id`, `name`, `category_id`, `description`, `is_archived`, `created_at`, `updated_at`
                    FROM `products`
                """)
                // 3. Drop old table
                db.execSQL("DROP TABLE `products`")
                // 4. Rename
                db.execSQL("ALTER TABLE `products_new` RENAME TO `products`")
                // 5. Recreate index
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_category_id` ON `products` (`category_id`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "waterproofing_inventory_db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
