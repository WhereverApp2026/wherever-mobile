// TooManyFunctions: una función por bloque de 48_NegPanel (portada, consejo, semana, próximas, sello)
// y sus vistas previas.
@file:Suppress("TooManyFunctions")

package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.Mist
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlueTint
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.BusinessPanel
import com.wherever.mobile.feature.business.domain.BusinessStats
import com.wherever.mobile.feature.business.domain.Reservation
import com.wherever.mobile.feature.business.domain.SealCriterion
import com.wherever.mobile.feature.business.domain.SealProgress
import kotlin.math.roundToInt

/** Velo oscuro sobre la foto de portada para que el texto blanco se lea. */
private const val COVER_SCRIM = 0.55f

/** Fondo translúcido de las cifras de la portada. */
private const val TILE_ALPHA = 0.18f

/** Negocios del dueño y cuál está activo (selector arriba del Panel si hay más de uno). */
data class BusinessHeader(val choices: List<BusinessChoice>, val selectedId: String, val onSelect: (String) -> Unit)

/** 48_NegPanel (specs/nv-35). Se vuelve a pedir cada vez que regresa al primer plano. */
@Composable
fun BusinessPanelRoute(
    header: BusinessHeader,
    onStats: () -> Unit,
    onReservations: () -> Unit,
    onCapacity: () -> Unit = {},
    viewModel: BusinessPanelViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(header.selectedId) {
        viewModel.start(header.selectedId)
        onPauseOrDispose { }
    }
    BusinessPanelContent(state, header, onStats, onReservations, viewModel::load, onCapacity)
}

@Composable
internal fun BusinessPanelContent(
    state: BusinessPanelUiState,
    header: BusinessHeader,
    onStats: () -> Unit,
    onReservations: () -> Unit,
    onRetry: () -> Unit,
    onCapacity: () -> Unit = {}
) {
    val panel = state.panel
    val error = state.error
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .testTag("business_panel")
    ) {
        val cover = header.choices.firstOrNull { it.id == header.selectedId }
        PanelHero(state, cover?.name.orEmpty(), cover?.coverUrl)
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.L),
            modifier = Modifier.padding(ScreenPadding)
        ) {
            if (header.choices.size > 1) BusinessSelector(header)
            when {
                state.loading -> ListSkeleton(rows = 3)
                error != null && panel == null -> ErrorState(
                    error.asString(),
                    onRetry,
                    Modifier.heightIn(max = Sizes.HeroImage)
                )
                panel != null -> {
                    PanelBody(state, panel, onStats, onReservations)
                    // 55_NegHoras (specs/nv-37): entra desde el Panel mientras no exista 58_NegPerfil (nv-38).
                    WhereverTextLink(
                        stringResource(R.string.capacity_open),
                        onCapacity,
                        Modifier.testTag("business_capacity")
                    )
                }
            }
        }
    }
}

@Composable
private fun BusinessSelector(header: BusinessHeader) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S), modifier = Modifier.testTag("business_selector")) {
        header.choices.forEach { choice ->
            SelectChip(
                text = choice.name,
                selected = choice.id == header.selectedId,
                onClick = { header.onSelect(choice.id) }
            )
        }
    }
}

