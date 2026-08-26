# Technical Architecture - Waterproofing Chemical Inventory App

This document outlines the detailed system architecture, database design, and code patterns.

## Application Architecture

The application is structured following modern Android guidelines:
- **UI Layer:** Jetpack Compose screens consuming state via standard Kotlin state flows.
- **ViewModel/State Layer:** ViewModel classes utilizing Kotlin `StateFlow` to manage and emit UI state, interacting with Repositories.
- **Data Layer:** Room database representing SQLite, using repositories to encapsulate DAO operations and handle atomic transactions.

```text
Jetpack Compose Screens (UI)
       ▲
       │ (StateFlow)
ViewModels (UI State / Events)
       ▲
       │
Repositories (Data Abstraction / Operations)
       ▲
       │
Room DAOs & SQLite Database
```

## Database Schema and Relationships (DB Version 4)

### Core Architectural Invariant
> **Historical transactions are independent records and must survive deletion of their associated Product, Variant, or Batch.**

### 1. Categories (`CategoryEntity`)
- `id` (Long, Primary Key, Auto-increment)
- `name` (String, Unique Index)
- `created_at` (Long)
- `updated_at` (Long)

### 2. Products (`ProductEntity`)
- `id` (Long, Primary Key, Auto-increment)
- `name` (String)
- `category_id` (Long?, Foreign Key to `CategoryEntity.id`, ON DELETE SET NULL)
- `description` (String)
- `is_archived` (Boolean, Default: false)
- `created_at` (Long)
- `updated_at` (Long)

*(Note: `brand` column dropped in DB migration v2→v3)*

### 3. Variants (`VariantEntity`)
- `id` (Long, Primary Key, Auto-increment)
- `product_id` (Long, Foreign Key to `ProductEntity.id`, ON DELETE CASCADE)
- `name` (String)
- `quantity_value` (Double)
- `unit` (String)
- `min_stock_threshold` (Double, Default: 0.0)
- `is_archived` (Boolean, Default: false)
- `created_at` (Long)
- `updated_at` (Long)

*(Note: `sku` column dropped in DB migration v1→v2)*

### 4. Batches (`BatchEntity`)
- `id` (Long, Primary Key, Auto-increment)
- `variant_id` (Long, Foreign Key to `VariantEntity.id`, ON DELETE CASCADE)
- `batch_number` (String, Index)
- `current_quantity` (Double)
- `mfg_date` (Long?)
- `shelf_life_value` (Int?)
- `shelf_life_unit` (String?)
- `expiry_date` (Long, Index)
- `purchase_price` (Double?)
- `supplier` (String?)
- `invoice_number` (String?)
- `notes` (String?)
- `is_depleted` (Boolean, Default: false)
- `created_at` (Long)
- `updated_at` (Long)

### 5. Stock Transactions (`StockTransactionEntity`)
- `id` (Long, Primary Key, Auto-increment)
- `batch_id` (Long?, Foreign Key to `BatchEntity.id`, ON DELETE SET NULL)
- `product_id` (Long?, Foreign Key to `ProductEntity.id`, ON DELETE SET NULL)
- `variant_id` (Long?, Foreign Key to `VariantEntity.id`, ON DELETE SET NULL)
- `product_name` (String, Snapshot at creation)
- `variant_name` (String, Snapshot at creation)
- `batch_number` (String, Snapshot at creation)
- `transaction_type` (String: "IN", "OUT", "ADJUSTMENT")
- `quantity` (Double)
- `unit` (String)
- `timestamp` (Long)
- `reason` (String)
- `customer_project` (String?)
- `invoice_number` (String?)
- `notes` (String?)
- `created_at` (Long)

*(Note: `ON DELETE SET NULL` and snapshot fields added in DB migration v3→v4)*

## Background Tasks (WorkManager)

1. **ExpiryNotificationWorker:** Daily periodic check for batches expiring within 30 days.
2. **TransactionCleanupWorker:** Daily periodic purge of stock transactions older than 6 calendar months. Does not alter stock quantities.
3. **AutoBackupWorker:** Scheduled daily offline database backup. Writes to app-internal storage (`filesDir/backups`), enforces configurable retention (default 7 backups), and sends completion/failure notifications.
