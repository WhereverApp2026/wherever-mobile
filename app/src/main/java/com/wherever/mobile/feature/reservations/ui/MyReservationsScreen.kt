package com.wherever.mobile.feature.reservations.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.ui.formatDateTime
import com.wherever.mobile.feature.reservations.domain.Reservation
import java.time.ZoneId

/** Mis reservas (US-077). */
@Composable
fun MyReservationsRoute(
    onBack: () -> Unit,
    onReservation: (String) -> Unit,
    viewModel: MyReservationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.load()
        onPauseOrDispose { }
    }
    val zone = remember { ZoneId.systemDefault() }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        TopTitleRow(stringResource(R.string.my_reservations_title), onBack, Modifier.padding(ScreenPadding))
        SegmentedControl(
            options = listOf(
                stringResource(R.string.my_reservations_upcoming),
                stringResource(R.string.my_reservations_past)
            ),
            selectedIndex = if (state.upcoming) 0 else 1,
            onSelect = { viewModel.onUpcoming(it == 0) },
            modifier = Modifier.padding(horizontal = ScreenPadding)
        )
        state.error?.let { ErrorBanner(it.asString(), Modifier.padding(ScreenPadding)) }
        val loadError = state.loadError
        when {
            state.loading -> ListSkeleton()
            loadError != null -> ErrorState(loadError.asString(), viewModel::load)
            state.items.isEmpty() -> Text(
                stringResource(
                    if (state.upcoming) R.string.my_reservations_empty else R.string.my_reservations_empty_past
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(ScreenPadding).testTag("my_reservations_empty")
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.M),
                modifier = Modifier.testTag("my_reservations_list")
            ) {
                items(state.items, key = { it.id }) { reservation ->
                    ReservationRow(
                        reservation,
                        state.placeNames[reservation.placeId],
                        formatDateTime(reservation.dateTime, zone)
                    ) { onReservation(reservation.id) }
                }
                if (state.nextCursor != null) {
                    item {
                        WhereverOutlinedButton(
                            text = stringResource(R.string.notifications_more),
                            onClick = viewModel::loadMore,
                            enabled = !state.loadingMore,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationRow(reservation: Reservation, placeName: String?, time: String, onClick: () -> Unit) {
    SurfaceCard(modifier = Modifier.testTag("reservation_${reservation.id}")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(Spacing.M)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    placeName ?: stringResource(R.string.reservation_place_fallback),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    pluralStringResource(R.plurals.reserve_people_count, reservation.people, reservation.people),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(reservation.status)
        }
    }
}
