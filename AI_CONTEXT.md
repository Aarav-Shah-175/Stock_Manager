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
- Room Database & SQLite **(DB version 2)**
- Navigation Compose (transitions disabled — instant navigation)
- Kotlin Coroutines & Flow (StateFlow)
- WorkManager (expiry notifications, daily background check)
- MVVM / Clean Architecture
- Material 3 Design
- Gradle Kotlin DSL (`.gradle.kts`)

## Database Schema Notes (Version 2)
- `variants` table: **SKU column removed** in migration v1→v2. Migration uses temp-table copy strategy.
- `batches` table: unchanged at v2. Supplier/purchase/invoice columns exist in schema but are no longer editable via UI.
- Migration class: `MIGRATION_1_2` in `AppDatabase.kt`.

## Key Post-Completion Changes (2026-08-13)
| Change | Files Affected |
|--------|---------------|
| SKU removed from variants | `VariantEntity`, `VariantWithStock`, `VariantDao`, `ProductDao`, `VariantRepository`, `ProductDetailViewModel`, `ProductDetailScreen`, `VariantDetailScreen`, `ProductListScreen` |
| Batch number auto-generated | `VariantDetailScreen` (BatchAddEditDialog) |
| Supplier/Purchasing UI removed | `VariantDetailScreen` (BatchAddEditDialog) |
| Searchable product dropdown | `AddStockScreen`, `RemoveStockScreen` |
| No page transition animations | `AppNavigation.kt` |
| Backup prefix changed | `BackupManager.kt` → `Inventory_Backup` |
| Adjustment hidden from history | `TransactionHistoryScreen` |
| Expiry notifications | `ExpiryNotificationWorker`, `InventoryApplication`, `MainActivity`, `AndroidManifest.xml`, `BatchDao` |

## How to Build and Run
1. Open the project in Android Studio (Giraffe/Hedgehog or newer).
2. Sync Gradle.
3. Run on a modern emulator or connected device (Android API 26+).
4. Run tests: `./gradlew test`
