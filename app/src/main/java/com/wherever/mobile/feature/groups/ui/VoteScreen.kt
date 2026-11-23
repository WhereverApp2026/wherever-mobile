package com.wherever.mobile.feature.groups.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverProgressBar
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.groups.domain.Vote
import com.wherever.mobile.feature.groups.domain.VoteOption
import java.time.Instant
import kotlinx.coroutines.delay

/** 48_Votar (specs/nv-25). */
@Composable
fun VoteRoute(onBack: () -> Unit, viewModel: VoteViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(VOTE_MESSAGE_MILLIS)
            viewModel.messageShown()
        }
    }
    VoteContent(
        state,
        VoteActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onSelect = viewModel::select,
            onSend = viewModel::send,
            onClose = viewModel::close
        )
    )
}

private const val VOTE_MESSAGE_MILLIS = 4_000L
private const val MINUTES_PER_HOUR = 60L
private const val HOURS_PER_DAY = 24L

@Composable
internal fun VoteContent(state: VoteUiState, actions: VoteActions) {
    FormScreen {
        TopTitleRow(stringResource(R.string.vote_title), actions.onBack)
        state.message?.let { InfoBanner(it.asString(), icon = Icons.Outlined.Check, tag = "vote_message") }
        state.error?.let { ErrorBanner(it.asString()) }
        when {
            state.loading -> LoadingState()
            state.loadError != null -> ErrorState(state.loadError.asString(), actions.onRetry)
            state.empty -> Text(
                stringResource(R.string.vote_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("vote_empty")
            )
            else -> state.votes.forEachIndexed { index, vote -> VoteCard(index + 1, vote, state, actions) }
        }
    }
}

@Composable
private fun VoteCard(number: Int, vote: Vote, state: VoteUiState, actions: VoteActions) {
    val open = state.isOpen(vote)
    SurfaceCard(modifier = Modifier.testTag("vote_${vote.id}")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.vote_stop, number),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(closingText(vote, state))
            }
            Text(
                pluralStringResource(R.plurals.vote_total, vote.total, vote.total),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            vote.options.forEach { option ->
                OptionRow(
                    option = option,
                    total = vote.total,
                    selected = state.choice(vote) == option.placeId,
                    winner = !open && vote.winner == option.placeId,
                    enabled = open && vote.id !in state.sending,
                    onClick = { actions.onSelect(vote, option.placeId) }
                )
            }
            if (!open && vote.applied == false) {
                Text(
                    stringResource(R.string.vote_not_applied),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (open) {
                WhereverPrimaryButton(
                    text = stringResource(if (vote.myVote == null) R.string.vote_send else R.string.vote_change),
                    onClick = { actions.onSend(vote) },
                    enabled = state.canSend(vote),
                    loading = vote.id in state.sending,
                    tag = "vote_send_${vote.id}"
                )
                if (state.iAmOrganizer) {
                    WhereverTextLink(
                        stringResource(R.string.vote_close),
                        onClick = { actions.onClose(vote) },
                        enabled = vote.id !in state.sending
                    )
                }
            }
        }
    }
}

@Composable
private fun closingText(vote: Vote, state: VoteUiState): String {
    if (!state.isOpen(vote)) return stringResource(R.string.vote_closed_label)
    val minutes =
        state.remaining(vote)?.toMinutes()?.coerceAtLeast(1) ?: return stringResource(R.string.vote_open_label)
    return when {
        minutes < MINUTES_PER_HOUR -> pluralStringResource(R.plurals.vote_closes_minutes, minutes.toInt(), minutes)
        minutes < MINUTES_PER_HOUR * HOURS_PER_DAY -> {
            val hours = minutes / MINUTES_PER_HOUR
            pluralStringResource(R.plurals.vote_closes_hours, hours.toInt(), hours)
        }
        else -> {
            val days = minutes / (MINUTES_PER_HOUR * HOURS_PER_DAY)
            pluralStringResource(R.plurals.vote_closes_days, days.toInt(), days)
        }
    }
}

@Composable
private fun OptionRow(
    option: VoteOption,
    total: Int,
    selected: Boolean,
    winner: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected || winner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Surface(
        shape = RoundedCornerShape(Radius.Field),
        border = BorderStroke(if (selected || winner) Strokes.Thin else Strokes.Hairline, border),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .testTag("vote_option_${option.placeId}")
    ) {
        Column(modifier = Modifier.padding(Spacing.M), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (enabled) RadioButton(selected = selected, onClick = null)
                Text(
                    option.placeName ?: stringResource(R.string.vote_place_gone),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f).padding(start = Spacing.S)
                )
                if (winner) StatusPill(stringResource(R.string.vote_winner))
                Text(
                    pluralStringResource(R.plurals.vote_votes, option.votes, option.votes),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = Spacing.S)
                )
            }
            WhereverProgressBar(
                progress = { if (total == 0) 0f else option.votes.toFloat() / total },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

internal val PREVIEW_VOTES = listOf(
    Vote(
        id = "v1",
        stopId = "s1",
        options = listOf(
            VoteOption("n1", "Café Tacuba", votes = 3),
            VoteOption("n2", "La Casa de Toño", votes = 1),
            VoteOption("n3", "El Cardenal", votes = 0)
        ),
        total = 4,
        myVote = "n1",
        closesAt = "2026-11-23T20:00:00Z",
        open = true,
        winner = null,
        applied = null
    ),
    Vote(
        id = "v2",
        stopId = "s2",
        options = listOf(VoteOption("n4", "Museo Soumaya", votes = 2), VoteOption("n5", null, 1)),
        total = 3,
        myVote = null,
        closesAt = "2026-11-22T20:00:00Z",
        open = false,
        winner = "n4",
        applied = true
    )
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun VotePreview() {
    WhereverTheme {
        VoteContent(
            VoteUiState(
                loading = false,
                votes = PREVIEW_VOTES,
                iAmOrganizer = true,
                selected = mapOf("v1" to "n2"),
                now = Instant.parse("2026-11-23T17:30:00Z")
            ),
            VoteActions()
        )
    }
}
