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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-23 (UI): Armar mi plan pide texto; los pasos de Generando y Ver mi plan al terminar. */
@RunWith(AndroidJUnit4::class)
class CreatePlanContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.of(2026, 10, 17)

    @Test
    fun armarMiPlanPideTexto() {
        var state by mutableStateOf(CreatePlanUiState())
        compose.setContent { WhereverTheme { CreatePlanContent(state, today, CreatePlanActions()) } }

        compose.onNodeWithTag("plan_generate").assertIsNotEnabled()
        state = state.copy(phrase = "tacos")
        compose.onNodeWithTag("plan_generate").assertIsEnabled()
    }

    @Test
    fun ajustarDetallesAbre17AjustaPlan() {
        var adjusted = false
        compose.setContent {
            WhereverTheme {
                CreatePlanContent(
                    CreatePlanUiState(phrase = "tacos"),
                    today,
                    CreatePlanActions(onAdjust = {
                        adjusted =
                            true
                    })
                )
            }
        }

        compose.onNodeWithTag("plan_adjust_details").performClick()
        assertTrue(adjusted)
    }

    @Test
    fun generandoMarcaElPasoActivoYAlFinalVerMiPlan() {
        var state by mutableStateOf(CreatePlanUiState(phrase = "tacos", phase = CreatePhase.GENERATING, step = 1))
        compose.setContent { WhereverTheme { CreatePlanContent(state, today, CreatePlanActions()) } }

        compose.onNodeWithTag("plan_step_0_done").assertIsDisplayed()
        compose.onNodeWithTag("plan_step_1_active").assertIsDisplayed()
        compose.onNodeWithTag("plan_cancel").assertIsDisplayed()

        state = state.copy(phase = CreatePhase.READY, step = 3)
        compose.onNodeWithTag("plan_step_3_done").assertIsDisplayed()
        compose.onNodeWithTag("plan_show").assertIsDisplayed()
    }
}
