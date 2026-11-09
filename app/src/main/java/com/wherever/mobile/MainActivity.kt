package com.wherever.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.navigation.MainViewModel
import com.wherever.mobile.navigation.WhereverNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { viewModel.start.value == null }
        enableEdgeToEdge()
        setContent {
            val theme by viewModel.theme.collectAsStateWithLifecycle()
            val start by viewModel.start.collectAsStateWithLifecycle()
            val dark = when (theme) {
                "claro" -> false
                "oscuro" -> true
                else -> isSystemInDarkTheme()
            }
            WhereverTheme(darkTheme = dark) {
                start?.let { WhereverNavHost(it, viewModel.events) }
            }
        }
    }
}
