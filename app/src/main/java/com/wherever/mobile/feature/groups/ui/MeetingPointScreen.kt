package com.wherever.mobile.feature.groups.ui

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
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
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.groups.domain.GroupMember
import com.wherever.mobile.feature.groups.domain.MeetingPlace
import com.wherever.mobile.feature.groups.domain.MeetingPoint
import com.wherever.mobile.feature.groups.domain.MeetingSuggestion
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.groups.domain.MemberTrip
import com.wherever.mobile.feature.groups.domain.TripRange
import com.wherever.mobile.feature.profile.ui.ProfileAvatar
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** Un pin del mapa de 47_PuntoMedio: un lugar sugerido o el punto ya fijado. */
data class MeetingMarker(val id: String, val name: String, val lat: Double, val lng: Double, val chosen: Boolean)

/** 47_PuntoMedio (specs/nv-25). */
@Composable
fun MeetingPointRoute(onBack: () -> Unit, viewModel: MeetingPointViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Permiso en contexto: solo al tocar «Compartir mi ubicación».
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), viewModel::share)
    MeetingPointContent(
        state,
        MeetingPointActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onShare = { permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
            onStopSharing = viewModel::stopSharing,
            onSelect = viewModel::select,
            onUse = viewModel::usePoint
        )
    )
}

data class MeetingPointActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onShare: () -> Unit = {},
    val onStopSharing: () -> Unit = {},
    val onSelect: (String) -> Unit = {},
    val onUse: () -> Unit = {}
)

@Composable
internal fun MeetingPointContent(
    state: MeetingPointUiState,
    actions: MeetingPointActions,
    zone: ZoneId = ZoneId.systemDefault(),
    map: @Composable (List<MeetingMarker>, (String) -> Unit) -> Unit = { markers, onMarker ->
        MeetingMap(
            markers,
            onMarker,
            Modifier.fillMaxWidth().height(Sizes.MapPreview).clip(RoundedCornerShape(Radius.L))
        )
    }
) {
    FormScreen(
        footer = {
            if (state.suggestion?.places?.isNotEmpty() == true) {
                WhereverPrimaryButton(
                    text = stringResource(R.string.meeting_use),
                    onClick = actions.onUse,
                    enabled = state.canUse,
                    loading = state.busy && state.selected != null,
                    tag = "meeting_use"
                )
            }
        }
    ) {
        TopTitleRow(stringResource(R.string.meeting_title), actions.onBack)
        when {
            state.loading -> LoadingState()
            state.loadError != null -> ErrorState(state.loadError.asString(), actions.onRetry)
            else -> MeetingBody(state, actions, zone, map)
        }
    }
}

@Composable
private fun MeetingBody(
    state: MeetingPointUiState,
    actions: MeetingPointActions,
    zone: ZoneId,
    map: @Composable (List<MeetingMarker>, (String) -> Unit) -> Unit
) {
    state.group?.meetingPoint?.let { CurrentPoint(it, zone) }
    state.error?.let { ErrorBanner(it.asString()) }
    val markers = markers(state)
    if (markers.isNotEmpty()) map(markers, actions.onSelect)
    ShareCard(state, actions)
    val places = state.suggestion?.places.orEmpty()
    when {
        state.needLocations -> InfoBanner(
            stringResource(R.string.meeting_need_locations),
            tag = "meeting_need_locations"
        )
        state.suggestion != null && places.isEmpty() -> Text(
            stringResource(R.string.meeting_no_places),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("meeting_no_places")
        )
        places.isNotEmpty() -> {
            Text(stringResource(R.string.meeting_suggested), style = MaterialTheme.typography.titleLarge)
            places.forEach { place ->
                PlaceChoice(
                    place,
                    place.id == state.selected,
                    place.id == state.group?.meetingPoint?.placeId,
                    actions.onSelect
                )
            }
        }
    }
    if (state.trips.isNotEmpty()) {
        Text(stringResource(R.string.meeting_trips), style = MaterialTheme.typography.titleLarge)
        state.trips.forEach { TripRow(it) }
    }
}

private fun markers(state: MeetingPointUiState): List<MeetingMarker> {
    val point = state.group?.meetingPoint
    val places = state.suggestion?.places.orEmpty().map {
        MeetingMarker(it.id, it.name, it.lat, it.lng, chosen = it.id == state.selected || it.id == point?.placeId)
    }
    val fixed = point?.takeIf { p -> places.none { it.id == p.placeId } }?.let {
        MeetingMarker(it.placeId ?: "punto", it.name, it.lat, it.lng, chosen = true)
    }
    return listOfNotNull(fixed) + places
}

