package com.wherever.mobile.feature.profile.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverProgressBar
import com.wherever.mobile.feature.profile.domain.Badge
import com.wherever.mobile.feature.profile.domain.LevelProgress

/** 83_Insignias: tarjeta de nivel con puntos y progreso, Ganadas y Por desbloquear. */
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
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier.testTag("badges_list")
        ) {
            state.progress?.let { progress -> item { LevelCard(progress) } }
            item { SectionTitle(R.string.badges_earned) }
            if (state.earned.isEmpty()) {
                item { EmptyText(R.string.badges_earned_empty) }
            } else {
                items(state.earned, key = { it.id }) { BadgeRow(it) }
            }
            item { SectionTitle(R.string.badges_locked) }
            if (state.locked.isEmpty()) {
                item { EmptyText(R.string.badges_locked_empty) }
            } else {
                items(state.locked, key = { it.id }) { BadgeRow(it) }
            }
        }
    }
}

@Composable
private fun LevelCard(progress: LevelProgress) {
    SurfaceCard(modifier = Modifier.testTag("badges_level")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Text(
                stringResource(R.string.badges_level_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(progress.current.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                pluralStringResource(R.plurals.badges_points, progress.points, progress.points),
                style = MaterialTheme.typography.bodyMedium
            )
            WhereverProgressBar(
                progress = { progress.fraction },
                modifier = Modifier.fillMaxWidth().testTag("badges_progress")
            )
            val next = progress.next
            Text(
                if (next == null) {
                    stringResource(R.string.badges_max_level)
                } else {
                    pluralStringResource(
                        R.plurals.badges_to_next,
                        progress.pointsToNext,
                        progress.pointsToNext,
                        next.name
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(@StringRes title: Int) {
    Text(
        stringResource(title),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = Spacing.S)
    )
}

@Composable
private fun EmptyText(@StringRes text: Int) {
    Text(stringResource(text), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun BadgeRow(badge: Badge) {
    SurfaceCard(modifier = Modifier.testTag("badge_${badge.id}")) {
        Row(modifier = Modifier.fillMaxWidth().padding(Spacing.M), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (badge.earned) Icons.Filled.Star else Icons.Outlined.Lock,
                contentDescription = stringResource(
                    if (badge.earned) R.string.badges_earned else R.string.badges_locked
                ),
                tint = if (badge.earned) Gold else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier.weight(1f).padding(start = Spacing.M),
                verticalArrangement = Arrangement.spacedBy(Spacing.XS)
            ) {
                Text(badge.name, style = MaterialTheme.typography.titleSmall)
                if (badge.description.isNotBlank()) {
                    Text(
                        badge.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Avance real de la API (p. ej. 2 de 5 check-ins) en las que faltan.
                if (!badge.earned && badge.goal > 1) {
                    LinearProgressIndicator(
                        progress = { badge.fraction },
                        modifier = Modifier.fillMaxWidth().testTag("badge_progress_${badge.id}")
                    )
                    Text(
                        stringResource(R.string.badges_progress, badge.current, badge.goal),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
