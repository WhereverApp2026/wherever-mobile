package com.wherever.mobile.feature.profile.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.profile.domain.Badge
import com.wherever.mobile.feature.profile.domain.LevelProgress
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-14 (UI): 83_Insignias muestra el nivel, los puntos que faltan y las dos secciones. */
@RunWith(AndroidJUnit4::class)
class BadgesContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun muestraNivelYSecciones() {
        val state = BadgesUiState(
            loading = false,
            progress = LevelProgress.of(50, "Explorador"),
            earned = listOf(Badge("resenista", "Reseñista", "Publica tu primera reseña", true, 1, 1)),
            locked = listOf(Badge("catador_cafe", "Catador de café", "Haz check-in en 5 cafeterías", false, 2, 5))
        )
        compose.setContent { WhereverTheme { BadgesContent(state, onBack = {}) } }

        compose.onNodeWithTag("badges_level").assertIsDisplayed()
        compose.onNodeWithText("Explorador").assertIsDisplayed()
        compose.onNodeWithText(
            context.getString(R.string.badges_points_of_next, 50, 250, "Aventurero")
        ).assertIsDisplayed()
        compose.onNodeWithText("Reseñista").assertIsDisplayed()
        compose.onNodeWithTag("badge_catador_cafe").assertIsDisplayed()
        compose.onNodeWithTag("badge_progress_catador_cafe").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.badges_progress, 2, 5)).assertIsDisplayed()
    }
}
