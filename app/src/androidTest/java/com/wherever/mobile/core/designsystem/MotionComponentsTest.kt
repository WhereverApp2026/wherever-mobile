package com.wherever.mobile.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-45 (UI): errores de campo, botón cargando y cambio de fase, con y sin animaciones. */
@RunWith(AndroidJUnit4::class)
class MotionComponentsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun fieldErrorAppearsAndLeaves() {
        var error by mutableStateOf<String?>(null)
        compose.setContent { WhereverTheme { FieldErrorText(error, Modifier.testTag("err")) } }
        compose.onNodeWithTag("err").assertDoesNotExist()
        error = ERROR_TEXT
        compose.onNodeWithText(ERROR_TEXT).assertExists()
        error = null
        compose.waitForIdle()
        compose.onNodeWithTag("err").assertDoesNotExist()
    }

    @Test
    fun primaryButtonSwapsToSpinnerWhileLoading() {
        var loading by mutableStateOf(false)
        compose.setContent {
            WhereverTheme { WhereverPrimaryButton(text = BUTTON_TEXT, onClick = {}, loading = loading, tag = "btn") }
        }
        compose.onNodeWithText(BUTTON_TEXT).assertExists()
        loading = true
        compose.waitForIdle()
        compose.onNodeWithText(BUTTON_TEXT).assertDoesNotExist()
    }

    @Test
    fun phaseChangesInstantlyWhenMotionIsOff() {
        var phase by mutableStateOf(ScreenPhase.LOADING)
        compose.setContent {
            WhereverTheme {
                CompositionLocalProvider(LocalMotionEnabled provides false) {
                    PhaseCrossfade(phase) { Text(it.name, Modifier.testTag("phase_${it.name}")) }
                }
            }
        }
        compose.mainClock.autoAdvance = false
        phase = ScreenPhase.CONTENT
        // Sin animación basta un cuadro: lo anterior ya no está y lo nuevo sí.
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithTag("phase_CONTENT").assertExists()
        compose.onNodeWithTag("phase_LOADING").assertDoesNotExist()
    }

    private companion object {
        const val ERROR_TEXT = "Escribe una cantidad"
        const val BUTTON_TEXT = "Guardar"
    }
}
