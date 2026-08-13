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

## Database Schema and Relationships

### 1. Categories (`CategoryEntity`)
- `id` (Long, Primary Key, Auto-increment)
- `name` (String, Unique Index)
- `created_at` (Long)
- `updated_at` (Long)

### 2. Products (`ProductEntity`)
- `id` (Long, Primary Key, Auto-increment)
- `name` (String)
- `brand` (String)
- `category_id` (Long?, Foreign Key to `CategoryEntity.id`, ON DELETE SET NULL)
- `description` (String)
- `is_archived` (Boolean, Default: false)
- `created_at` (Long)
- `updated_at` (Long)

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
- `batch_id` (Long, Foreign Key to `BatchEntity.id`, ON DELETE RESTRICT)
- `product_id` (Long, Foreign Key to `ProductEntity.id`, ON DELETE RESTRICT)
- `variant_id` (Long, Foreign Key to `VariantEntity.id`, ON DELETE RESTRICT)
- `transaction_type` (String: "IN", "OUT", "ADJUSTMENT")
- `quantity` (Double)
- `unit` (String)
- `timestamp` (Long)
- `reason` (String)
- `customer_project` (String?)
- `invoice_number` (String?)
- `notes` (String?)
- `created_at` (Long)

## Critical Business Logic Flow

### 1. Adding Stock
1. User provides batch details (batch #, quantity, dates, etc.).
2. Database checks if batch exists. If yes, updates quantity. If no, creates new Batch.
3. Database inserts transaction record with type `IN` and details.
4. Performed atomically using `@Transaction`.

### 2. FEFO Stock Removal (First Expire, First Out)
1. User specifies Product, Variant, and Quantity to remove.
2. Query variant's active batches, sorted by `expiry_date` ascending.
3. Select batches sequentially starting from the top.
4. Auto-recommend batch selections matching required quantity.
5. User can override batch selection.
6. Verify selected batch(es) have sufficient quantity to prevent negative stock.
7. Decrement batch quantity and insert `OUT` transaction(s) atomically.

### 3. Expiry & Shelf Life Calculations
- **Dynamic:** Expiry Date = Manufacturing Date + (Shelf Life Value in Shelf Life Units).
- Supports manual override. Direct manual edits to `expiry_date` are preserved and will not be overwritten by recalculation unless requested.
