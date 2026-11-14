package com.wherever.mobile.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Serializable
private data object FirstTestRoute

@Serializable
private data object SecondTestRoute

/**
 * nv-45 (UI): un doble toque no abre la pantalla dos veces, y el botón atrás regresa una sola vez
 * aunque se pulse dos veces seguidas. Usa las transiciones reales de la app.
 */
@RunWith(AndroidJUnit4::class)
class SafeNavigationUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: NavHostController

    private fun setUpHost() {
        compose.setContent {
            WhereverTheme {
                navController = rememberNavController()
                NavHost(navController = navController, startDestination = FirstTestRoute) {
                    composable<FirstTestRoute> { e ->
                        Button(
                            onClick = { navController.navigateFrom(e, SecondTestRoute) },
                            modifier = Modifier.testTag("go")
                        ) { Text("go") }
                    }
                    composable<SecondTestRoute> { e ->
                        Button(onClick = { navController.popFrom(e) }, modifier = Modifier.testTag("back")) {
                            Text("back")
                        }
                    }
                }
            }
        }
    }

    private fun stackSize(): Int = navController.currentBackStack.value.count { it.destination.route != null }

    @Test
    fun doubleTapOpensTheScreenOnce() {
        setUpHost()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("go").performClick()
        compose.onNodeWithTag("go").performClick()
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        assertEquals(2, stackSize())
        compose.onNodeWithTag("back").assertExists()
    }

    @Test
    fun doubleBackReturnsOnce() {
        setUpHost()
        compose.onNodeWithTag("go").performClick()
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("back").performClick()
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        assertEquals(1, stackSize())
        compose.onNodeWithTag("go").assertExists()
    }

    @Test
    fun systemBackReturnsToPreviousScreen() {
        setUpHost()
        compose.onNodeWithTag("go").performClick()
        compose.waitForIdle()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
        assertEquals(1, stackSize())
        compose.onNodeWithTag("go").assertExists()
    }
}
