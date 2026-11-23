package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.Mist
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverProgressBar
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.HourSlot
import com.wherever.mobile.feature.business.domain.TURN_STEP
import java.time.LocalDate

/** Hasta aquí la hora es tranquila; arriba de [BUSY] es hora pico. */
private const val QUIET = 0.5f
private const val BUSY = 0.85f

/** 55_NegHoras (specs/nv-37). */
@Composable
fun CapacityRoute(businessId: String, onBack: () -> Unit, viewModel: CapacityViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(businessId) { viewModel.start(businessId) }
    CapacityContent(
        state,
        CapacityActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onDay = viewModel::onDay,
            onCapacity = viewModel::onCapacity,
            onTurn = viewModel::onTurn,
            onSave = viewModel::save,
            onMessageShown = viewModel::messageShown
        )
    )
}

@Composable
internal fun CapacityContent(state: CapacityUiState, actions: CapacityActions) {
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            actions.onMessageShown()
        }
    }
    val error = state.error
    FormScreen(
        footer = {
            WhereverSnackbarHost(snackbar)
            WhereverPrimaryButton(
                text = stringResource(R.string.capacity_save),
                onClick = actions.onSave,
                enabled = state.canSave,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
                tag = "capacity_save"
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onBack)
            Spacer(Modifier.width(Spacing.M))
            Column {
                Text(stringResource(R.string.capacity_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.capacity_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        when {
            state.loading -> ListSkeleton(rows = 4)
            error != null -> ErrorState(error.asString(), actions.onRetry, Modifier.heightIn(max = Sizes.HeroImage))
            else -> CapacityBody(state, actions)
        }
    }
}

@Composable
private fun CapacityBody(state: CapacityUiState, actions: CapacityActions) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        state.days.forEach { day -> DayChip(day, selected = day == state.date) { actions.onDay(day) } }
    }
    SurfaceCard(modifier = Modifier.testTag("capacity_hours")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            when {
                state.loadingSlots -> ListSkeleton(rows = 3)
                state.slots.isEmpty() -> Text(
                    stringResource(R.string.capacity_no_slots),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> state.slots.forEach { slot -> SlotRow(slot, state.occupancy(slot)) }
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.L)) {
        Legend(Mint, R.string.capacity_quiet)
        Legend(Gold, R.string.capacity_moderate)
        Legend(Sunset, R.string.capacity_peak)
    }
    SurfaceCard {
        Column(modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.S)) {
            BusinessStepper(
                title = stringResource(R.string.capacity_per_slot),
                subtitle = stringResource(R.string.capacity_per_slot_hint),
                value = state.capacity.toString(),
                onLess = { actions.onCapacity(state.capacity - 1) },
                onMore = { actions.onCapacity(state.capacity + 1) },
                tag = "capacity_value"
            )
            BusinessStepper(
                title = stringResource(R.string.capacity_turn),
                subtitle = stringResource(R.string.capacity_turn_hint),
                value = stringResource(R.string.business_minutes, state.turnMinutes),
                onLess = { actions.onTurn(state.turnMinutes - TURN_STEP) },
                onMore = { actions.onTurn(state.turnMinutes + TURN_STEP) },
                tag = "capacity_turn"
            )
        }
    }
    InfoBanner(stringResource(R.string.capacity_notice), icon = ImageVector.vectorResource(R.drawable.ic_wh_sparkle))
}

@Composable
private fun DayChip(day: LocalDate, selected: Boolean, onClick: () -> Unit) {
    val description = day.longText()
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) RouteBlue else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) Mist else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(Sizes.TouchTarget).semantics {
            contentDescription = description
        }.testTag("capacity_day_$day")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(day.dayInitial(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SlotRow(slot: HourSlot, occupancy: Float) {
    val color = when {
        occupancy < QUIET -> Mint
        occupancy < BUSY -> Gold
        else -> Sunset
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(slot.hour, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(Spacing.M))
        WhereverProgressBar(progress = { occupancy }, color = color, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(Spacing.M))
        Text(
            pluralStringResource(R.plurals.capacity_free, slot.free, slot.free),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Legend(color: Color, label: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(Sizes.Dot).background(color, CircleShape))
        Spacer(Modifier.width(Spacing.XS))
        Text(stringResource(label), style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(name = "55_NegHoras claro")
@Preview(name = "55_NegHoras oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CapacityPreview() {
    WhereverTheme { CapacityContent(sampleCapacityState(), CapacityActions()) }
}
