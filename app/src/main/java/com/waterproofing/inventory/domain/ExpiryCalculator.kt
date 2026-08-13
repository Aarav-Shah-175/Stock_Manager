package com.waterproofing.inventory.domain

import java.time.LocalDate
import java.time.ZoneId

object ExpiryCalculator {

    /**
     * Calculates expiry date from manufacturing date + shelf life.
     * Returns epoch milliseconds (start of day, device timezone).
     *
     * Shelf life unit: "Days", "Months", "Years"
     * Uses correct calendar arithmetic (handles month-end, leap years).
     */
    fun calculate(mfgDateEpochMs: Long, shelfLifeValue: Int, shelfLifeUnit: String): Long {
        val mfgDate = java.time.Instant.ofEpochMilli(mfgDateEpochMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val expiryDate = when (shelfLifeUnit.lowercase()) {
            "days"   -> mfgDate.plusDays(shelfLifeValue.toLong())
            "months" -> mfgDate.plusMonths(shelfLifeValue.toLong())
            "years"  -> mfgDate.plusYears(shelfLifeValue.toLong())
            else     -> mfgDate.plusMonths(shelfLifeValue.toLong())
        }

        return expiryDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * Returns the expiry status based on now and the warning threshold (in days).
     */
    fun getStatus(expiryEpochMs: Long, warningDays: Int = 90): ExpiryStatus {
        val now = System.currentTimeMillis()
        val warningThreshold = now + warningDays * 86_400_000L
        return when {
            expiryEpochMs < now               -> ExpiryStatus.EXPIRED
            expiryEpochMs <= warningThreshold -> ExpiryStatus.EXPIRING_SOON
            else                              -> ExpiryStatus.NORMAL
        }
    }
}

enum class ExpiryStatus { EXPIRED, EXPIRING_SOON, NORMAL }
