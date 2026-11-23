package com.wherever.mobile.feature.business.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-35 (UI): la barra del negocio cambia de pestaña y el check-in sin código no deja confirmar. */
@RunWith(AndroidJUnit4::class)
class BusinessHomeContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun barraDeNegocioCambiaDePestana() {
        var tab by mutableStateOf(BusinessTab.PANEL)
        compose.setContent { WhereverTheme { BusinessBar(tab) { tab = it } } }

        compose.onNodeWithTag("business_bar_panel").assertIsSelected()
        compose.onNodeWithTag("business_bar_reservations").performClick()

        assertEquals(BusinessTab.RESERVATIONS, tab)
        compose.onNodeWithTag("business_bar_reservations").assertIsSelected()
        compose.onNodeWithTag("business_bar_panel").assertIsNotSelected()
    }

    @Test
    fun checkinSinCodigoDeshabilitaConfirmar() {
        var state by mutableStateOf(CheckinUiState())
        compose.setContent { WhereverTheme { CheckinContent(state, CheckinActions()) } }

        compose.onNodeWithTag("checkin_confirm").assertIsNotEnabled()
        state = state.copy(code = "WH-4821")
        compose.onNodeWithTag("checkin_confirm").assertIsEnabled()
    }

    @Test
    fun reservasMuestraLasFilasYSuEstado() {
        compose.setContent {
            WhereverTheme { BusinessReservationsContent(sampleReservationsState(), ReservationsActions()) }
        }

        compose.onNodeWithTag("business_reservations_tabs").assertIsDisplayed()
        compose.onNodeWithTag("reservation_1").assertIsDisplayed()
    }

    @Test
    fun reservasVaciasMuestranSuAccion() {
        compose.setContent {
            WhereverTheme {
                BusinessReservationsContent(sampleReservationsState().copy(items = emptyList()), ReservationsActions())
            }
        }

        compose.onNodeWithTag("business_reservations_empty").assertIsDisplayed()
    }
}
