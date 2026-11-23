package com.wherever.mobile.feature.business.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.Catalog
import com.wherever.mobile.feature.business.domain.Option
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-34 (UI): Continuar se habilita solo con el paso válido; el 422 de enviar-revision lista lo que falta. */
@RunWith(AndroidJUnit4::class)
class OnboardingContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val catalog = Catalog(listOf(Option("cafeteria", "Cafetería")), emptyList())

    @Test
    fun continuarSoloConElPasoValido() {
        var state by mutableStateOf(OnboardingUiState(loading = false, catalog = catalog))
        compose.setContent { WhereverTheme { OnboardingContent(state, OnboardingActions()) } }

        compose.onNodeWithTag("business_next").assertIsNotEnabled()
        state = state.copy(name = "Café Raíz", category = "cafeteria")
        compose.onNodeWithTag("business_next").assertIsEnabled()
    }

    @Test
    fun faltantesDelEnvioSeListan() {
        val state = OnboardingUiState(
            loading = false,
            step = OnboardingStep.VERIFY,
            catalog = catalog,
            missing = listOf("Faltan 2 fotos", "Falta el comprobante de domicilio")
        )
        compose.setContent { WhereverTheme { OnboardingContent(state, OnboardingActions()) } }

        compose.onNodeWithTag("business_missing").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Faltan 2 fotos", substring = true).assertIsDisplayed()
    }
}
