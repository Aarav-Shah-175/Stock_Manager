# Feature Update Log — Post-Completion Changes

**Date:** 2026-08-13  
**Build Result:** ✅ BUILD SUCCESSFUL (2m 56s, 0 errors)

---

## Goals

Remove unused/unwanted fields from the UI, add fast product search, remove slow animations, rename backup files, clean up transaction history, and add offline expiry notifications.

---

## Progress

| # | Feature | Status | Notes |
|---|---------|--------|-------|
| 1 | Remove SKU from Variants | ✅ DONE | DB migration v1→v2 applied |
| 2 | Remove Batch Number input from Add Stock | ✅ DONE | Auto-generates `B-yyyyMMdd-HHmmss` |
| 3 | Remove Supplier & Purchasing section | ✅ DONE | Fields kept in DB schema (data preserved) |
| 4 | Searchable Product in Stock IN | ✅ DONE | Live text filter, offline |
| 5 | Searchable Product in Stock OUT | ✅ DONE | Same pattern as Stock IN |
| 6 | Page transitions faster (none) | ✅ DONE | `EnterTransition.None` / `ExitTransition.None` |
| 7 | Rename backup prefix | ✅ DONE | `Inventory_Backup_...` |
| 8 | Remove Adjustment from Transaction History | ✅ DONE | Tab removed; existing data preserved |
| 9 | Expiry-soon notifications | ✅ DONE | WorkManager, daily, single notification |
| 10 | DB migration safety | ✅ DONE | Temp-table copy, no data loss |
| 11 | Documentation updated | ✅ DONE | AI_CONTEXT.md, DEVELOPMENT_STATUS.md |
| 12 | Delete product, variant, and batch | ✅ DONE | Added buttons & confirmation dialogs |

---

## Files Changed

### Data Layer
- `VariantEntity.kt` — removed `sku` field
- `VariantWithStock.kt` — removed `sku` field
- `VariantDao.kt` — removed `v.sku` from SQL projections
- `ProductDao.kt` — removed `v.sku` from search query
- `VariantRepository.kt` — removed sku parameter passing; added `deleteVariant()` method
- `BatchDao.kt` — added `getExpiringSoonBatchesOnce()` one-shot query; added `deleteBatch()` atomic transaction method
- `ProductDao.kt` — added `deleteProduct()` atomic transaction method
- `VariantDao.kt` — added `deleteVariant()` atomic transaction method
- `ProductRepository.kt` — added `deleteProduct()` method
- `BatchRepository.kt` — added `deleteBatch()` method

### Database
- `AppDatabase.kt` — version 1→2, added `MIGRATION_1_2` (recreates `variants` table without `sku`)

### ViewModels
- `ProductDetailViewModel.kt` — removed sku from `addVariant()`; added `deleteProduct()`
- `VariantViewModel.kt` — added `deleteVariant()` and `deleteBatch()`

### UI — Products
- `ProductDetailScreen.kt` — removed SKU field from `VariantAddEditDialog`, SKU display from `VariantCardItem`; added Product delete button and confirmation prompt
- `ProductListScreen.kt` — removed "SKU" from search placeholder text
- `VariantDetailScreen.kt` — removed SKU display from variant banner; removed Batch Number input, Supplier & Purchasing section from `BatchAddEditDialog`; added Variant delete button and Batch delete button with confirmation prompts

### UI — Stock
- `AddStockScreen.kt` — replaced read-only product dropdown with searchable text field; removed Invoice Number field
- `RemoveStockScreen.kt` — replaced read-only product dropdown with searchable text field
- `TransactionHistoryScreen.kt` — removed ADJUSTMENT tab; ALL filter excludes ADJUSTMENT records

### Domain
- `BackupManager.kt` — prefix: `WaterproofInventory_Backup` → `Inventory_Backup`
- `ExpiryNotificationWorker.kt` — **NEW** — daily WorkManager worker, checks expiring batches within 30 days, posts single summary notification

### App Infrastructure
- `InventoryApplication.kt` — creates notification channel + schedules WorkManager on startup
- `MainActivity.kt` — requests `POST_NOTIFICATIONS` permission on Android 13+
- `AndroidManifest.xml` — added `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`
- `app/build.gradle.kts` — added `androidx.work:work-runtime-ktx:2.9.0`

### Navigation
- `AppNavigation.kt` — `EnterTransition.None` / `ExitTransition.None` on NavHost

### Documentation
- `AI_CONTEXT.md` — updated tech stack, DB version, post-completion changes table
- `DEVELOPMENT_STATUS.md` — added post-completion changes section and delete button info
- `FEATURE_UPDATE.md` — this file

---

## Database Migration Detail

```sql
-- MIGRATION_1_2: Drop SKU column from variants
-- (SQLite < 3.35 does not support ALTER TABLE DROP COLUMN, so use temp-table copy)

CREATE TABLE variants_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    product_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    quantity_value REAL NOT NULL,
    unit TEXT NOT NULL,
    min_stock_threshold REAL NOT NULL DEFAULT 0.0,
    is_archived INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL DEFAULT 0,
    updated_at INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE
);

INSERT INTO variants_new SELECT id, product_id, name, quantity_value, unit,
    min_stock_threshold, is_archived, created_at, updated_at FROM variants;

DROP TABLE variants;
ALTER TABLE variants_new RENAME TO variants;
CREATE INDEX index_variants_product_id ON variants (product_id);
```

---

## Notification Architecture

```
InventoryApplication.onCreate()
  ├── ExpiryNotificationWorker.createNotificationChannel()   // channel: inventory_expiry_alerts
  └── ExpiryNotificationWorker.schedule()                    // WorkManager, 24h periodic, KEEP policy

ExpiryNotificationWorker.doWork()
  ├── Query: BatchDao.getExpiringSoonBatchesOnce(now, now + 30 days)
  ├── If empty → cancel existing notification
  └── If batches found → post/update notification ID 1001 with summary
```

---

## Remaining Minor Items

- Notification small icon is the system `android.R.drawable.ic_dialog_alert`. For a more branded look, add an `ic_notification.xml` vector drawable and reference `R.drawable.ic_notification` in `ExpiryNotificationWorker`. No structural changes needed.
