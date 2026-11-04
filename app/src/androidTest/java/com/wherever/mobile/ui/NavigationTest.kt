package com.wherever.mobile.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.navigation.DebugConnectionRoute
import com.wherever.mobile.navigation.HomeRoute
import com.wherever.mobile.navigation.WhereverNavHost
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun startsAtHome() {
        composeRule.setContent {
            WhereverTheme {
                WhereverNavHost(
                    startDestination = HomeRoute,
                    debugScreen = { Text("debug") }
                )
            }
        }

        composeRule.onNodeWithText("Wherever").assertIsDisplayed()
    }

    @Test
    fun navigatesToDebugRoute() {
        lateinit var navController: NavHostController
        composeRule.setContent {
            WhereverTheme {
                navController = rememberNavController()
                WhereverNavHost(
                    navController = navController,
                    startDestination = HomeRoute,
                    debugScreen = { Text("debug") }
                )
            }
        }

        composeRule.runOnIdle {
            navController.navigate(route = DebugConnectionRoute)
        }
        composeRule.onNodeWithText("debug").assertIsDisplayed()
    }
}
