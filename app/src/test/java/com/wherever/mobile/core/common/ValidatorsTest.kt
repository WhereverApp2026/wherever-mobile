package com.wherever.mobile.core.common

import com.wherever.mobile.feature.auth.domain.isValidName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {
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