@Composable
private fun PanelHero(state: BusinessPanelUiState, fallbackName: String, coverUrl: String?) {
    Box(modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.HeroImage).background(Ink)) {
        if (coverUrl != null) {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(Modifier.matchParentSize().background(Ink.copy(alpha = COVER_SCRIM)))
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier.statusBarsPadding().padding(ScreenPadding)
        ) {
            Surface(shape = RoundedCornerShape(Radius.S), color = Mist.copy(alpha = TILE_ALPHA), contentColor = Mist) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = Spacing.M, vertical = Spacing.XS)
                ) {
                    Icon(Icons.Outlined.Home, contentDescription = null, modifier = Modifier.size(Sizes.IconS))
                    Spacer(Modifier.width(Spacing.S))
                    Text(stringResource(R.string.business_home_badge), style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(
                text = state.panel?.name ?: fallbackName,
                style = MaterialTheme.typography.headlineLarge,
                color = Mist,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (state.panel?.seal == true) {
                StatusPill(
                    stringResource(R.string.business_seal),
                    container = Gold,
                    contentColor = Ink,
                    modifier = Modifier.testTag("business_seal")
                )
            }
            if (state.tiles.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    state.tiles.forEachIndexed { index, tile ->
                        HeroTile(tile, highlight = index == state.tiles.lastIndex, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroTile(tile: PanelTile, highlight: Boolean, modifier: Modifier) {
    Surface(
        shape = RoundedCornerShape(Radius.M),
        color = Mist.copy(alpha = TILE_ALPHA),
        contentColor = Mist,
        modifier = modifier.testTag("business_tile_${tile.metric.name.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(Spacing.M)) {
            Text(
                text = tile.value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (highlight) Gold else Mist
            )
            Text(
                text = stringResource(
                    when (tile.metric) {
                        PanelMetric.RESERVATIONS_TODAY -> R.string.business_tile_reservations
                        PanelMetric.ARRIVALS_TODAY -> R.string.business_tile_arrivals
                        PanelMetric.COUPONS_TODAY -> R.string.business_tile_coupons
                    }
                ),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PanelBody(
    state: BusinessPanelUiState,
    panel: BusinessPanel,
    onStats: () -> Unit,
    onReservations: () -> Unit
) {
    state.tip?.let { tip ->
        InfoBanner(tip.text, icon = Icons.Outlined.Info, tag = "business_tip")
    }
    state.week?.let { WeekCard(it, onStats) }
    UpcomingCard(state.upcoming, onReservations)
    WeekSummaryCard(panel)
    panel.sealProgress?.takeIf { !panel.seal && it.criteria.isNotEmpty() }?.let { SealCard(it) }
}

@Composable
private fun WeekCard(week: BusinessStats, onStats: () -> Unit) {
    SurfaceCard(modifier = Modifier.clickable(onClick = onStats).testTag("business_week")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.business_week_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                week.reservationsChange?.let { change ->
                    Text(
                        text = stringResource(R.string.business_change, change.signedPercent()),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (change >= 0) Mint else MaterialTheme.colorScheme.error
                    )
                }
            }
            BarChart(
                values = week.series.map { it.reservations },
                labels = week.series.map { it.start.dayInitial() },
                height = Sizes.Thumbnail,
                barColor = RouteBlueTint
            )
        }
    }
}

@Composable
private fun UpcomingCard(upcoming: List<Reservation>, onReservations: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.business_upcoming_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        WhereverTextLink(
            stringResource(R.string.business_see_all),
            onReservations,
            Modifier.testTag("business_see_all")
        )
    }
    SurfaceCard(modifier = Modifier.testTag("business_upcoming")) {
        if (upcoming.isEmpty()) {
            Text(
                text = stringResource(R.string.business_upcoming_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.L)
            )
        }
        upcoming.forEach { reservation ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onReservations)
                    .padding(horizontal = Spacing.L, vertical = Spacing.S)
            ) {
                TimeBadge(reservation.dateTime.hourText())
                Spacer(Modifier.width(Spacing.M))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reservation.client,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = pluralStringResource(R.plurals.business_people, reservation.people, reservation.people),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ReservationStatusPill(reservation.status)
            }
        }
    }
}

/** Datos del panel que no tienen lugar en la portada; cada uno se oculta si llega nulo. */
@Composable
private fun WeekSummaryCard(panel: BusinessPanel) {
    val rows = buildList {
        panel.nextReservation?.let {
            add(
                stringResource(R.string.business_next_reservation) to
                    stringResource(
                        R.string.business_next_value,
                        it.time,
                        pluralStringResource(R.plurals.business_people, it.people, it.people)
                    )
            )
        }
        panel.weekRating?.let { rating ->
            val change = panel.ratingChange?.let { stringResource(R.string.business_rating_change, it.signed()) }
            add(
                stringResource(R.string.business_week_rating) to
                    listOfNotNull(stringResource(R.string.business_rating_value, rating), change).joinToString(" ")
            )
        }
        panel.reviewsToAnswer?.let { add(stringResource(R.string.business_reviews_to_answer) to it.toString()) }
        panel.activeCoupons?.let { add(stringResource(R.string.business_active_coupons) to it.toString()) }
    }
    if (rows.isEmpty()) return
    SurfaceCard(modifier = Modifier.testTag("business_summary")) {
        Text(
            text = stringResource(R.string.business_summary_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.S)
        )
        rows.forEach { (label, value) -> DetailRow(label, value) }
    }
}

@Composable
private fun SealCard(progress: SealProgress) {
    SurfaceCard(modifier = Modifier.testTag("business_seal_progress")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.S)
        ) {
            Icon(Icons.Outlined.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(Sizes.IconL))
            Spacer(Modifier.width(Spacing.S))
            Text(
                text = stringResource(R.string.business_seal_progress),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        progress.criteria.forEach { criterion -> SealRow(criterion) }
    }
}

@Composable
private fun SealRow(criterion: SealCriterion) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.TouchTarget).padding(horizontal = Spacing.L)
    ) {
        Icon(
            Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (criterion.met) Mint else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(Sizes.IconM)
        )
        Spacer(Modifier.width(Spacing.S))
        Text(text = criterion.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            text = stringResource(
                R.string.business_seal_value,
                criterion.current?.compact() ?: stringResource(R.string.business_seal_unknown),
                criterion.goal.compact()
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/** «4.5» o «12». */
private fun Double.compact(): String = if (this % 1.0 ==
    0.0
) {
    toLong().toString()
} else {
    String.format(MexicanSpanish, "%.1f", this)
}

private fun Double.signed(): String = (if (this >= 0) "+" else "") + compact()

private fun Double.signedPercent(): String = (if (this >= 0) "+" else "") + roundToInt()

@Preview(name = "Panel claro")
@Preview(name = "Panel oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BusinessPanelPreview() {
    WhereverTheme { BusinessPanelContent(samplePanelState(), sampleHeader, {}, {}, {}) }
}
