package com.wherever.mobile.feature.tastes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** nv-44: limpieza de lo escrito o pegado y validación del presupuesto. */
class BudgetValidatorTest {
    @Test
    fun `limpia espacios, ceros a la izquierda y letras`() {
        assertEquals("150", sanitizeAmount(" 150 "))
        assertEquals("7", sanitizeAmount("007"))
        assertEquals("0", sanitizeAmount("000"))
        assertEquals("12", sanitizeAmount("1a2b"))
        assertEquals("", sanitizeAmount("abc"))
        assertEquals("", sanitizeAmount(""))
    }

    @Test
    fun `quita decimales y comas de miles`() {
        assertEquals("150", sanitizeAmount("150.50"))
        assertEquals("1500", sanitizeAmount("1,500"))
        assertEquals("", sanitizeAmount(".5"))
    }

    @Test
    fun `texto pegado no truena`() {
        assertEquals("300", sanitizeAmount("Mi presupuesto: $300 MXN"))
        // Más dígitos de los que cabe en un Int: se corta a 6 y luego da error de rango, sin excepción.
        assertEquals("123456", sanitizeAmount("12345678901234567890"))
    }

    @Test
    fun `conserva el signo menos para avisar`() {
        assertEquals("-200", sanitizeAmount("-200"))
        assertEquals("-", sanitizeAmount("-"))
        assertEquals("200", sanitizeAmount("2-00"))
    }

    @Test
    fun `campo vacio pide una cantidad`() {
        assertEquals(BudgetIssue.EMPTY, validateAmount(""))
        assertEquals(BudgetIssue.EMPTY, validateAmount("-"))
    }

    @Test
    fun `rango de 0 a 50000 con sus bordes`() {
        assertNull(validateAmount("0"))
        assertNull(validateAmount("50000"))
        assertEquals(BudgetIssue.OUT_OF_RANGE, validateAmount("50001"))
        assertEquals(BudgetIssue.OUT_OF_RANGE, validateAmount("-1"))
        assertEquals(BudgetIssue.OUT_OF_RANGE, validateAmount("123456"))
    }

    @Test
    fun `minimo mayor que maximo va en el minimo`() {
        assertEquals(BudgetIssue.MIN_GREATER_THAN_MAX to null, validateBudget("500", "300"))
        assertEquals(null to null, validateBudget("300", "300"))
    }

    @Test
    fun `con un campo invalido no se compara el orden`() {
        assertEquals(null to BudgetIssue.EMPTY, validateBudget("500", ""))
        assertEquals(BudgetIssue.OUT_OF_RANGE to null, validateBudget("60000", "300"))
    }
}
