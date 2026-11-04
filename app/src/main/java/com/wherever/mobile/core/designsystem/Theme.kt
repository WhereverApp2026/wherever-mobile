package com.wherever.mobile.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

@Composable
fun WhereverTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = WhereverTypography,
        content = content
    )
}
