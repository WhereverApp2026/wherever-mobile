package com.wherever.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.notifications.PushDestination
import com.wherever.mobile.feature.status.ui.AppLockOverlay
import com.wherever.mobile.feature.status.ui.AppLockViewModel
import com.wherever.mobile.feature.status.ui.AppStatusOverlay
import com.wherever.mobile.feature.status.ui.AppStatusViewModel
import com.wherever.mobile.feature.status.ui.IntegrityOverlay
import com.wherever.mobile.feature.status.ui.IntegrityViewModel
import com.wherever.mobile.navigation.InAppPushBanner
import com.wherever.mobile.navigation.MainViewModel
import com.wherever.mobile.navigation.WhereverNavHost
import dagger.hilt.android.AndroidEntryPoint

// FragmentActivity (subclase de ComponentActivity): la necesita BiometricPrompt (constitución E-05).
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val status: AppStatusViewModel by viewModels()
    private val integrity: IntegrityViewModel by viewModels()
    private val appLock: AppLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { viewModel.start.value == null }
        // Tocar un push abre esta actividad con `data` como extras (specs/nv-46).
        if (savedInstanceState == null) {
            openPush(intent)
            viewModel.onLinkOpened(intent?.dataString)
        }
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
                val banner by viewModel.banner.collectAsStateWithLifecycle()
                val appStatus by status.state.collectAsStateWithLifecycle()
                val integrityState by integrity.state.collectAsStateWithLifecycle()
                val locked by appLock.locked.collectAsStateWithLifecycle()
                Box(Modifier.fillMaxSize()) {
                    start?.let { WhereverNavHost(it, viewModel.events, viewModel.pushes, viewModel.links) }
                    InAppPushBanner(banner, viewModel::onBannerOpen, viewModel::onBannerLater)
                    // 89_SinConexion, mantenimiento y versión mínima (specs/nv-16) quedan sobre todo lo demás.
                    AppStatusOverlay(appStatus, status::retry, status::dismiss)
                    // Root, depurador o app alterada: sobre todo, incluso Sin conexión.
                    // Desbloqueo con huella o PIN (opcional, Ajustes): tapa la app hasta identificarse.
                    AppLockOverlay(locked, appLock::unlocked)
                    IntegrityOverlay(integrityState, integrity::dismissWarning)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onForeground(true)
        appLock.onForeground()
    }

    override fun onStop() {
        viewModel.onForeground(false)
        appLock.onBackground()
        super.onStop()
    }

    // `singleTask`: con la app abierta, el push tocado llega aquí en lugar de crear otra actividad.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openPush(intent)
        viewModel.onLinkOpened(intent.dataString)
    }

    private fun openPush(intent: Intent?) {
        val extras = intent?.extras ?: return
        viewModel.onPushOpened(PushDestination.KEYS.associateWith { extras.getString(it) })
    }
}
