package com.wherever.mobile.feature.plans.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-24 (UI): acciones de parada en 39_Plan, límite de 4 paradas y Crear plan en CrearPlan. */
@RunWith(AndroidJUnit4::class)
class PlanDetailContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun cambiarYQuitarMandanLaParada() {
        val events = mutableListOf<String>()
        compose.setContent {
            WhereverTheme {
                PlanDetailContent(
                    PlanDetailUiState(loading = false, plan = samplePlanDetail()),
                    PlanDetailActions(
                        onChange = { events += "change:${it.id}" },
                        onRemove = { events += "remove:${it.id}" }
                    ),
                    null,
                    zone = PREVIEW_ZONE
                )
            }
        }

        compose.onNodeWithTag("stop_change_s1").performClick()
        compose.onNodeWithTag("stop_remove_s2").performClick()

        assertEquals(listOf("change:s1", "remove:s2"), events)
    }

    @Test
    fun conCuatroParadasNoSeAgregaOtra() {
        val plan = samplePlanDetail()
        var state by mutableStateOf(PlanDetailUiState(loading = false, plan = plan))
        compose.setContent {
            WhereverTheme { PlanDetailContent(state, PlanDetailActions(), null, zone = PREVIEW_ZONE) }
        }

        compose.onNodeWithTag("plan_add_stop").performScrollTo().assertIsEnabled()
        state = state.copy(plan = plan.copy(stops = plan.stops + plan.stops.first().copy(id = "s4")))
        compose.onNodeWithTag("plan_add_stop").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun crearPlanPideNombreYDosParadas() {
        var state by mutableStateOf(ManualPlanUiState(date = LocalDate.of(2026, 10, 18)))
        compose.setContent {
            WhereverTheme { ManualPlanContent(state, LocalDate.of(2026, 10, 17), ManualPlanActions(), emptyList()) }
        }

        compose.onNodeWithTag("manual_create").assertIsNotEnabled()
        state = state.copy(
            name = "Sábado",
            stops = listOf(ManualPlace("a", "Café", "cafes", null, 0), ManualPlace("b", "Parque", "parques", null, 0))
        )
        compose.onNodeWithTag("manual_create").assertIsEnabled()
        compose.onNodeWithTag("manual_stop_1").assertIsDisplayed()
    }
}
