package com.wherever.mobile.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Boceto 2 §4.3: barra de 5 con "+" y hoja 15_Crear. La hoja (ModalBottomSheet) se cuelga en pruebas en
 * MIUI, así que su contenido se prueba con [CreateOptionsList] y la hoja se revisa a mano en el teléfono.
 */
@RunWith(AndroidJUnit4::class)
class BottomBarCreateTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun laBarraNoSeEstiraConTodaLaPantallaDisponibleYMuestraElMas() {
        compose.setContent {
            WhereverTheme {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    WhereverBottomBar(selected = MainTab.HOME, onSelect = {}, onCreate = {})
                }
            }
        }

        val height = compose.onNodeWithTag("bottom_bar").getUnclippedBoundsInRoot().height
        assertTrue("alto de la barra: $height", height < 120.dp)
        compose.onNodeWithTag("bottom_bar_create").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun soloLasOpcionesConPantallaResponden() {
        val chosen = mutableListOf<CreateOption>()
        compose.setContent { WhereverTheme { CreateOptionsList(onSelect = { chosen += it }) } }

        compose.onNodeWithTag("create_option_ai_plan").performClick()
        compose.onNodeWithTag("create_option_with_friends").performClick()
        compose.onNodeWithTag("create_option_moment").performClick()
        compose.onNodeWithTag("create_option_review").performClick()
        // nv-24: «Crear un plan a mano» ya tiene pantalla (CrearPlan).
        compose.onNodeWithTag("create_option_manual_plan").performClick()

        assertEquals(
            listOf(
                CreateOption.AI_PLAN,
                CreateOption.WITH_FRIENDS,
                CreateOption.MOMENT,
                CreateOption.REVIEW,
                CreateOption.MANUAL_PLAN
            ),
            chosen
        )
    }
}
