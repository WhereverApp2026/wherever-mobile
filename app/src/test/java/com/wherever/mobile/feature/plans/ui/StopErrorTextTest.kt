package com.wherever.mobile.feature.plans.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.ui.UiText
import org.junit.Assert.assertEquals
import org.junit.Test

/** Encontrado con staging: el error de una parada mostraba el id del negocio en vez de su nombre. */
class StopErrorTextTest {
    private val stops = listOf(
        "6b045db57d777f3ea0301d45" to "Bar Dembow",
        "6b045db57d777f3ea0301d46" to "Restaurante Colonial"
    )

    @Test
    fun `parada cerrada por id se nombra`() {
        val error = AppError.Validation(apiMessage = "Parada 6b045db57d777f3ea0301d45: cerrado")
        assertEquals(UiText.Res(R.string.plan_stop_closed, listOf("Bar Dembow")), error.stopErrorText(stops))
    }

    @Test
    fun `parada por numero tambien se nombra`() {
        val error = AppError.Validation(apiMessage = "parada 1: no_cabe")
        assertEquals(UiText.Res(R.string.plan_stop_overlap, listOf("Restaurante Colonial")), error.stopErrorText(stops))
    }

    @Test
    fun `un mensaje que no es de parada queda igual`() {
        val error = AppError.Validation(apiMessage = "La fecha ya pasó")
        assertEquals(UiText.Plain("La fecha ya pasó"), error.stopErrorText(stops))
    }
}
