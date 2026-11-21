package com.wherever.mobile.feature.friends.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.UiText
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-20 (UI): 25_MiCodigo muestra el QR con sus acciones, cambia a Escanear y muestra error y vacío. */
@RunWith(AndroidJUnit4::class)
class MyCodeContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun codigoAccionesYPestanaEscanear() {
        val events = mutableListOf<String>()
        var state by mutableStateOf(
            MyCodeUiState(loading = false, name = "Ana López", username = "ana_l", code = "K7M2P9QX")
        )
        compose.setContent {
            WhereverTheme {
                MyCodeContent(
                    state,
                    MyCodeActions(
                        onTab = { state = state.copy(tab = it) },
                        onShare = { events += "share" },
                        onSave = { events += "save" },
                        onScan = { events += "scan" }
                    )
                )
            }
        }

        compose.onNodeWithTag("my_code_qr").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.friends_username_at, "ana_l")).assertIsDisplayed()
        compose.onNodeWithTag("my_code_share").performClick()
        compose.onNodeWithTag("my_code_save").performClick()

        compose.onNodeWithText(context.getString(R.string.my_code_scan_tab)).performClick()
        compose.onNodeWithText(context.getString(R.string.my_code_scan_title)).assertIsDisplayed()
        compose.onNodeWithTag("my_code_scan").performClick()

        state = state.copy(scanError = UiText.Res(R.string.my_code_scan_invalid))
        compose.onNodeWithTag("my_code_scan_error").assertIsDisplayed()
        assertEquals(listOf("share", "save", "scan"), events)
    }

    @Test
    fun errorYVacioOfrecenReintentar() {
        var retries = 0
        var state by mutableStateOf(
            MyCodeUiState(loading = false, loadError = UiText.Res(R.string.my_code_image_error))
        )
        compose.setContent { WhereverTheme { MyCodeContent(state, MyCodeActions(onRetry = { retries++ })) } }

        compose.onNodeWithTag("common_error_state").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.common_retry)).performClick()

        state = MyCodeUiState(loading = false)
        compose.onNodeWithText(context.getString(R.string.my_code_empty)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.common_retry)).performClick()
        assertEquals(2, retries)
    }
}
