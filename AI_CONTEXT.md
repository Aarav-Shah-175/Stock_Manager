# AI Context - Waterproofing Chemical Inventory App

This document acts as the primary handoff context for any AI assistant continuing development on this application.

## Application Purpose
Offline stock-management system for a waterproofing and construction chemical business.

## Core Constraints
- **Offline:** 100% offline. No internet, Cloud DB, Firebase, external APIs or Wi-Fi required.
- **Device:** Single device, single user. No synchronization or authentication needed for V1.
- **Data Preservation:** Products with stock transaction history must not be permanently deleted. Only soft deletes (archiving) are permitted.

## Data Hierarchy
```text
Product
 └── Variant (no SKU)
      └── Batch (auto-generated batch number, expiry, mfg date, shelf life)
           └── Stock Transaction (History)
```
*Note: A single product (e.g. Dr. Fixit Pidicrete URP) can have multiple variants (e.g., "1 kg", "5 kg", "20 kg"). Each variant can have multiple batches, tracked with individual manufacturing/expiry dates and quantities. Batch numbers are auto-generated (format: `B-yyyyMMdd-HHmmss`).*

## Tech Stack
- Kotlin & Jetpack Compose
- Room Database & SQLite **(DB version 4)**
- Navigation Compose (transitions disabled — instant navigation)
- Kotlin Coroutines & Flow (StateFlow)
- WorkManager (expiry notifications, 6-month transaction purge, automatic daily backup)
- MVVM / Clean Architecture
- Material 3 Design
- Gradle Kotlin DSL (`.gradle.kts`)

## Database Schema Notes (Version 4)
- `products` table: **Brand column removed** in migration v2→v3 (`MIGRATION_2_3`).
- `variants` table: **SKU column removed** in migration v1→v2 (`MIGRATION_1_2`).
- `stock_transactions` table: **Transaction Independence** added in migration v3→v4 (`MIGRATION_3_4`). Foreign keys set to `ON DELETE SET NULL` and snapshot columns (`product_name`, `variant_name`, `batch_number`) added so historical transactions survive deletion of products, variants, or batches.
- `batches` table: Supplier/purchase/invoice columns exist in schema for backward compatibility but are not editable in UI.
- Migration classes: `MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4` in `AppDatabase.kt`.

## Key Post-Completion Changes (2026-08-14)
| Change | Files Affected |
|--------|---------------|
| Brand field completely removed | `ProductEntity`, `ProductWithCategory`, `StockTransactionWithDetails`, `LowStockVariant`, `BatchWithProductInfo`, DAOs, `ProductRepository`, `ProductViewModel`, `ProductListScreen`, `ProductDetailScreen`, `AddStockScreen`, `RemoveStockScreen` |
| DB Migration v2→v3 | `AppDatabase.kt` (MIGRATION_2_3 removes `brand` from `products`) |
| Variant unit display | `VariantDetailScreen.kt` (shows unit next to stock & min stock threshold) |
| Transaction History search & date filter | `StockTransactionDao`, `TransactionRepository`, `StockViewModel`, `TransactionHistoryScreen` |
| 6-Month Transaction Cleanup | `TransactionCleanupWorker`, `StockTransactionDao`, `InventoryApplication` |
| Automatic Daily Offline Backup | `AutoBackupWorker`, `BackupManager`, `SettingsRepository`, `SettingsViewModel`, `SettingsScreen`, `InventoryApplication` |
| Add Variant Fix & Data Protection | `ProductDetailScreen.kt` (dialog validation), `AppDatabase.kt` (removed fallbackToDestructiveMigration) |
| Historical Transaction Independence (DB v3→v4) | `StockTransactionEntity`, `StockTransactionDao`, `ProductDao`, `VariantDao`, `BatchDao`, `StockViewModel`, `AppDatabase` (MIGRATION_3_4) |

## How to Build and Run
1. Open the project in Android Studio (Giraffe/Hedgehog or newer).
2. Sync Gradle.
3. Run on a modern emulator or connected device (Android API 26+).
4. Run tests: `./gradlew test`
