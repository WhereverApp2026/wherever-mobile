package com.wherever.mobile.feature.auth.ui

import com.wherever.mobile.feature.auth.domain.DEFAULT_BIRTH_YEARS_AGO
import com.wherever.mobile.feature.auth.domain.oldestBirthDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Límites del calendario de fecha de nacimiento (nv-42).
 *
 * El DatePicker de Material 3 representa cada día como la medianoche UTC de ese día, sin importar
 * la zona del teléfono. Por eso [today] (calculado con el reloj inyectado, en la zona local) se
 * convierte a milisegundos en UTC y no al revés: a las 23:00 en México ya es mañana en UTC, y
 * comparar contra `System.currentTimeMillis()` dejaría elegir mañana.
 */
internal class BirthDateBounds(private val today: LocalDate) {
    private val oldest = oldestBirthDate(today)

    val yearRange: IntRange = oldest.year..today.year

    /** El calendario abre en el mes de hace 18 años, no en hoy. */
    val initialDisplayedMonthMillis: Long = today.minusYears(DEFAULT_BIRTH_YEARS_AGO).toPickerMillis()

    fun isSelectable(utcTimeMillis: Long): Boolean = utcTimeMillis.toPickerDate() in oldest..today

    fun isSelectableYear(year: Int): Boolean = year in yearRange
}

internal fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun Long.toPickerDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
