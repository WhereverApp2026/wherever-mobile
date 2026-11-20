package com.wherever.mobile.core.designsystem

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

// Componentes únicos de diálogo, botones de texto e ícono, avisos y progreso (docs/diseno.md §4).
// Las pantallas no usan los de Material directamente: ArchitectureTest lo comprueba.

/** Diálogo de la app: misma forma, colores y botones en todas las pantallas. */
@Composable
fun WhereverDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        title = title,
        text = text,
        shape = WhereverShapes.Card,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Botón de texto con área táctil de 48 dp. */
@Composable
fun WhereverTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = Sizes.TouchTarget),
        enabled = enabled,
        content = content
    )
}

/** Botón de ícono con área táctil de 48 dp. El ícono de dentro lleva su `contentDescription`. */
@Composable
fun WhereverIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.sizeIn(minWidth = Sizes.TouchTarget, minHeight = Sizes.TouchTarget),
        enabled = enabled,
        content = content
    )
}

/** Avisos breves (snackbar) con la forma y los colores del tema. */
@Composable
fun WhereverSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            shape = WhereverShapes.Field,
            containerColor = Ink,
            contentColor = Mist,
            actionColor = Sunset
        )
    }
}

/** Indicador de carga dentro de un control o una tarjeta (para la pantalla entera: [LoadingState]). */
@Composable
fun InlineProgress(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    CircularProgressIndicator(
        modifier = modifier.size(Sizes.ProgressSmall),
        color = color,
        strokeWidth = Strokes.Thin
    )
}

/** Indicador de carga grande, para esperas largas como generar un plan. */
@Composable
fun LargeProgress(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    CircularProgressIndicator(modifier = modifier.size(Sizes.ProgressLarge), color = color, strokeWidth = Strokes.Thick)
}

/** Barra de progreso: alto y forma únicos en toda la app. */
@Composable
fun WhereverProgressBar(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier.height(Sizes.BarHeight).clip(WhereverShapes.Pill),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round,
        gapSize = Spacing.None,
        drawStopIndicator = {}
    )
}
