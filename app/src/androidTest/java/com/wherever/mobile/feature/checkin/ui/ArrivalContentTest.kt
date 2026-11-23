package com.wherever.mobile.feature.checkin.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-30 (UI): sin permiso se ofrecen la ubicación y el código; 26_CheckinOk lleva a la postal. */
@RunWith(AndroidJUnit4::class)
class ArrivalContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun sinPermisoOfreceUbicacionYCodigo() {
        val events = mutableListOf<String>()
        compose.setContent {
            WhereverTheme {
                ArrivalContent(
                    sampleArrivalState().copy(phase = ArrivalPhase.NEEDS_PERMISSION, checkin = null),
                    ArrivalActions(onUseLocation = { events += "ubicacion" }, onScan = { events += "codigo" })
                )
            }
        }

        compose.onNodeWithTag("arrival_permission").assertIsDisplayed()
        compose.onNodeWithTag("arrival_use_location").performClick()
        compose.onNodeWithTag("arrival_scan_alt").performClick()

        assertEquals(listOf("ubicacion", "codigo"), events)
    }

    @Test
    fun checkinOkLlevaALaPostal() {
        var postcard = false
        compose.setContent {
            WhereverTheme { ArrivalContent(sampleArrivalState(), ArrivalActions(onPostcard = { postcard = true })) }
        }

        compose.onNodeWithTag("arrival_ok").assertIsDisplayed()
        compose.onNodeWithTag("arrival_postcard").performClick()

        assertEquals(true, postcard)
    }
}
