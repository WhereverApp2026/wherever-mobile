package com.wherever.mobile.feature.auth.ui

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** nv-42: el calendario trabaja en medianoche UTC y "hoy" llega del reloj en la zona local. */
class BirthDateBoundsTest {
    private val today = LocalDate.of(2026, 11, 12)
    private val bounds = BirthDateBounds(today)

    @Test
    fun `hoy se puede tocar y manana no`() {
        assertTrue(bounds.isSelectable(today.toPickerMillis()))
        assertFalse(bounds.isSelectable(today.plusDays(1).toPickerMillis()))
        assertFalse(bounds.isSelectable(LocalDate.of(2026, 12, 1).toPickerMillis()))
        assertFalse(bounds.isSelectable(LocalDate.of(2027, 1, 1).toPickerMillis()))
    }

    @Test
    fun `el dia mas viejo es hoy hace 100 anos`() {
        assertTrue(bounds.isSelectable(LocalDate.of(1926, 11, 12).toPickerMillis()))
        assertFalse(bounds.isSelectable(LocalDate.of(1926, 11, 11).toPickerMillis()))
        assertEquals(1926..2026, bounds.yearRange)
        assertFalse(bounds.isSelectableYear(1925))
        assertFalse(bounds.isSelectableYear(2027))
    }

    @Test
    fun `abre en el mes de hace 18 anos`() {
        assertEquals(LocalDate.of(2008, 11, 12), bounds.initialDisplayedMonthMillis.toPickerDate())
    }

    @Test
    fun `los milisegundos UTC del calendario no cambian de dia con la zona del telefono`() {
        val millis = today.toPickerMillis()

        assertEquals(Instant.parse("2026-11-12T00:00:00Z").toEpochMilli(), millis)
        assertEquals(today, millis.toPickerDate())
        assertEquals(today, (millis + MILLIS_IN_DAY - 1).toPickerDate())
    }

    private companion object {
        const val MILLIS_IN_DAY = 86_400_000L
    }
}
