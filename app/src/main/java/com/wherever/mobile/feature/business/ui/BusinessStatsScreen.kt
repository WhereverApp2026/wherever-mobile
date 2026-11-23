// TooManyFunctions: una función por bloque de 56_NegStats (cifras, gráfica, origen, reporte) y sus vistas previas.
@file:Suppress("TooManyFunctions")

package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.Grape
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverIcons
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverProgressBar
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.BusinessStats
import com.wherever.mobile.feature.business.domain.StatsOrigin
import com.wherever.mobile.feature.business.domain.StatsRange
import java.time.format.DateTimeFormatter

private const val PERCENT = 100

/** Más barras que esto (Mes) ya no caben con su letra; se muestran sin etiquetas. */
private const val MAX_LABELED_BARS = 7

private val DayMonth = DateTimeFormatter.ofPattern("d MMM", MexicanSpanish)

/** 56_NegStats (specs/nv-35). */
@Composable
fun BusinessStatsRoute(businessId: String, onBack: () -> Unit, viewModel: BusinessStatsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(businessId) { viewModel.start(businessId) }
    BusinessStatsContent(
        state = state,
        actions = StatsActions(
            onBack = onBack,
            onRange = viewModel::selectRange,
            onRetry = viewModel::load,
            onDownload = viewModel::downloadReport,
            onMessageShown = viewModel::consumeMessage
        )
    )
}

internal data class StatsActions(
    val onBack: () -> Unit = {},
    val onRange: (StatsRange) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onDownload: () -> Unit = {},
    val onMessageShown: () -> Unit = {}
)

@Composable
internal fun BusinessStatsContent(state: BusinessStatsUiState, actions: StatsActions) {
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            actions.onMessageShown()
        }
    }
    val error = state.error
    val stats = state.stats
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.L),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(ScreenPadding)
            .testTag("business_stats")
    ) {
        StatsHeader(actions.onBack)
        SegmentedControl(
            options = listOf(
                stringResource(R.string.business_stats_week),
                stringResource(R.string.business_stats_month),
                stringResource(R.string.business_stats_quarter)
            ),
            selectedIndex = state.range.ordinal,
            onSelect = { actions.onRange(StatsRange.entries[it]) },
            modifier = Modifier.testTag("business_stats_range")
        )
        when {
            state.loading -> ListSkeleton(rows = 4)
            error != null -> ErrorState(error.asString(), actions.onRetry, Modifier.heightIn(max = Sizes.HeroImage))
            stats != null -> StatsBody(stats, state.range)
        }
        WhereverOutlinedButton(
            text = stringResource(R.string.business_stats_report),
            onClick = actions.onDownload,
            enabled = !state.downloading,
            modifier = Modifier.fillMaxWidth(),
            icon = WhereverIcons.Download,
            tag = "business_stats_report"
        )
        WhereverSnackbarHost(snackbar)
    }
}

@Composable
private fun StatsHeader(onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = onBack)
            Text(
                text = stringResource(R.string.business_stats_title),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(Sizes.TouchTarget))
        }
        Text(
            text = stringResource(R.string.business_stats_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatsBody(stats: BusinessStats, range: StatsRange) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
        StatTile(
            stats.totals.reservations.toString(),
            R.string.business_stats_reservations,
            MaterialTheme.colorScheme.onSurface,
            Modifier.weight(1f)
        )
        StatTile(
            stats.totals.views.toString(),
            R.string.business_stats_views,
            MaterialTheme.colorScheme.primary,
            Modifier.weight(1f)
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
        StatTile(
            stats.attendance?.let { stringResource(R.string.business_percent, it) }
                ?: stringResource(R.string.business_dash),
            R.string.business_stats_attendance,
            Mint,
            Modifier.weight(1f)
        )
        StatTile(
            stats.totals.coupons.toString(),
            R.string.business_stats_coupons,
            MaterialTheme.colorScheme.onSurface,
            Modifier.weight(1f)
        )
    }
    ChartCard(stats, range)
    if (stats.phrase.isNotBlank()) InfoBanner(stats.phrase, icon = Icons.Outlined.Info, tag = "business_stats_phrase")
    val origin = stats.origin
    when {
        origin != null && origin.total > 0 -> OriginCard(origin)
        stats.premiumRequired -> InfoBanner(
            stringResource(R.string.business_stats_premium),
            tag = "business_stats_premium"
        )
    }
}

@Composable
private fun StatTile(value: String, @StringRes label: Int, color: Color, modifier: Modifier) {
    SurfaceCard(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.S)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = stringResource(label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChartCard(stats: BusinessStats, range: StatsRange) {
    val best = stats.series.maxByOrNull { it.reservations }?.takeIf { it.reservations > 0 }
    SurfaceCard(modifier = Modifier.testTag("business_stats_chart")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        if (range ==
                            StatsRange.QUARTER
                        ) {
                            R.string.business_stats_per_week
                        } else {
                            R.string.business_stats_per_day
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (best != null) {
                    Text(
                        text = if (range == StatsRange.WEEK) {
                            stringResource(R.string.business_stats_best_day, best.start.dayName())
                        } else {
                            stringResource(
                                R.string.business_stats_best_from,
                                best.start.format(DayMonth).replace(".", "")
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            BarChart(
                values = stats.series.map { it.reservations },
                labels = if (stats.series.size <=
                    MAX_LABELED_BARS
                ) {
                    stats.series.map { it.start.dayInitial() }
                } else {
                    emptyList()
                },
                height = Sizes.AvatarL,
                barColor = RouteBlue
            )
        }
    }
}

@Composable
private fun OriginCard(origin: StatsOrigin) {
    val rows = listOf(
        R.string.business_origin_ai_plan to origin.aiPlan to RouteBlue,
        R.string.business_origin_search to origin.search to Sunset,
        R.string.business_origin_map to origin.map to Mint,
        R.string.business_origin_home to origin.home to Gold,
        R.string.business_origin_coupon to origin.coupon to Grape
    ).sortedByDescending { it.first.second }
    SurfaceCard(modifier = Modifier.testTag("business_stats_origin")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Text(
                text = stringResource(R.string.business_origin_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            rows.forEach { (labelAndValue, color) ->
                val (label, value) = labelAndValue
                val share = value * PERCENT / origin.total
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
                    Row {
                        Text(
                            stringResource(label),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            stringResource(R.string.business_percent, share),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    WhereverProgressBar(progress = {
                        share / PERCENT.toFloat()
                    }, color = color, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Preview(name = "Estadísticas claro")
@Preview(name = "Estadísticas oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BusinessStatsPreview() {
    WhereverTheme { BusinessStatsContent(sampleStatsState(), StatsActions()) }
}
