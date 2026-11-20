package com.wherever.detekt

import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class HardcodedDesignValueTest {
    private val rule = HardcodedDesignValue()

    @Test
    fun `reporta dp y sp escritos a mano`() {
        val code = """
            val a = 16.dp
            val b = 14.sp
            val c = 4.5.dp
        """.trimIndent()
        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `reporta un color hex`() {
        assertEquals(1, rule.lint("val c = Color(0xFF2455E8)").size)
    }

    @Test
    fun `acepta tokens, aritmetica de tokens y colores del tema`() {
        val code = """
            val a = Spacing.L
            val b = Sizes.StepBadge / 2 - Strokes.Thin / 2
            val c = Color.White
            val d = Color(red = 1f, green = 0f, blue = 0f)
            val e = size.dp
        """.trimIndent()
        assertEquals(0, rule.lint(code).size)
    }
}
