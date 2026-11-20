package com.wherever.mobile.core.designsystem

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.wherever.mobile.R

private const val PULSE_MS = 900
private const val PULSE_LOW = 0.45f
private const val TITLE_FRACTION = 0.6f
private const val SUBTITLE_FRACTION = 0.4f

/**
 * Esqueleto de una lista mientras carga: reserva el espacio de las filas para que no salte el diseño al llegar
 * los datos. Lleva el mismo `testTag` que [LoadingState].
 */
@Composable
fun ListSkeleton(modifier: Modifier = Modifier, rows: Int = 5) {
    val alpha = skeletonAlpha()
    val loading = stringResource(R.string.common_loading)
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.L),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding, vertical = Spacing.L)
            .alpha(alpha)
            .semantics { contentDescription = loading }
            .testTag("common_loading")
    ) {
        repeat(rows) { SkeletonRow() }
    }
}

@Composable
private fun SkeletonRow() {
    val shade = MaterialTheme.colorScheme.surfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(Sizes.AvatarList).clip(WhereverShapes.Pill).background(shade))
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.S),
            modifier = Modifier.padding(start = Spacing.M).fillMaxWidth()
        ) {
            Box(
                Modifier.fillMaxWidth(TITLE_FRACTION).height(Sizes.SkeletonLineL).clip(WhereverShapes.Small)
                    .background(shade)
            )
            Box(
                Modifier.fillMaxWidth(SUBTITLE_FRACTION).height(Sizes.SkeletonLineS).clip(WhereverShapes.Small)
                    .background(shade)
            )
        }
    }
}

/** Pulso suave; quieto si el sistema tiene las animaciones apagadas. */
@Composable
private fun skeletonAlpha(): Float {
    if (!LocalMotionEnabled.current) return 1f
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = PULSE_LOW,
        animationSpec = infiniteRepeatable(tween(PULSE_MS), RepeatMode.Reverse),
        label = "skeleton_alpha"
    )
    return alpha
}
