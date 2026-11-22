package com.wherever.mobile.feature.auth.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Boceto 2: "Continuar con Google" se ve en Login y, mientras la API no lo tenga, avisa que llega pronto. */
@RunWith(AndroidJUnit4::class)
class GoogleSignInBlockTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun alTocarGoogleAvisaQueLlegaPronto() {
        compose.setContent { WhereverTheme { GoogleSignInBlock(tag = "login_google") } }

        compose.onNodeWithTag("login_google_soon").assertDoesNotExist()
        compose.onNodeWithTag("login_google").assertIsDisplayed().performClick()
        compose.onNodeWithTag("login_google_soon").assertIsDisplayed()
    }
}
