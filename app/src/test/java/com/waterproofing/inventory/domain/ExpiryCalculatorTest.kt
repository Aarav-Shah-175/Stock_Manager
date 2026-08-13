package com.waterproofing.inventory.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ExpiryCalculatorTest {

    @Test
    fun testCalculateDays() {
        val mfgDate = LocalDate.of(2026, 3, 15)
        val mfgDateEpochMs = mfgDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val calculated = ExpiryCalculator.calculate(mfgDateEpochMs, 10, "Days")
        val expected = LocalDate.of(2026, 3, 25).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expected, calculated)
    }

    @Test
    fun testCalculateMonths() {
        val mfgDate = LocalDate.of(2026, 3, 15)
        val mfgDateEpochMs = mfgDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val calculated = ExpiryCalculator.calculate(mfgDateEpochMs, 24, "Months")
        val expected = LocalDate.of(2028, 3, 15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expected, calculated)
    }

    @Test
    fun testCalculateLeapYear() {
        val mfgDate = LocalDate.of(2024, 2, 28)
        val mfgDateEpochMs = mfgDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val calculated = ExpiryCalculator.calculate(mfgDateEpochMs, 1, "Days")
        val expected = LocalDate.of(2024, 2, 29).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expected, calculated)
    }

    @Test
    fun testCalculateMonthEnd() {
        val mfgDate = LocalDate.of(2026, 10, 31)
        val mfgDateEpochMs = mfgDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val calculated = ExpiryCalculator.calculate(mfgDateEpochMs, 1, "Months")
        val expected = LocalDate.of(2026, 11, 30).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expected, calculated)
    }
}
