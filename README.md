# Stock Manager — Retail Inventory App

An offline-first Android application designed for managing stock, variants, batches, and transactions for retail businesses.

---

## Key Features

- **Product & Variant Hierarchy:** Manage products, categories, and custom variants with low stock threshold alerts.
- **Batch Tracking & FEFO:** Track individual batches with auto-generated batch IDs, manufacturing dates, shelf life, and FEFO (First-Expired, First-Out) stock issue recommendations.
- **"Never Expire" Support:** Full support for non-expiring chemical products alongside standard date-based expiry.
- **Stock IN & Stock OUT:** Simple stock adjustment workflows with notes tracking.
- **Transaction History:** Detailed historical audit log with searchable notes, product/variant filtering, and date range filters.
- **Data Integrity:** Historical transactions survive product, variant, or batch deletion.
- **Automatic Daily Offline Backups:** Built-in offline database backup and restore manager.
- **Accessibility & Responsive Layouts:** Dynamic screen fitting and system text scaling support.

---

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material 3 Design
- **Database:** Room DB / SQLite (Schema Version 5)
- **Architecture:** MVVM with Clean Architecture principles
- **Asynchronous Flow:** Kotlin Coroutines & `StateFlow`
- **Background Operations:** WorkManager (Daily notifications, transaction retention cleanup, automated daily backups)

---

## Building the Project

### Requirements

- Android Studio Giraffe or newer
- JDK 17+ (or JDK 22)
- Android SDK API 34 (Minimum SDK API 26)

### Gradle Commands

- **Build Debug APK:**
  ```bash
  ./gradlew assembleDebug
  ```

- **Run Unit Tests:**
  ```bash
  ./gradlew test
  ```

- **Build Release APK:**
  ```bash
  ./gradlew assembleRelease
  ```

---

## License

Copyright © 2026 Aarav Shah. All rights reserved.
