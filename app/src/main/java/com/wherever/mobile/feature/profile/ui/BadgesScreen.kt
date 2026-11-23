package com.wherever.mobile.feature.profile.ui

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.InkDeep
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverProgressBar
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.profile.domain.Badge
import com.wherever.mobile.feature.profile.domain.LevelProgress
import com.wherever.mobile.feature.profile.domain.LevelStep

/** 83_Insignias (Figma `298:8945`): tarjeta de nivel, Ganadas y Por desbloquear en rejilla de 3. */
@Composable
fun BadgesRoute(onBack: () -> Unit, viewModel: BadgesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BadgesContent(state, onBack)
}

/** Sin estado, para la prueba de UI. */
@Composable
internal fun BadgesContent(state: BadgesUiState, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        TopTitleRow(stringResource(R.string.badges_title), onBack, Modifier.padding(ScreenPadding))
        state.error?.let { ErrorBanner(it.asString(), Modifier.padding(horizontal = ScreenPadding)) }
        if (state.loading && state.progress == null) {
            LoadingState()
            return@Column
        }
        LazyColumn(
            contentPadding = PaddingValues(ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.L),
            modifier = Modifier.testTag("badges_list")
        ) {
            state.progress?.let { progress -> item { LevelCard(progress, state.levelNumber, state.nextLevelNumber) } }
            item {
                Text(
                    stringResource(R.string.badges_earned_count, state.earned.size),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (state.earned.isEmpty()) {
                item { EmptyText(R.string.badges_earned_empty) }
            } else {
                state.earned.chunked(COLUMNS).forEach { row -> item { BadgeGridRow(row) } }
            }
            item { Text(stringResource(R.string.badges_locked), style = MaterialTheme.typography.titleMedium) }
            if (state.locked.isEmpty()) {
                item { EmptyText(R.string.badges_locked_empty) }
            } else {
                state.locked.chunked(COLUMNS).forEach { row -> item { BadgeGridRow(row) } }
            }
        }
    }
}

private const val COLUMNS = 3

@Composable
private fun LevelCard(progress: LevelProgress, number: Int?, nextNumber: Int?) {
    // Tarjeta de tinta en ambos temas, como en Figma (degradado #1B3A63 → #10243B).
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.S),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.Card))
            .background(Brush.verticalGradient(listOf(InkDeep, Ink)))
            .padding(Spacing.XL)
            .testTag("badges_level")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.badges_level_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = SOFT_WHITE)
                )
                Text(
                    progress.current.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            number?.let {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(Sizes.AvatarM).clip(CircleShape).background(Gold)
                ) {
                    Text(
                        it.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }
            }
        }
        WhereverProgressBar(
            progress = { progress.fraction },
            modifier = Modifier.fillMaxWidth().testTag("badges_progress"),
            color = Sunset
        )
        val next = progress.next
        Text(
            when {
                next == null -> stringResource(R.string.badges_max_level)
                next.name == progress.current.name && nextNumber != null ->
                    stringResource(R.string.badges_points_to_level, progress.points, next.minPoints, nextNumber)
                else -> stringResource(R.string.badges_points_of_next, progress.points, next.minPoints, next.name)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = SOFT_WHITE)
        )
    }
}

private const val SOFT_WHITE = 0.8f

@Composable
private fun EmptyText(@StringRes text: Int) {
    Text(stringResource(text), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun BadgeGridRow(badges: List<Badge>) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M), modifier = Modifier.fillMaxWidth()) {
        badges.forEach { BadgeCell(it, Modifier.weight(1f)) }
        repeat(COLUMNS - badges.size) { Spacer(Modifier.weight(1f)) }
    }
}

@Composable
private fun BadgeCell(badge: Badge, modifier: Modifier) {
    val description = stringResource(
        R.string.badges_cell_a11y,
        badge.name,
        stringResource(if (badge.earned) R.string.badges_earned else R.string.badges_locked)
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.XS),
        modifier = modifier
            .semantics { contentDescription = description }
            .testTag("badge_${badge.id}")
    ) {
        val color = badgeColor(badge.id)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(Sizes.AvatarM)
                .clip(CircleShape)
                .background(
                    if (badge.earned) {
                        Brush.linearGradient(listOf(color.copy(alpha = LIGHT_STOP), color))
                    } else {
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                )
        ) {
            Icon(
                badgePainter(badge.id),
                contentDescription = null,
                tint = if (badge.earned) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Sizes.IconL)
            )
        }
        Text(badge.name, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, maxLines = 2)
        val detail = when {
            !badge.earned && badge.goal > 1 -> stringResource(R.string.badges_progress, badge.current, badge.goal)
            else -> badge.description
        }
        if (detail.isNotBlank()) {
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
        if (!badge.earned && badge.goal > 1) {
            WhereverProgressBar(
                progress = { badge.fraction },
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.S).testTag("badge_progress_${badge.id}")
            )
        }
    }
}

private const val LIGHT_STOP = 0.7f

/** Color de cada insignia del catálogo del backend (nv-79), como en Figma. */
private fun badgeColor(id: String): Color = when (id) {
    "catador_cafe", "cronista" -> Sunset
    "resenista", "racha_3" -> Mint
    "viajero_pueblo", "sello_oro" -> Gold
    else -> RouteBlue
}

@Composable
private fun badgePainter(id: String): Painter = when (id) {
    "resenista", "sello_oro" -> painterResource(R.drawable.ic_wh_shield_check)
    "anfitrion_grupo" -> painterResource(R.drawable.ic_wh_users)
    "viajero_pueblo" -> rememberVectorPainter(Icons.Outlined.Place)
    "cronista" -> painterResource(R.drawable.ic_gallery)
    "racha_3" -> painterResource(R.drawable.ic_wh_sparkle)
    "querido" -> rememberVectorPainter(Icons.Outlined.FavoriteBorder)
    "explorador_total" -> painterResource(R.drawable.ic_wh_compass)
    else -> rememberVectorPainter(Icons.Outlined.Star)
}

private const val PREVIEW_POINTS = 340
private const val PREVIEW_NEXT = 500
private const val PREVIEW_LEVEL = 3
private const val PREVIEW_CURRENT = 12
private const val PREVIEW_GOAL = 20

private val PREVIEW_BADGES = listOf(
    Badge("catador_cafe", "Catador de café", "5 cafés", earned = true),
    Badge("resenista", "Reseñista", "Visita verificada", earned = true),
    Badge("anfitrion_grupo", "Anfitrión de grupo", "Tu 1er grupo", earned = true),
    Badge("viajero_pueblo", "Viajero del pueblo", "10 lugares", earned = true),
    Badge("sello_oro", "Sello de oro", "20 reseñas", earned = false, current = PREVIEW_CURRENT, goal = PREVIEW_GOAL),
    Badge("querido", "Querido", "50 me gusta", earned = false),
    Badge("explorador_total", "Explorador total", "30 lugares", earned = false)
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BadgesPreview() {
    WhereverTheme {
        BadgesContent(
            BadgesUiState(
                loading = false,
                progress = LevelProgress(
                    LevelStep("Explorador", 0),
                    LevelStep("Anfitrión", PREVIEW_NEXT),
                    PREVIEW_POINTS
                ),
                levelNumber = PREVIEW_LEVEL,
                earned = PREVIEW_BADGES.filter { it.earned },
                locked = PREVIEW_BADGES.filterNot { it.earned }
            ),
            onBack = {}
        )
    }
}
