package com.wherever.mobile.feature.places.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.places.domain.PlaceCard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-47 (UI): la tarjeta de lugar muestra giro, abierto y calificación, y abre la ficha al tocarla. */
@RunWith(AndroidJUnit4::class)
class PlaceCardTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val place = PlaceCard(
        id = "p1",
        name = "Café Andrade",
        category = "cafeteria",
        priceLevel = 2,
        averagePrice = 150,
        rating = 4.5,
        reviews = 12,
        coverUrl = null,
        distanceMeters = 820.0,
        openNow = false
    )

    @Test
    fun muestraDatosYAbreLaFicha() {
        var opened = 0
        compose.setContent { WhereverTheme { PlaceCardItem(place, onClick = { opened++ }) } }

        compose.onNodeWithText("Café Andrade").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.place_closed)).assertIsDisplayed()
        compose.onNodeWithText(
            "${context.getString(R.string.category_cafe)} · $$ · 820 m",
            substring = true
        ).assertIsDisplayed()
        compose.onNodeWithTag("place_card_p1").performClick()

        assertEquals(1, opened)
    }
}
