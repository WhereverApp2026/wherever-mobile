package com.wherever.mobile.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.notifications.InAppPush
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** nv-18 (UI): 88_PushBanner muestra el push y responde a Ver y Más tarde. */
@RunWith(AndroidJUnit4::class)
class InAppPushBannerTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun muestraElPushYRespondeAVerYMasTarde() {
        var opened = 0
        var later = 0
        val push = InAppPush("Wherever", "Tienes una nueva solicitud de amistad", emptyMap())
        compose.setContent { WhereverTheme { InAppPushBanner(push, { opened++ }, { later++ }) } }

        compose.onNodeWithTag("in_app_push").assertIsDisplayed()
        compose.onNodeWithText("Tienes una nueva solicitud de amistad").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.in_app_push_open)).performClick()
        compose.onNodeWithText(context.getString(R.string.in_app_push_later)).performClick()

        assertEquals(1, opened)
        assertEquals(1, later)
    }
}
