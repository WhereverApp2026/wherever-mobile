package com.wherever.mobile.feature.plans.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.ui.formatTime
import com.wherever.mobile.feature.places.ui.categoryLabel
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.PlanParameters
import com.wherever.mobile.feature.plans.domain.PlanStop
import com.wherever.mobile.feature.plans.domain.Transfer
import java.time.ZoneId
import java.util.Locale

@StringRes
internal fun companyLabel(company: Company): Int = when (company) {
    Company.SOLO -> R.string.plan_company_solo
    Company.FRIENDS -> R.string.plan_company_friends
    Company.COUPLE -> R.string.plan_company_couple
    Company.FAMILY -> R.string.plan_company_family
}

@StringRes
internal fun priceLabel(price: PriceLevel): Int = when (price) {
    PriceLevel.LOW -> R.string.plan_price_low
    PriceLevel.MEDIUM -> R.string.plan_price_medium
    PriceLevel.HIGH -> R.string.plan_price_high
}

@StringRes
internal fun mobilityLabel(mobility: Mobility): Int = when (mobility) {
    Mobility.WALK -> R.string.mobility_walk
    Mobility.BIKE -> R.string.mobility_bike
    Mobility.CAR -> R.string.mobility_car
    Mobility.TRANSIT -> R.string.mobility_transit
}

@StringRes
internal fun moodLabel(mood: Mood): Int = when (mood) {
    Mood.HAPPY -> R.string.mood_happy
    Mood.RELAXED -> R.string.mood_relaxed
    Mood.ROMANTIC -> R.string.mood_romantic
    Mood.ADVENTUROUS -> R.string.mood_adventurous
}

/** US-041: lo que la IA entendió de la frase. */
@Composable
internal fun UnderstoodChips(params: PlanParameters, people: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        Text(stringResource(R.string.plan_understood), style = MaterialTheme.typography.labelLarge)
        ChipGroup {
            if (params.company.isNotBlank()) StatusPill(params.company.replaceFirstChar { it.uppercase() })
            StatusPill(stringResource(R.string.plan_people, people))
            if (params.maxBudget > 0) StatusPill(stringResource(R.string.plan_budget_up_to, params.maxBudget))
            if (params.time.isNotBlank()) StatusPill(params.time)
            if (params.withKids) StatusPill(stringResource(R.string.plan_with_kids))
            if (params.withPet) StatusPill(stringResource(R.string.plan_with_pet))
        }
    }
}

/** US-066: costo total y por persona, siempre el que calcula la API. */
@Composable
internal fun CostSummary(total: Int, perPerson: Int, people: Int) {
    SurfaceCard(modifier = Modifier.testTag("plan_cost")) {
        Row(modifier = Modifier.fillMaxWidth().padding(Spacing.L), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(stringResource(R.string.plan_total), style = MaterialTheme.typography.labelMedium)
                Text(
                    stringResource(R.string.place_price, total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.plan_per_person, people), style = MaterialTheme.typography.labelMedium)
                Text(
                    stringResource(R.string.place_price, perPerson),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * US-055: línea de tiempo con el traslado entre paradas. `actions` agrega los botones de edición
 * debajo de cada parada (solo en el plan guardado).
 */
@Composable
internal fun Timeline(
    stops: List<PlanStop>,
    transfers: List<Transfer>,
    mobility: Mobility,
    actions: (@Composable (PlanStop, Int) -> Unit)? = null
) {
    val zone = ZoneId.systemDefault()
    val ordered = stops.sortedBy { it.arrives }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S), modifier = Modifier.testTag("plan_timeline")) {
        ordered.forEachIndexed { index, stop ->
            StopCard(stop, index, zone, actions)
            val next = ordered.getOrNull(index + 1)
            val transfer = next?.let { n -> transfers.firstOrNull { it.fromStopId == stop.id && it.toStopId == n.id } }
            if (transfer != null) TransferRow(transfer, mobility)
        }
    }
}

@Composable
private fun StopCard(stop: PlanStop, index: Int, zone: ZoneId, actions: (@Composable (PlanStop, Int) -> Unit)?) {
    SurfaceCard(modifier = Modifier.testTag("plan_stop_$index")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(Sizes.StepBadge).clip(CircleShape).background(RouteBlue)
                ) {
                    Text(
                        (index + 1).toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(Modifier.width(Spacing.M))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stop.name.ifBlank { stringResource(R.string.plan_stop_unknown) },
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (stop.category.isNotBlank()) {
                        Text(
                            stringResource(categoryLabel(stop.category)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    formatTime(stop.arrives, zone),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                stringResource(R.string.plan_stop_detail, stop.minutes, stop.price),
                style = MaterialTheme.typography.bodySmall
            )
            stop.why?.let {
                Text(stringResource(R.string.home_why, it), style = MaterialTheme.typography.bodySmall, color = Mint)
            }
            actions?.invoke(stop, index)
        }
    }
}

@Composable
private fun TransferRow(transfer: Transfer, mobility: Mobility) {
    val label = stringResource(
        R.string.plan_transfer,
        stringResource(mobilityLabel(mobility)),
        transfer.minutes,
        String.format(Locale.US, "%.1f", transfer.km)
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        // La línea queda bajo el centro del círculo numerado y el texto, alineado con el de la parada.
        modifier = Modifier.padding(start = Spacing.L + Sizes.StepBadge / 2 - Strokes.Thin / 2)
            .semantics(mergeDescendants = true) { }
    ) {
        Box(Modifier.width(Strokes.Thin).height(Sizes.StepBadge).background(MaterialTheme.colorScheme.outline))
        Spacer(Modifier.width(Sizes.StepBadge / 2 + Spacing.M - Strokes.Thin / 2))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
