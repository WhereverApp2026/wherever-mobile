package com.wherever.mobile.feature.business.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-38 (UI): Eliminar negocio se habilita solo con el nombre exacto; la fila de cobros navega. */
@RunWith(AndroidJUnit4::class)
class BusinessProfileContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun eliminarSoloConElNombreExacto() {
        val profile = sampleBusinessProfile()
        var state by mutableStateOf(BusinessProfileUiState(loading = false, profile = profile, deleteOpen = true))
        compose.setContent {
            WhereverTheme { Column { DeleteBusinessForm(state, profile.name, BusinessProfileActions()) } }
        }

        compose.onNodeWithTag("biz_delete_confirm").assertIsNotEnabled()
        state = state.copy(deleteText = profile.name.lowercase())
        compose.onNodeWithTag("biz_delete_confirm").assertIsNotEnabled()
        state = state.copy(deleteText = profile.name)
        compose.onNodeWithTag("biz_delete_confirm").assertIsEnabled()
    }

    @Test
    fun filaDeCobrosAbreCobros() {
        var opened = false
        compose.setContent {
            WhereverTheme {
                BusinessProfileContent(
                    BusinessProfileUiState(loading = false, profile = sampleBusinessProfile()),
                    ProfileNavigation(onPayments = { opened = true }),
                    BusinessProfileActions()
                )
            }
        }

        compose.onNodeWithTag("biz_profile_payments_row").performScrollTo().performClick()
        assertTrue(opened)
    }
}
