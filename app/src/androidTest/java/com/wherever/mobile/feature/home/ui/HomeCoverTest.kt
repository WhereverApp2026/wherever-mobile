package com.wherever.mobile.feature.home.ui

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.places.domain.PlaceCard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Boceto 2, 10_Inicio: buscador con chispa de IA y tarjetas-foto de "Cerca de ti". */
@RunWith(AndroidJUnit4::class)
class HomeCoverTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun buscadorAbreBuscarYLaChispaArmaUnPlanConIa() {
        val events = mutableListOf<String>()
        compose.setContent {
            WhereverTheme {
                HomeCover(
                    state = HomeUiState(loading = false, firstName = "Osmar", city = "Tula de Allende"),
                    unread = 0,
                    onSearch = { events += "search" },
                    onAiPlan = { events += "ai" },
                    onNotifications = { events += "bell" }
                )
            }
        }

        compose.onNodeWithTag("home_city").assertIsDisplayed()
        compose.onNodeWithTag("home_search").performClick()
        compose.onNodeWithTag("home_ai_plan").performClick()
        compose.onNodeWithTag("home_notifications").performClick()

        assertEquals(listOf("search", "ai", "bell"), events)
    }

    @Test
    fun tarjetaAbreElLugarYMantenerPresionadoEsNoMeInteresa() {
        val events = mutableListOf<String>()
        val place = PlaceCard("p1", "Café Raíz", "cafeteria", 2, 120, 4.8, 120, null, 640.0, true)
        compose.setContent {
            WhereverTheme {
                NearbyCarousel(
                    places = listOf(place),
                    onPlace = { events += "open:$it" },
                    onNotInterested = { events += "hide:${it.id}" }
                )
            }
        }

        compose.onNodeWithTag("home_place_p1").performClick()
        compose.onNodeWithTag("home_place_p1").performSemanticsAction(SemanticsActions.OnLongClick)

        assertEquals(listOf("open:p1", "hide:p1"), events)
    }
}
