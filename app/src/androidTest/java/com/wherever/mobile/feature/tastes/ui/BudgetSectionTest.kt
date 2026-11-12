package com.wherever.mobile.feature.tastes.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.WhereverTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * nv-44 (UI): barra y campos del presupuesto sincronizados. Usa el `BudgetForm` real, sin ViewModel,
 * y busca los nodos por `testTag` y por textos de `strings.xml`.
 */
@RunWith(AndroidJUnit4::class)
class BudgetSectionTest {
    @get:Rule
    val compose = createComposeRule()

    private var form by mutableStateOf(BudgetForm())
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        form = BudgetForm()
        compose.setContent {
            WhereverTheme(darkTheme = false) {
                BudgetSection(
                    form = form,
                    actions = BudgetActions(
                        onMin = { form = form.withMinText(it) },
                        onMax = { form = form.withMaxText(it) },
                        onSlide = { start, end -> form = form.slide(start, end) },
                        onSlideFinished = { form = form.finishSlide() }
                    )
                )
            }
        }
    }

    /** Los dos extremos de la barra: el primero es el mínimo y el segundo el máximo. */
    private fun thumb(index: Int): SemanticsNodeInteraction =
        compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress))[index]

    private fun SemanticsNodeInteraction.progress(): Float =
        fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current

    @Test
    fun escribir_en_los_campos_mueve_la_barra() {
        compose.onNodeWithTag("tastes_budget_min").performTextReplacement("200")
        compose.onNodeWithTag("tastes_budget_max").performTextReplacement("750")
        compose.waitForIdle()

        assertEquals(200f, thumb(0).progress())
        assertEquals(750f, thumb(1).progress())
    }

    @Test
    fun mover_la_barra_cambia_el_campo() {
        thumb(1).performSemanticsAction(SemanticsActions.SetProgress) { it(600f) }
        compose.waitForIdle()

        compose.onNodeWithTag("tastes_budget_max").assertTextContains("600")
    }

    @Test
    fun mas_de_1000_deja_la_barra_al_tope_y_conserva_lo_escrito() {
        compose.onNodeWithTag("tastes_budget_max").performTextReplacement("2500")
        compose.waitForIdle()

        assertEquals(1000f, thumb(1).progress())
        compose.onNodeWithTag("tastes_budget_max").assertTextContains("2500")
    }

    @Test
    fun las_letras_se_descartan() {
        compose.onNodeWithTag("tastes_budget_min").performTextReplacement("1a2b0")
        compose.waitForIdle()

        compose.onNodeWithTag("tastes_budget_min").assertTextContains("120")
    }

    @Test
    fun minimo_mayor_que_maximo_muestra_el_error() {
        compose.onNodeWithTag("tastes_budget_min").performTextReplacement("800")
        compose.waitForIdle()

        compose.onNode(hasText(context.getString(R.string.tastes_budget_min_greater))).assertExists()
    }

    @Test
    fun campo_vacio_muestra_el_error() {
        compose.onNodeWithTag("tastes_budget_max").performTextReplacement("")
        compose.waitForIdle()

        compose.onNode(hasText(context.getString(R.string.tastes_budget_empty))).assertExists()
    }
}
