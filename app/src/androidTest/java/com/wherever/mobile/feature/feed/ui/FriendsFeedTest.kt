package com.wherever.mobile.feature.feed.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.friends.domain.FriendSuggestion
import com.wherever.mobile.feature.friends.domain.Person
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-17 (UI): pestaña Amigos de Inicio vacía, con sugerencias, y Agregar → Pendiente. */
@RunWith(AndroidJUnit4::class)
class FriendsFeedTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val luis = Person("u2", "Luis Mendoza", "luis", null)

    @Test
    fun vacioMuestraMensajeYAgregarAmigos() {
        var addFriends = 0
        compose.setContent {
            WhereverTheme {
                LazyColumn {
                    friendsFeedItems(
                        FriendsFeedUiState(loading = false),
                        actions(onAddFriends = { addFriends++ })
                    )
                }
            }
        }

        compose.onNodeWithTag("feed_empty").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.feed_empty_title)).assertIsDisplayed()
        compose.onNodeWithTag("feed_add_friends").performClick()
        assertEquals(1, addFriends)
    }

    @Test
    fun agregarSugerenciaPasaAPendiente() {
        val requested = mutableListOf<Person>()
        var state by mutableStateOf(
            FriendsFeedUiState(loading = false, suggestions = listOf(FriendSuggestion(luis, 3)))
        )
        compose.setContent {
            WhereverTheme {
                LazyColumn {
                    friendsFeedItems(
                        state,
                        actions(onRequest = { person ->
                            requested += person
                            state = state.copy(sent = state.sent + person.id)
                        })
                    )
                }
            }
        }

        compose.onNodeWithTag("suggestion_u2").assertIsDisplayed()
        compose.onNodeWithTag("suggestion_add_u2").performClick()

        assertEquals(listOf(luis), requested)
        // La tarjeta es clickable y fusiona la semántica de sus hijos: el texto vive en el árbol sin fusionar.
        compose.onNodeWithTag("suggestion_sent_u2", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.friends_request_pending)).assertIsDisplayed()
    }

    private fun actions(onAddFriends: () -> Unit = {}, onRequest: (Person) -> Unit = {}) = FriendsFeedActions(
        onMoment = {},
        onPlace = {},
        onPlan = {},
        onPerson = {},
        onAddFriends = onAddFriends,
        onRequest = onRequest,
        onRetry = {},
        onLoadMore = {}
    )
}
