package com.wherever.mobile.core.common

import com.wherever.mobile.feature.auth.domain.isAdult
import com.wherever.mobile.feature.auth.domain.isValidName
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {
    private val clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `contrasena de 8 a 128 con letra y numero`() {
        assertTrue(isValidPassword("abcdefg1"))
        assertFalse(isValidPassword("abc1"))
        assertFalse(isValidPassword("abcdefgh"))
        assertFalse(isValidPassword("12345678"))
        assertFalse(isValidPassword("a1" + "x".repeat(127)))
    }

    @Test
    fun `nombre de 2 a 40`() {
        assertTrue(isValidName("Al"))
        assertFalse(isValidName("A"))
        assertFalse(isValidName("x".repeat(41)))
    }

    @Test
    fun `mayoria de edad justo en el cumpleanos 18`() {
        assertTrue(isAdult(LocalDate.of(2008, 11, 4), clock))
        assertFalse(isAdult(LocalDate.of(2008, 11, 5), clock))
    }

    @Test
    fun `correo`() {
        assertTrue(isValidEmail("ana@ejemplo.com"))
        assertFalse(isValidEmail("ana@"))
        assertFalse(isValidEmail("ana ejemplo.com"))
    }

    @Test
    fun `correo enmascarado`() {
        assertEquals("os•••@correo.com", maskEmail("osmar@correo.com"))
        assertEquals("a•••@b.co", maskEmail("a@b.co"))
    }
}