@Composable
private fun CurrentPoint(point: MeetingPoint, zone: ZoneId) {
    SurfaceCard(modifier = Modifier.testTag("meeting_current")) {
        Row(
            modifier = Modifier.padding(Spacing.L),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.M)
        ) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
                Text(stringResource(R.string.meeting_current), style = MaterialTheme.typography.labelLarge)
                Text(point.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                clock(point.at, zone)?.let {
                    Text(
                        stringResource(R.string.meeting_at, it),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareCard(state: MeetingPointUiState, actions: MeetingPointActions) {
    SurfaceCard(modifier = Modifier.testTag("meeting_share")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Text(
                stringResource(R.string.meeting_sharers, state.sharers, state.confirmed),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("meeting_sharers")
            )
            if (state.sharing) {
                Text(stringResource(R.string.meeting_sharing), color = MaterialTheme.colorScheme.onSurfaceVariant)
                WhereverTextLink(
                    stringResource(R.string.meeting_stop_sharing),
                    onClick = actions.onStopSharing,
                    enabled = !state.busy,
                    modifier = Modifier.testTag("meeting_stop_sharing")
                )
            } else {
                Text(stringResource(R.string.meeting_share_why), color = MaterialTheme.colorScheme.onSurfaceVariant)
                WhereverOutlinedButton(
                    text = stringResource(R.string.meeting_share),
                    onClick = actions.onShare,
                    enabled = !state.busy,
                    icon = Icons.Outlined.LocationOn,
                    tag = "meeting_share_button"
                )
            }
        }
    }
}

@Composable
private fun PlaceChoice(place: MeetingPlace, selected: Boolean, current: Boolean, onSelect: (String) -> Unit) {
    SurfaceCard(
        modifier = Modifier
            .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(place.id) })
            .testTag("meeting_place_${place.id}")
    ) {
        Row(
            modifier = Modifier.padding(Spacing.M),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.S)
        ) {
            RadioButton(selected = selected, onClick = null)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
                Text(place.name, style = MaterialTheme.typography.titleMedium)
                val detail = if (place.rating > 0) {
                    stringResource(R.string.meeting_place_rated, place.category, place.rating)
                } else {
                    place.category
                }
                Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (current) {
                Text(
                    stringResource(R.string.meeting_place_current),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TripRow(trip: MemberTrip) {
    Row(
        modifier = Modifier.fillMaxWidth().testTag("meeting_trip_${trip.userId}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.M)
    ) {
        ProfileAvatar(trip.name, trip.photoUrl, size = Sizes.AvatarS)
        Text(
            if (trip.mine) stringResource(R.string.meeting_you) else trip.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(tripLabel(trip), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun tripLabel(trip: MemberTrip): String = when {
    trip.noLocation -> stringResource(R.string.meeting_trip_no_location)
    trip.minutes != null && trip.cost != null && trip.cost > 0 ->
        stringResource(R.string.meeting_trip_exact_cost, trip.minutes, trip.cost)
    trip.minutes != null -> stringResource(R.string.meeting_trip_exact, trip.minutes)
    else -> when (trip.range) {
        TripRange.UP_TO_15 -> stringResource(R.string.meeting_trip_up_to_15)
        TripRange.FROM_15_TO_30 -> stringResource(R.string.meeting_trip_15_30)
        TripRange.FROM_30_TO_60 -> stringResource(R.string.meeting_trip_30_60)
        TripRange.OVER_60 -> stringResource(R.string.meeting_trip_over_60)
        null -> stringResource(R.string.meeting_trip_unknown)
    }
}

private val CLOCK = DateTimeFormatter.ofPattern("HH:mm")

private fun clock(iso: String, zone: ZoneId): String? = try {
    Instant.parse(iso).atZone(zone).format(CLOCK)
} catch (_: DateTimeParseException) {
    null
}

@Composable
private fun PreviewMap(markers: List<MeetingMarker>) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(Sizes.MapPreview)
            .clip(RoundedCornerShape(Radius.L))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(markers.joinToString(" · ") { it.name }, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Suppress("MagicNumber")
private val PREVIEW_MEMBERS = listOf(
    GroupMember("yo", "Jorge", null, true, MemberStatus.CONFIRMED, sharesLocation = true),
    GroupMember("u2", "Mariana López", null, false, MemberStatus.CONFIRMED, sharesLocation = true),
    GroupMember("u3", "Luis", null, false, MemberStatus.CONFIRMED)
)

@Suppress("MagicNumber")
private val PREVIEW_PLACES = listOf(
    MeetingPlace("p1", "Restaurante Colonial", "restaurante", 4.6, 20.057, -99.337),
    MeetingPlace("p2", "Café del Centro", "cafeteria", 0.0, 20.056, -99.340),
    MeetingPlace("p3", "Jardín Tula", "parque", 4.2, 20.058, -99.341)
)

@Suppress("MagicNumber")
private fun previewState(withPoint: Boolean) = MeetingPointUiState(
    group = PREVIEW_GROUPS.first().copy(
        members = PREVIEW_MEMBERS,
        meetingPoint = MeetingPoint("Restaurante Colonial", 20.057, -99.337, "2026-11-29T00:00:00Z", "p1").takeIf {
            withPoint
        }
    ),
    myId = "yo",
    loading = false,
    suggestion = MeetingSuggestion(PREVIEW_PLACES, participants = 2),
    selected = "p1",
    trips = if (withPoint) {
        listOf(
            MemberTrip("yo", "Jorge", null, true, 12, 25, null, false),
            MemberTrip("u2", "Mariana López", null, false, null, null, TripRange.FROM_15_TO_30, false),
            MemberTrip("u3", "Luis", null, false, null, null, null, true)
        )
    } else {
        emptyList()
    }
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MeetingPointPreview() {
    WhereverTheme {
        MeetingPointContent(previewState(withPoint = true), MeetingPointActions(), ZoneOffset.UTC) { m, _ ->
            PreviewMap(m)
        }
    }
}

/** 47_PuntoMedio antes de que 2 personas compartan su ubicación. */
@Preview
@Composable
internal fun MeetingPointNeedLocationsPreview() {
    WhereverTheme {
        MeetingPointContent(
            previewState(withPoint = false).copy(
                suggestion = null,
                needLocations = true,
                selected = null,
                group = previewState(false).group?.copy(
                    members = PREVIEW_MEMBERS.map {
                        it.copy(sharesLocation = false)
                    }
                )
            ),
            MeetingPointActions(),
            ZoneOffset.UTC
        ) { m, _ -> PreviewMap(m) }
    }
}
