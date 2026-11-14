package com.wherever.mobile

import android.content.Intent
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
import com.wherever.mobile.core.notifications.PushDestination
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
        // Tocar un push abre esta actividad con `data` como extras (specs/nv-46).
        if (savedInstanceState == null) openPush(intent)
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
                start?.let { WhereverNavHost(it, viewModel.events, viewModel.pushes) }
            }
        }
    }

    // `singleTask`: con la app abierta, el push tocado llega aquí en lugar de crear otra actividad.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openPush(intent)
    }

    private fun openPush(intent: Intent?) {
        val extras = intent?.extras ?: return
        viewModel.onPushOpened(PushDestination.KEYS.associateWith { extras.getString(it) })
    }
}
