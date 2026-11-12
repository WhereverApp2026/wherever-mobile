package com.wherever.mobile.feature.auth.domain

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** nv-42: fecha de nacimiento contra "hoy" del reloj inyectado, en la zona de México. */
class BirthDateValidatorTest {
    private val mexico = ZoneId.of("America/Mexico_City")

    /** 2026-11-12 a las 23:30 en México; en UTC ya es 2026-11-13. */
    private val nightInMexico = Clock.fixed(Instant.parse("2026-11-13T05:30:00Z"), mexico)
    private val today = LocalDate.of(2026, 11, 12)

    private fun clockOn(date: LocalDate) = Clock.fixed(date.atTime(12, 0).atZone(mexico).toInstant(), mexico)

    @Test
    fun `hoy no es futuro, pero es menor de edad`() {
        assertEquals(BirthDateIssue.UNDERAGE, validateBirthDate(today, nightInMexico))
    }

    @Test
    fun `de noche en Mexico, la fecha de hoy no se vuelve manana`() {
        assertEquals(BirthDateIssue.FUTURE, validateBirthDate(today.plusDays(1), nightInMexico))
        assertNull(validateBirthDate(today.minusYears(18), nightInMexico))
    }

    @Test
    fun `manana es futuro`() {
        assertEquals(BirthDateIssue.FUTURE, validateBirthDate(today.plusDays(1), nightInMexico))
    }

    @Test
    fun `el mes que viene es futuro`() {
        assertEquals(BirthDateIssue.FUTURE, validateBirthDate(LocalDate.of(2026, 12, 12), nightInMexico))
    }

    @Test
    fun `el ano que viene es futuro`() {
        assertEquals(BirthDateIssue.FUTURE, validateBirthDate(LocalDate.of(2027, 11, 12), nightInMexico))
    }

    @Test
    fun `justo 18 anos es valido`() {
        assertNull(validateBirthDate(LocalDate.of(2008, 11, 12), nightInMexico))
    }

    @Test
    fun `un dia antes de cumplir 18 es menor de edad`() {
        assertEquals(BirthDateIssue.UNDERAGE, validateBirthDate(LocalDate.of(2008, 11, 13), nightInMexico))
    }

    @Test
    fun `29 de febrero cumple 18 el 1 de marzo de un ano no bisiesto`() {
        val leapDay = LocalDate.of(2008, 2, 29)

        assertEquals(BirthDateIssue.UNDERAGE, validateBirthDate(leapDay, clockOn(LocalDate.of(2026, 2, 28))))
        assertNull(validateBirthDate(leapDay, clockOn(LocalDate.of(2026, 3, 1))))
        assertNull(validateBirthDate(leapDay, nightInMexico))
    }

    @Test
    fun `hace 101 anos es demasiado viejo`() {
        assertEquals(BirthDateIssue.TOO_OLD, validateBirthDate(LocalDate.of(1925, 11, 12), nightInMexico))
    }

    @Test
    fun `hace 100 anos justos todavia se acepta, un dia antes no`() {
        assertNull(validateBirthDate(LocalDate.of(1926, 11, 12), nightInMexico))
        assertEquals(BirthDateIssue.TOO_OLD, validateBirthDate(LocalDate.of(1926, 11, 11), nightInMexico))
    }
}
