package com.wherever.mobile.feature.vaca.ui

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
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-26 (UI): aviso de que no se mueve dinero, Marcar pagado y Guardar gasto solo con datos válidos. */
@RunWith(AndroidJUnit4::class)
class VacaContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun avisoYMarcarPagado() {
        val paid = mutableListOf<String>()
        compose.setContent {
            WhereverTheme { VacaContent(sampleVacaState(), VacaActions(onPay = { paid += it.id })) }
        }

        compose.onNodeWithTag("vaca_notice").assertIsDisplayed()
        compose.onNodeWithTag("pay_d1").performScrollTo().performClick()

        assertEquals(listOf("d1"), paid)
    }

    @Test
    fun guardarGastoSoloConMontoValido() {
        var state by mutableStateOf(sampleExpenseState().copy(amount = ""))
        compose.setContent { WhereverTheme { AddExpenseContent(state, AddExpenseActions()) } }

        compose.onNodeWithTag("expense_save").assertIsNotEnabled()
        state = state.copy(amount = "480")
        compose.onNodeWithTag("expense_save").assertIsEnabled()
    }
}
