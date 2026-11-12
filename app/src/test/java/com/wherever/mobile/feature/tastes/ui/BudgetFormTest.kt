package com.wherever.mobile.feature.tastes.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** nv-44: sincronía entre la barra y los campos del presupuesto. */
class BudgetFormTest {
    @Test
    fun `arranca en 150 a 300 y es valido`() {
        val form = BudgetForm()
        assertEquals(150f, form.sliderStart)
        assertEquals(300f, form.sliderEnd)
        assertTrue(form.isValid)
    }

    @Test
    fun `escribir en los campos mueve la barra`() {
        val form = BudgetForm().withMinText("200").withMaxText("750")
        assertEquals(200f, form.sliderStart)
        assertEquals(750f, form.sliderEnd)
    }

    @Test
    fun `mover la barra cambia los campos`() {
        val form = BudgetForm().slide(150f, 640.4f)
        assertEquals("150", form.minText)
        assertEquals("640", form.maxText)
    }

    @Test
    fun `al soltar el extremo movido queda en pasos de 50`() {
        val form = BudgetForm().slide(180f, 300f).slide(180f, 640f).finishSlide()
        assertEquals("200", form.minText)
        assertEquals("650", form.maxText)
        assertFalse(form.minDragged || form.maxDragged)
    }

    @Test
    fun `lo escrito a mano no se redondea al mover el otro extremo`() {
        val form = BudgetForm().withMinText("123").slide(123f, 620f).finishSlide()
        assertEquals("123", form.minText)
        assertEquals("600", form.maxText)
    }

    @Test
    fun `mas de 1000 deja la barra al tope pero guarda lo escrito`() {
        val form = BudgetForm().withMaxText("2500")
        assertEquals(1000f, form.sliderEnd)
        assertEquals(2500, form.max)

        val moved = form.slide(400f, 1000f).finishSlide()
        assertEquals("400", moved.minText)
        assertEquals("2500", moved.maxText)
    }

    @Test
    fun `arrastrar al tope deja 1000`() {
        val form = BudgetForm().slide(150f, 1000f).finishSlide()
        assertEquals(1000, form.max)
    }

    @Test
    fun `minimo mayor que maximo no es valido y la barra no se cruza`() {
        val form = BudgetForm().withMinText("800").withMaxText("300")
        assertFalse(form.isValid)
        assertEquals(800f, form.sliderStart)
        assertEquals(800f, form.sliderEnd)
    }

    @Test
    fun `campo vacio deja la barra en el borde y no es valido`() {
        val form = BudgetForm().withMinText("")
        assertEquals(0f, form.sliderStart)
        assertFalse(form.isValid)
    }

    @Test
    fun `negativo se queda en el campo y la barra en 0`() {
        val form = BudgetForm().withMinText("-50")
        assertEquals("-50", form.minText)
        assertEquals(0f, form.sliderStart)
        assertFalse(form.isValid)
    }
}
