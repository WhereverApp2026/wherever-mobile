package com.wherever.mobile.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = RouteBlue,
    onPrimary = Color.White,
    primaryContainer = RouteBlueTint,
    onPrimaryContainer = Ink,
    secondary = Sunset,
    onSecondary = Ink,
    tertiary = Mint,
    onTertiary = Color.White,
    tertiaryContainer = MintTint,
    onTertiaryContainer = Ink,
    background = Mist,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    onSurfaceVariant = InkMuted,
    surfaceVariant = SegmentTrack,
    outline = OutlineSoft,
    error = Danger,
    onError = Color.White,
    errorContainer = DangerTint,
    onErrorContainer = Ink
)

private val DarkColors = darkColorScheme(
    primary = RouteBlueTint,
    onPrimary = Ink,
    primaryContainer = InkDeep,
    onPrimaryContainer = Mist,
    secondary = Sunset,
    onSecondary = Ink,
    tertiary = Mint,
    onTertiary = Color.White,
    tertiaryContainer = InkDeep,
    onTertiaryContainer = Mist,
    background = Ink,
    onBackground = Mist,
    surface = InkDeep,
    onSurface = Mist,
    onSurfaceVariant = OutlineSoft,
    surfaceVariant = InkDeep,
    outline = InkMuted,
    error = DangerTint,
    onError = Ink,
    errorContainer = Danger,
    onErrorContainer = Color.White
)

private const val DARK_LUMINANCE = 0.5f

@Composable
fun WhereverTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = WhereverTypography
    ) {
        // Sin esto, el texto fuera de un Surface sale negro también en tema oscuro.
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            LocalMotionEnabled provides rememberSystemMotionEnabled(),
            content = content
        )
    }
}

/** Íconos de las barras del sistema. Los fija el tema, y [lightStatusIcons] para encabezados oscuros. */
@Composable
fun SystemBarIcons(lightStatusIcons: Boolean) {
    val view = LocalView.current
    val darkBackground = MaterialTheme.colorScheme.background.luminance() < DARK_LUMINANCE
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !lightStatusIcons && !darkBackground
                isAppearanceLightNavigationBars = !darkBackground
            }
        }
    }
}
