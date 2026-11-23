package com.wherever.mobile.feature.groups.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.plans.domain.PlanSummary
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-25 (UI): 48_Votar, 46_Grupo, 43_CrearGrupo e InvitarPlan. */
@RunWith(AndroidJUnit4::class)
class GroupFlowsContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun votarEligeYEnviaYElOrganizadorCierra() {
        val events = mutableListOf<String>()
        var state by mutableStateOf(
            VoteUiState(
                loading = false,
                votes = PREVIEW_VOTES,
                iAmOrganizer = true,
                now = Instant.parse("2026-11-23T17:30:00Z")
            )
        )
        compose.setContent {
            WhereverTheme {
                VoteContent(
                    state,
                    VoteActions(
                        onSelect = { vote, place ->
                            state = state.copy(selected = state.selected + (vote.id to place))
                        },
                        onSend = { events += "send:${it.id}" },
                        onClose = { events += "close:${it.id}" }
                    )
                )
            }
        }

        // Mi voto ya es n1: no se reenvía lo mismo.
        compose.onNodeWithTag("vote_send_v1").assertIsNotEnabled()
        compose.onNodeWithTag("vote_option_n2").performClick()
        compose.onNodeWithTag("vote_send_v1").assertIsEnabled().performClick()
        compose.onNodeWithText(context.getString(R.string.vote_close)).performClick()
        assertEquals(listOf("send:v1", "close:v1"), events)
        // La cerrada no tiene botón de votar y marca a la ganadora.
        compose.onNodeWithText(context.getString(R.string.vote_winner)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun grupoDelOrganizadorInvitaYSacaYElIntegranteSale() {
        val events = mutableListOf<String>()
        var state by mutableStateOf(
            GroupDetailUiState(loading = false, group = PREVIEW_GROUPS.first().copy(iAmOrganizer = true))
        )
        compose.setContent {
            WhereverTheme {
                GroupDetailContent(
                    state,
                    GroupDetailNavigation(
                        onVotes = { events += "votes:$it" },
                        onInvite = { plan, group -> events += "invite:$plan:$group" }
                    ),
                    GroupDetailActions(
                        onLeave = { events += "leave" },
                        onRemove = { events += "remove:${it.userId}" }
                    )
                )
            }
        }
        val group = state.group!!
        val other = group.members.first { !it.organizer }

        compose.onNodeWithTag("group_votes").performClick()
        compose.onNodeWithTag("member_remove_${other.userId}").performScrollTo().performClick()
        compose.onNodeWithTag("group_invite").performClick()
        assertEquals(
            listOf("votes:${group.id}", "remove:${other.userId}", "invite:${group.planId}:${group.id}"),
            events
        )

        state = state.copy(group = group.copy(iAmOrganizer = false))
        compose.onNodeWithTag("member_remove_${other.userId}").assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.group_detail_leave)).performClick()
        assertEquals("leave", events.last())
    }

    @Test
    fun crearGrupoSoloConAmigosElegidos() {
        var state by mutableStateOf(
            CreateGroupUiState(
                loading = false,
                planName = "Domingo",
                friends = listOf(Person("u1", "Ana", null, null), Person("u2", "Luis", null, null)),
                alreadyIn = setOf("u2"),
                inviting = true
            )
        )
        compose.setContent {
            WhereverTheme {
                CreateGroupContent(
                    state,
                    CreateGroupActions(
                        onToggle = { p ->
                            state = state.copy(
                                selected = if (p.id in state.selected) state.selected - p.id else state.selected + p.id
                            )
                        }
                    )
                )
            }
        }

        compose.onNodeWithTag("create_group_submit").assertIsNotEnabled()
        // Quien ya está en el grupo no se puede elegir: su fila no tiene acción.
        compose.onNodeWithTag("person_u2").performScrollTo().assertHasNoClickAction()
        compose.onNodeWithTag("person_u1").performScrollTo().performClick()
        compose.onNodeWithTag("create_group_submit").assertIsEnabled()
    }

    @Test
    fun invitarAUnPlanEligeEnviaYAbreElGrupo() {
        val events = mutableListOf<String>()
        var state by mutableStateOf(
            InvitePlanUiState(
                personName = "Mariana",
                loading = false,
                plans = listOf(PlanSummary("p1", "Domingo", "2026-11-29", stops = 2, costPerPerson = 300))
            )
        )
        compose.setContent {
            WhereverTheme {
                InvitePlanContent(
                    state,
                    InvitePlanActions(
                        onSelect = { state = state.copy(selected = it) },
                        onSend = { state = state.copy(sentToGroup = "g1") },
                        onGroup = { events += "group:$it" }
                    )
                )
            }
        }

        compose.onNodeWithTag("invite_plan_send").assertIsNotEnabled()
        compose.onNodeWithTag("invite_plan_p1").performClick()
        compose.onNodeWithTag("invite_plan_send").assertIsEnabled().performClick()
        compose.onNodeWithTag("invite_plan_sent").assertIsDisplayed()
        compose.onNodeWithTag("invite_plan_group").performClick()
        assertEquals(listOf("group:g1"), events)
    }
}
