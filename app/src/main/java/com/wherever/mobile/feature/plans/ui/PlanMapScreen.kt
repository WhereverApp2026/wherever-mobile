package com.wherever.mobile.feature.plans.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatTime
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.RoutePoint
import com.wherever.mobile.feature.plans.domain.Transfer
import com.wherever.mobile.feature.plans.domain.into
import java.time.ZoneId
import java.util.Locale

private const val MINUTES_PER_HOUR = 60

/** 13_Mapa y 14_MapaFull (specs/nv-30, D-30f a D-30j). */
@Composable
fun PlanMapRoute(onBack: () -> Unit, onFull: () -> Unit, viewModel: PlanMapViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val uri = when (event) {
                is PlanMapEvent.Directions ->
                    "geo:${event.lat},${event.lng}?q=${event.lat},${event.lng}(${Uri.encode(event.name)})"
                is PlanMapEvent.StartRoute -> event.url
            }
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
            } catch (_: ActivityNotFoundException) {
                viewModel.noMapsApp()
            }
        }
    }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            viewModel.messageShown()
        }
    }
    Scaffold(snackbarHost = { WhereverSnackbarHost(snackbar) }) { padding ->
        Box(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            PlanMapContent(
                state,
                ZoneId.systemDefault(),
                PlanMapActions(
                    onBack = onBack,
                    onFull = onFull,
                    onDirections = viewModel::directionsToFirst,
                    onStartRoute = viewModel::startRoute,
                    onRetry = viewModel::load
                )
            ) { points -> PlanRouteMap(points, Modifier.fillMaxSize()) }
        }
    }
}

@Composable
internal fun PlanMapContent(
    state: PlanMapUiState,
    zone: ZoneId,
    actions: PlanMapActions,
    map: @Composable (List<RoutePoint>) -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            map(state.points)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.statusBarsPadding().padding(Spacing.L).fillMaxWidth()
            ) {
                BackCircleButton(onClick = actions.onBack)
                Spacer(Modifier.width(Spacing.M))
                state.plan?.let { plan ->
                    Text(
                        if (state.full) {
                            pluralStringResource(R.plurals.plan_map_full_title, plan.stops.size, plan.stops.size)
                        } else {
                            plan.name
                        },
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = Spacing.L, vertical = Spacing.S)
                    )
                }
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = Radius.XXL, topEnd = Radius.XXL))
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding()
                .padding(Spacing.L)
                .testTag("plan_map_sheet")
        ) {
            val error = state.error
            val plan = state.plan
            when {
                state.loading -> ListSkeleton(rows = 3)
                error != null || plan == null -> ErrorState(
                    error?.asString() ?: stringResource(R.string.error_unknown),
                    actions.onRetry
                )
                state.full -> FullSheet(state, zone, actions)
                else -> RouteSheet(state, plan.mobility, plan.transfers, zone, actions)
            }
        }
    }
}

/** Hoja de 13_Mapa: paradas con su hora y el traslado real que llega a cada una. */
@Composable
private fun RouteSheet(
    state: PlanMapUiState,
    mobility: Mobility,
    transfers: List<Transfer>,
    zone: ZoneId,
    actions: PlanMapActions
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            stringResource(R.string.plan_map_route_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        // Con una sola parada no hay traslados: «0 min · 0.0 km» no dice nada.
        if (transfers.isNotEmpty()) {
            Text(
                stringResource(R.string.plan_map_totals, state.totals.transferMinutes, km(state.totals.km)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    state.plan?.stops?.forEachIndexed { index, stop ->
        val transfer = transfers.into(stop.id)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
            NumberBadge(index + 1)
            Column(Modifier.weight(1f)) {
                Text(
                    stop.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (transfer != null && index > 0) {
                    Text(
                        stringResource(
                            R.string.plan_map_from_stop,
                            transfer.minutes,
                            stringResource(mobilityLabel(mobility)).lowercase(Locale.forLanguageTag("es-MX")),
                            index
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                formatTime(stop.arrives, zone),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    if (state.missing >
        0
    ) {
        InfoBanner(
            pluralStringResource(R.plurals.plan_map_missing, state.missing, state.missing),
            tag = "plan_map_missing"
        )
    }
    WhereverTextLink(
        stringResource(R.string.plan_map_see_full),
        actions.onFull,
        modifier = Modifier.testTag("plan_map_full")
    )
    state.points.firstOrNull()?.let { first ->
        WhereverPrimaryButton(
            stringResource(R.string.plan_map_directions_to, first.stop.name),
            actions.onDirections,
            tag = "plan_map_directions"
        )
    }
}

/** Hoja de 14_MapaFull: totales reales, paradas con su hora e «Iniciar ruta». */
@Composable
private fun FullSheet(state: PlanMapUiState, zone: ZoneId, actions: PlanMapActions) {
    Text(stringResource(R.string.plan_map_full_sheet), style = MaterialTheme.typography.titleLarge)
    SurfaceCard {
        Row(Modifier.fillMaxWidth().padding(vertical = Spacing.M)) {
            // Con una sola parada no hay traslados: solo queda la duración.
            if (state.plan?.transfers.orEmpty().isNotEmpty()) {
                Stat(
                    stringResource(R.string.plan_map_km, km(state.totals.km)),
                    stringResource(R.string.plan_map_stat_total),
                    Modifier.weight(1f)
                )
                Stat(
                    stringResource(R.string.plan_map_minutes, state.totals.transferMinutes),
                    stringResource(R.string.plan_map_stat_between),
                    Modifier.weight(1f)
                )
            }
            state.totals.spanMinutes?.let { span ->
                Stat(hours(span), stringResource(R.string.plan_map_stat_with_stops), Modifier.weight(1f))
            }
        }
    }
    // Una fila por parada: con letra grande, tres tarjetas lado a lado partían la hora (09:/00).
    state.plan?.stops?.forEachIndexed { index, stop ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
            NumberBadge(index + 1)
            Text(
                stop.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(formatTime(stop.arrives, zone), style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
    if (state.missing >
        0
    ) {
        InfoBanner(
            pluralStringResource(R.plurals.plan_map_missing, state.missing, state.missing),
            tag = "plan_map_missing"
        )
    }
    WhereverPrimaryButton(
        stringResource(R.string.plan_map_start),
        actions.onStartRoute,
        enabled = state.points.isNotEmpty(),
        tag = "plan_map_start"
    )
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NumberBadge(number: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(Sizes.StepBadge).clip(CircleShape).background(stopColor(number))
    ) {
        Text(
            number.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = onStopColor(number),
            fontWeight = FontWeight.Bold
        )
    }
}

private fun km(value: Double): String = String.format(Locale.US, "%.1f", value)

@Composable
private fun hours(minutes: Long): String {
    val h = minutes / MINUTES_PER_HOUR
    val m = minutes % MINUTES_PER_HOUR
    return if (m ==
        0L
    ) {
        stringResource(R.string.plan_map_hours, h)
    } else {
        stringResource(R.string.plan_map_hours_minutes, h, m)
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PlanMapPreview() {
    WhereverTheme {
        PlanMapContent(previewMapState(full = false), ZoneId.of("UTC"), PlanMapActions()) { PreviewRouteMap() }
    }
}

@Preview
@Composable
internal fun PlanMapFullPreview() {
    WhereverTheme {
        PlanMapContent(previewMapState(full = true), ZoneId.of("UTC"), PlanMapActions()) { PreviewRouteMap() }
    }
}

/** En las vistas previas y capturas el mapa real no tiene teselas: un fondo liso ocupa su lugar. */
@Composable
private fun PreviewRouteMap() {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
}
