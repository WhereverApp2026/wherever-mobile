package com.wherever.mobile.feature.plans.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.RouteBlueTint
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.ui.formatTime
import com.wherever.mobile.feature.places.ui.categoryLabel
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.PlanStop
import com.wherever.mobile.feature.plans.domain.Transfer
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.util.Locale

private const val MINUTES_PER_HOUR = 60

/** «1 h 30», «2 h» o «45 min». */
@Composable
internal fun durationText(minutes: Int): String {
    val hours = minutes / MINUTES_PER_HOUR
    val rest = minutes % MINUTES_PER_HOUR
    return when {
        hours == 0 -> stringResource(R.string.plan_minutes, rest)
        rest == 0 -> stringResource(R.string.plan_hours, hours)
        else -> stringResource(R.string.plan_hours_minutes, hours, rest)
    }
}

/** «Gratis», «$$» (nivel de precio) o «$120» (precio de la parada). */
@Composable
internal fun priceText(price: Int, priceLevel: Int): String = when {
    price == 0 && priceLevel == 0 -> stringResource(R.string.plan_free)
    priceLevel > 0 -> "$".repeat(priceLevel)
    else -> stringResource(R.string.place_price, price)
}

/** Minutos del plan, de la primera llegada al final de la última parada. */
internal fun totalMinutes(stops: List<PlanStop>): Int {
    val first = stops.minByOrNull { it.arrives } ?: return 0
    val last = stops.maxByOrNull { it.arrives } ?: return 0
    return try {
        Duration.between(Instant.parse(first.arrives), Instant.parse(last.arrives)).toMinutes().toInt() + last.minutes
    } catch (_: DateTimeParseException) {
        stops.sumOf { it.minutes }
    }
}

/** Resumen de 39_Plan: paradas, horas en total, traslados y costo por persona. */
@Composable
internal fun PlanSummaryRow(stops: List<PlanStop>, transfers: List<Transfer>, costPerPerson: Int) {
    val total = totalMinutes(stops)
    val items = listOf(
        stops.size.toString() to stringResource(R.string.plan_summary_stops),
        durationText(total) to stringResource(R.string.plan_summary_total),
        stringResource(R.string.plan_minutes, transfers.sumOf { it.minutes }) to
            stringResource(R.string.plan_summary_transfers),
        stringResource(R.string.place_price, costPerPerson) to stringResource(R.string.plan_summary_per_person)
    )
    SurfaceCard(modifier = Modifier.testTag("plan_summary")) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = Spacing.XS)) {
            items.forEachIndexed { index, (value, label) ->
                if (index > 0) VerticalDivider(color = MaterialTheme.colorScheme.outline)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(
                        value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Foto redonda del lugar; sin foto, su inicial sobre azul claro. */
@Composable
internal fun PlaceAvatar(name: String, photoUrl: String?, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(Sizes.AvatarList).clip(CircleShape).background(RouteBlueTint)
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(Sizes.AvatarList)
            )
        } else {
            Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, color = RouteBlue)
        }
    }
}

/**
 * Paradas de 39_Plan: foto, tarjeta con hora, duración y giro, y el traslado a la siguiente.
 * [actions] dibuja Cambiar y Quitar a la derecha de cada tarjeta.
 */
@Composable
internal fun PlanItinerary(
    stops: List<PlanStop>,
    transfers: List<Transfer>,
    mobility: Mobility,
    zone: ZoneId,
    actions: @Composable (PlanStop, Int) -> Unit
) {
    val ordered = stops.sortedBy { it.arrives }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S), modifier = Modifier.testTag("plan_itinerary")) {
        ordered.forEachIndexed { index, stop ->
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PlaceAvatar(stop.name, stop.photoUrl)
                    if (index < ordered.lastIndex) {
                        Box(
                            Modifier.width(Strokes.Thick).fillMaxHeight().padding(top = Spacing.S)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                    }
                }
                Spacer(Modifier.width(Spacing.M))
                ItineraryCard(stop, index, zone, actions, Modifier.weight(1f))
            }
            val next = ordered.getOrNull(index + 1)
            val transfer = next?.let { n -> transfers.firstOrNull { it.fromStopId == stop.id && it.toStopId == n.id } }
            if (transfer != null) TransferPill(transfer, mobility)
        }
    }
}

@Composable
private fun ItineraryCard(
    stop: PlanStop,
    index: Int,
    zone: ZoneId,
    actions: @Composable (PlanStop, Int) -> Unit,
    modifier: Modifier
) {
    SurfaceCard(modifier = modifier.testTag("plan_stop_$index")) {
        Row(modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.S)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatTime(stop.arrives, zone),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RouteBlue
                    )
                    Spacer(Modifier.width(Spacing.S))
                    val category = stop.category.takeIf { it.isNotBlank() }?.let { stringResource(categoryLabel(it)) }
                    Text(
                        listOfNotNull(durationText(stop.minutes), category).joinToString(" · "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    stop.name.ifBlank { stringResource(R.string.plan_stop_unknown) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    priceText(stop.price, stop.priceLevel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) { actions(stop, index) }
        }
    }
}

/** «8 min a pie · 0.6 km», en una píldora azul clara bajo la tarjeta. */
@Composable
private fun TransferPill(transfer: Transfer, mobility: Mobility) {
    Surface(
        shape = CircleShape,
        color = RouteBlueTint,
        contentColor = RouteBlue,
        modifier = Modifier.padding(start = Sizes.AvatarList + Spacing.M)
    ) {
        Text(
            stringResource(
                R.string.plan_transfer_pill,
                transfer.minutes,
                stringResource(mobilityLabel(mobility)).lowercase(Locale.forLanguageTag("es-MX")),
                String.format(Locale.US, "%.1f", transfer.km)
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.heightIn(min = Sizes.IconL).padding(horizontal = Spacing.M, vertical = Spacing.XS)
        )
    }
}
