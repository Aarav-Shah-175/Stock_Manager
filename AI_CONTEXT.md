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
 └── Variant
      └── Batch (with expiry, mfg date, shelf life)
           └── Stock Transaction (History)
```
*Note: A single product (e.g. Dr. Fixit Pidicrete URP) can have multiple variants (e.g., "1 kg", "5 kg", "20 kg"). Each variant can have multiple batches (e.g., "Batch A", "Batch B"), tracked with individual manufacturing/expiry dates and quantities.*

## Tech Stack
- Kotlin & Jetpack Compose
- Room Database & SQLite
- Navigation Compose
- Kotlin Coroutines & Flow (StateFlow)
- MVVM / Clean Architecture
- Material 3 Design
- Gradle Kotlin DSL (`.gradle.kts`)

## How to Build and Run
1. Open the project in Android Studio (Giraffe/Hedgehog or newer).
2. Sync Gradle.
3. Run on a modern emulator or connected device (Android API 26+).
4. Run tests: `./gradlew test`
