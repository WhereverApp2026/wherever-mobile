package com.wherever.mobile.feature.help.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.help.domain.Faq
import com.wherever.mobile.feature.help.domain.SupportContacts
import com.wherever.mobile.feature.help.domain.matching
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private val FAQS = listOf(
    Faq(0, "¿Cómo cancelo una reservación?", "Entra a Perfil y abre Mis reservas."),
    Faq(1, "¿Qué es el Sello de Excelencia?", "Es una insignia de Wherever.")
)

/** nv-33 (UI): el acordeón abre y cierra; sin resultados muestra los contactos. */
@RunWith(AndroidJUnit4::class)
class HelpContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private var state by mutableStateOf(HelpUiState(faqs = FAQS, contacts = SupportContacts(null, null)))
    private val events = mutableListOf<String>()

    private fun show() {
        compose.setContent {
            WhereverTheme {
                HelpContent(
                    state,
                    HelpActions(
                        onBack = { events += "back" },
                        onQuery = { state = state.copy(query = it, faqs = FAQS.matching(it)) },
                        onToggleFaq = { id ->
                            state = state.copy(expandedId = if (state.expandedId == id) null else id)
                        },
                        onWhatsApp = { events += "whatsapp:$it" },
                        onEmail = { events += "email:$it" },
                        onReport = { events += "report" }
                    )
                )
            }
        }
    }

    @Test
    fun elAcordeonAbreYCierraLaRespuesta() {
        show()
        compose.onNodeWithTag("help_faq_answer_0").assertDoesNotExist()

        compose.onNodeWithText(FAQS[0].question).performClick()
        compose.onNodeWithTag("help_faq_answer_0").assertIsDisplayed()

        compose.onNodeWithText(FAQS[0].question).performClick()
        compose.onNodeWithTag("help_faq_answer_0").assertDoesNotExist()
    }

    @Test
    fun sinResultadosMuestraElAvisoYLosContactos() {
        show()
        compose.onNodeWithTag("help_search").performTextInput("mascotas")

        compose.onNodeWithTag("help_no_results").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.help_no_results)).assertIsDisplayed()
        compose.onNodeWithTag("help_report").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("help_faq_0").assertDoesNotExist()
    }

    @Test
    fun sinDatosDeContactoSoloQuedaReportar() {
        show()
        compose.onNodeWithTag("help_whatsapp").assertDoesNotExist()
        compose.onNodeWithTag("help_email").assertDoesNotExist()

        compose.onNodeWithTag("help_report").performScrollTo().performClick()
        assertEquals(listOf("report"), events)
    }

    @Test
    fun conDatosDeContactoAbreCorreoYWhatsApp() {
        state = state.copy(contacts = SupportContacts(email = "ayuda@ejemplo.com", whatsapp = "+52 55 0000 0000"))
        show()

        compose.onNodeWithTag("help_whatsapp").performScrollTo().performClick()
        compose.onNodeWithTag("help_email").performScrollTo().performClick()
        assertEquals(listOf("whatsapp:+52 55 0000 0000", "email:ayuda@ejemplo.com"), events)
    }
}
