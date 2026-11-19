package com.wherever.mobile.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * nv-49 (UI): con poca altura (teléfono en horizontal o teclado abierto) el pie entra al scroll y el
 * campo de arriba sigue a la vista; con altura normal el pie queda fijo.
 */
@RunWith(AndroidJUnit4::class)
class FormScreenCompactTest {
    @get:Rule
    val compose = createComposeRule()

    private fun setForm(heightDp: Int) = compose.setContent {
        WhereverTheme {
            Box(Modifier.width(700.dp).height(heightDp.dp)) {
                FormScreen(footer = { WhereverPrimaryButton("x", {}, tag = "form_footer") }) {
                    TitleBlock("t")
                    LabeledTextField("l", "", {}, tag = "form_field")
                }
            }
        }
    }

    @Test
    fun conPocaAlturaElCampoSeVeYElPieSeAlcanzaConScroll() {
        setForm(heightDp = 360)

        compose.onNodeWithTag("form_field").assertIsDisplayed()
        compose.onNodeWithTag("form_footer").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun conAlturaNormalElPieEstaFijo() {
        setForm(heightDp = 800)

        compose.onNodeWithTag("form_footer").assertIsDisplayed()
        compose.onNodeWithTag("form_field").assertIsDisplayed()
    }
}
