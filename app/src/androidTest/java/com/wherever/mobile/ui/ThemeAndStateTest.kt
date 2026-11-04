package com.wherever.mobile.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.designsystem.components.WhereverContentState
import com.wherever.mobile.core.designsystem.components.WhereverStateView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeAndStateTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun themeUsesBrandPrimaryColor() {
        var primary: Color? = null
        composeRule.setContent {
            WhereverTheme {
                primary = MaterialTheme.colorScheme.primary
            }
        }

        composeRule.runOnIdle {
            assertEquals(RouteBlue, primary)
        }
    }

    @Test
    fun rendersLoadingState() {
        composeRule.setContent {
            WhereverTheme {
                WhereverStateView(WhereverContentState.Loading)
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.state_loading))
            .assertIsDisplayed()
    }

    @Test
    fun rendersErrorStateAndRetries() {
        var retried = false
        composeRule.setContent {
            WhereverTheme {
                WhereverStateView(
                    state = WhereverContentState.Error(message = "Fallo de prueba"),
                    onRetry = { retried = true }
                )
            }
        }

        composeRule.onNodeWithText("Fallo de prueba").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.action_retry)).performClick()
        composeRule.runOnIdle {
            assertTrue(retried)
        }
    }
}
