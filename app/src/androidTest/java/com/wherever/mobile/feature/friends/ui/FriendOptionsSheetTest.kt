package com.wherever.mobile.feature.friends.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.friends.domain.PersonDetail
import com.wherever.mobile.feature.friends.domain.Relation
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * nv-20 (UI): contenido de 27_AmigoOpc; silencia con el interruptor y manda a confirmar eliminar y bloquear.
 * La hoja modal se prueba a mano en el teléfono (en el Xiaomi la prueba con la ventana modal se cuelga).
 */
@RunWith(AndroidJUnit4::class)
class FriendOptionsSheetTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun silenciarYOpcionesDestructivas() {
        val events = mutableListOf<String>()
        var detail by mutableStateOf(PersonDetail(Person("u1", "Ana López", "ana_l", null), Relation.FRIEND))
        compose.setContent {
            WhereverTheme {
                FriendOptionsContent(
                    detail = detail,
                    working = false,
                    error = null,
                    actions = FriendOptionsActions(
                        onMute = { muted ->
                            events += "mute:$muted"
                            detail = detail.copy(muted = muted)
                        },
                        onRemove = { events += "remove" },
                        onBlock = { events += "block" },
                        onDismiss = { events += "dismiss" }
                    )
                )
            }
        }

        compose.onNodeWithText("Ana López").assertIsDisplayed()
        compose.onNodeWithTag("person_mute").assertIsOff().performClick()
        compose.onNodeWithTag("person_mute").assertIsOn()
        compose.onNodeWithText(context.getString(R.string.friends_remove)).performClick()
        compose.onNodeWithText(context.getString(R.string.friends_block)).performClick()

        assertEquals(listOf("mute:true", "remove", "block"), events)
    }
}
