package com.wherever.mobile.feature.home.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/** "8 min a pie" de las tarjetas de "Cerca de ti" (10_Inicio): 80 m por minuto, hacia arriba, mínimo 1. */
class WalkMinutesTest {
    @Test
    fun redondeaHaciaArribaConMinimoDeUno() {
        assertEquals(1, walkMinutes(0.0))
        assertEquals(1, walkMinutes(80.0))
        assertEquals(2, walkMinutes(81.0))
        assertEquals(8, walkMinutes(640.0))
        assertEquals(14, walkMinutes(1120.0))
    }
}
