# Development Status - Waterproofing Chemical Inventory App

This document details the current state of implementation across the planned development phases.

## Status Summary

```text
Phase 1 — Foundation          COMPLETE
Phase 2 — Products & Variants COMPLETE
Phase 3 — Batches & Expiry    COMPLETE
Phase 4 — Stock & History     COMPLETE
Phase 5 — Dashboard & Search  COMPLETE
Phase 6 — Backup & Restore    COMPLETE
Phase 7 — Testing & Polish    COMPLETE
Post-Completion Changes       COMPLETE (2026-08-13)
```

## Post-Completion Changes (2026-08-13)

### 1. SKU Removed from Variants (DB Migration v1 → v2)
- Removed `sku` column from `variants` table via `MIGRATION_1_2` in `AppDatabase`.
- DB version bumped from 1 → 2. Migration uses temp-table copy strategy to preserve all existing data.
- Removed from: `VariantEntity`, `VariantWithStock`, `VariantDao`, `ProductDao` (search), `VariantRepository`, `ProductDetailViewModel`, `ProductDetailScreen` (`VariantAddEditDialog` + card display), `VariantDetailScreen` (banner display), `ProductListScreen` (search placeholder).

### 2. Batch Number Auto-Generated (Add Stock Batch)
- Removed manual Batch Number input from `BatchAddEditDialog` in `VariantDetailScreen`.
- Auto-generates batch number format: `B-yyyyMMdd-HHmmss` using `SimpleDateFormat`.
- Internal batch tracking, FEFO, and all history remain intact.

### 3. Supplier & Purchasing Section Removed (Add Stock Batch)
- Removed Supplier, Purchase Price, and Invoice Number input fields from `BatchAddEditDialog`.
- Fields are preserved in the database schema and entity for backward compatibility with existing data.
- Existing batch records retain their supplier/purchase data; they are simply no longer editable from the UI.

### 4. Searchable Product Selection (Stock IN & Stock OUT)
- `AddStockScreen`: Product dropdown replaced with a live-filter autocomplete text field.
- `RemoveStockScreen`: Same searchable dropdown added.
- Filtering is in-memory and fully offline; matches product `name` and `brand`.
- Clearing the field resets the product/variant/batch selection cascade.

### 5. Page Transitions Disabled
- Added `EnterTransition.None` / `ExitTransition.None` to NavHost in `AppNavigation.kt`.
- All routes now transition instantly with no animation delay.

### 6. Backup Filename Renamed
- `BackupManager.BACKUP_PREFIX` changed from `WaterproofInventory_Backup` → `Inventory_Backup`.
- Files now named: `Inventory_Backup_yyyyMMdd_HHmmss.db`.

### 7. Adjustment Hidden from Transaction History
- `TransactionHistoryScreen` tabs reduced to: ALL / IN / OUT.
- "ALL" filter now explicitly excludes ADJUSTMENT records from display.
- Existing ADJUSTMENT records in the DB are untouched; they are simply not shown.

### 8. Expiry-Soon Notifications (WorkManager)
- Added `androidx.work:work-runtime-ktx:2.9.0` dependency.
- Created `ExpiryNotificationWorker` (CoroutineWorker) — runs once per day via WorkManager.
- Queries batches expiring within 30 days; sends a single summary notification (NOTIFICATION_ID=1001).
- Notification channel `inventory_expiry_alerts` created in `InventoryApplication.onCreate`.
- Worker scheduled in `InventoryApplication.onCreate` with `ExistingPeriodicWorkPolicy.KEEP` (safe to restart app).
- `POST_NOTIFICATIONS` permission added to `AndroidManifest.xml`.
- Runtime permission requested on Android 13+ in `MainActivity`.
- Notification icon uses system `android.R.drawable.ic_dialog_alert` (no custom drawable required).

### 9. Database Safety
- No data loss risk. DB migrated from v1→v2 using temp-table strategy.
- Supplier/purchase/invoice fields retained in `BatchEntity` schema; no migration needed for those.

### 10. Delete Button functionality (with confirmation prompts)
- **Product deletion**: Added a delete icon button to `ProductDetailScreen` TopAppBar. Deleting a product removes all associated variants, batches, and transactions atomically inside a `@Transaction` block to respect SQLite foreign key constraints.
- **Variant deletion**: Added a delete icon button to `VariantDetailScreen` TopAppBar. Deleting a variant removes all associated batches and transactions atomically.
- **Batch deletion**: Added a delete icon button to `BatchCardItem` within `VariantDetailScreen`. Deleting a batch removes all associated transactions.
- **Confirmation dialogs**: All delete triggers present a standard Compose `AlertDialog` asking the user to confirm before proceeding to avoid accidental loss.

## Features Implemented
* **Phase 1 Foundation:**
  * Configured multi-module project Gradle dependencies (Jetpack Compose, Room DB, Material 3, Navigation, KSP compilation).
  * Built complete SQLite/Room database schema: `CategoryEntity`, `ProductEntity`, `VariantEntity`, `BatchEntity`, `StockTransactionEntity`, `AppSettingsEntity`.
  * Implemented all Room DAOs with advanced SQL queries (FEFO sorting, low stock check, joins).
  * Created custom database POJOs/models.
  * Designed premium Light/Dark Material 3 color schemes and typography systems.
* **Phase 2 Products & Variants:** Category/Product list, add/edit/archive, variant management.
* **Phase 3 Batches & Expiry:** VariantDetailScreen, ExpiryCalculator, batch CRUD, unit tests.
* **Phase 4 Stock & History:** StockScreen, AddStockScreen, RemoveStockScreen (FEFO), TransactionHistoryScreen.
* **Phase 5 Dashboard & Search:** DashboardViewModel, LowStockScreen, ExpiryManagementScreen, MoreScreen.
* **Phase 6 Backup & Restore:** BackupManager (WAL checkpoint + FileProvider), BackupRestoreScreen, SAF restore.
* **Phase 7 Testing & Polish:** SettingsScreen, dynamic expiry threshold, full build + unit tests verified.

## Known Issues or Limitations

* Notification worker icon uses `android.R.drawable.ic_dialog_alert`. A dedicated `ic_notification` drawable in the app's resources would look more professional; this can be added later without any structural change.
* WorkManager's minimum repeat interval is 15 minutes on Android; the 24-hour periodic work request is correct for daily checks.
