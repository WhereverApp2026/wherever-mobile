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
    secondary = Sunset,
    onSecondary = Ink,
    tertiary = Mint,
    onTertiary = Color.White,
    background = Mist,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink
)

private val DarkColors = darkColorScheme(
    primary = RouteBlue,
    onPrimary = Color.White,
    secondary = Sunset,
    onSecondary = Ink,
    tertiary = Mint,
    onTertiary = Color.White,
    background = Ink,
    onBackground = Mist,
    surface = Ink,
    onSurface = Mist
)

@Composable
fun WhereverTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = WhereverTypography,
        content = content
    )
}
