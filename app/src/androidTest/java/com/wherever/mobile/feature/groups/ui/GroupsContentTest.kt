package com.wherever.mobile.feature.groups.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.groups.domain.ChatMessage
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.groups.domain.MessageStatus
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-25 (UI): 44_MisGrupos con invitación y vacío; 45_GrupoChat con envío, fallido y sin red. */
@RunWith(AndroidJUnit4::class)
class GroupsContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun misGruposAbreElChatYRespondeLaInvitacion() {
        val events = mutableListOf<String>()
        compose.setContent {
            WhereverTheme {
                MyGroupsContent(
                    MyGroupsUiState(loading = false, groups = PREVIEW_GROUPS),
                    now = Instant.parse("2026-11-20T18:00:00Z"),
                    actions = MyGroupsActions(
                        onGroup = { events += "open:$it" },
                        onRespond = { group, accept -> events += "${group.id}:$accept" }
                    )
                )
            }
        }

        compose.onNodeWithText("Viernes de tacos").performClick()
        compose.onNodeWithText(context.getString(R.string.groups_status_invited)).assertIsDisplayed()
        compose.onNodeWithTag("group_accept_g3").performClick()
        compose.onNodeWithTag("group_decline_g3").performClick()
        assertEquals(listOf("open:g1", "g3:true", "g3:false"), events)
        assertEquals(MemberStatus.INVITED, PREVIEW_GROUPS.last().myStatus)
    }

    @Test
    fun misGruposVacioOfreceNuevoGrupo() {
        var created = 0
        compose.setContent {
            WhereverTheme {
                MyGroupsContent(
                    MyGroupsUiState(loading = false),
                    Instant.now(),
                    MyGroupsActions(onNewGroup = { created++ })
                )
            }
        }
        compose.onNodeWithTag("groups_empty").assertIsDisplayed()
        compose.onNodeWithTag("groups_empty_new").performClick()
        assertEquals(1, created)
    }

    @Test
    fun chatEnviaReintentaYAvisaSinRed() {
        val events = mutableListOf<String>()
        val failed = ChatMessage(
            "local:c1",
            "c1",
            null,
            null,
            null,
            false,
            "No salió",
            false,
            "2026-11-20T18:00:00Z",
            MessageStatus.FAILED
        )
        var state by mutableStateOf(
            GroupChatUiState(group = PREVIEW_GROUPS.first(), loading = false, myId = "yo", messages = listOf(failed))
        )
        var keyboard: SoftwareKeyboardController? = null
        compose.setContent {
            keyboard = LocalSoftwareKeyboardController.current
            WhereverTheme {
                GroupChatContent(
                    state,
                    GroupChatActions(
                        onDraft = { state = state.copy(draft = it) },
                        onSend = { events += "send:${state.draft}" },
                        onResend = { events += "retry:${it.clientId}" }
                    ),
                    zone = ZoneOffset.UTC
                )
            }
        }

        compose.onNodeWithTag("chat_send").assertIsNotEnabled()
        compose.onNodeWithTag("chat_input").performTextInput("Hola")
        compose.onNodeWithTag("chat_send").assertIsEnabled().performClick()
        // Con el teclado abierto, en un teléfono de pantalla baja la lista queda fuera de la vista.
        compose.runOnIdle { keyboard?.hide() }
        compose.onNodeWithText(context.getString(R.string.groups_chat_failed)).assertIsDisplayed()
        compose.onNodeWithTag("message_retry").performClick()
        assertEquals(listOf("send:Hola", "retry:c1"), events)

        state = state.copy(offline = true)
        compose.onNodeWithTag("chat_offline").assertIsDisplayed()
        state = state.copy(messages = emptyList(), offline = false)
        compose.onNodeWithTag("chat_empty").assertIsDisplayed()
    }
}
