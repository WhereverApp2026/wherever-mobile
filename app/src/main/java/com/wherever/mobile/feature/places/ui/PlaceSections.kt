package com.wherever.mobile.feature.places.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.WhereverProgressBar
import com.wherever.mobile.feature.places.domain.AspectRatings
import com.wherever.mobile.feature.places.domain.MenuSection
import com.wherever.mobile.feature.places.domain.PlaceDetail
import com.wherever.mobile.feature.places.domain.Traffic

// Secciones de la ficha del lugar (20_FichaNegocio): aspectos, afluencia, menú y servicios.

private const val FIRST_TRAFFIC_HOUR = 7
private const val TRAFFIC_HOURS = 17
private const val MIN_BAR_FRACTION = 0.05f

/** US-068: calificación por aspecto. */
@Composable
internal fun Aspects(aspects: AspectRatings) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        AspectBar(R.string.aspect_taste, aspects.taste)
        AspectBar(R.string.aspect_service, aspects.service)
        AspectBar(R.string.aspect_ambience, aspects.ambience)
        AspectBar(R.string.aspect_wait, aspects.wait)
        AspectBar(R.string.aspect_value, aspects.value)
    }
}

@Composable
private fun AspectBar(label: Int, value: Double) {
    val name = stringResource(label)
    val description = stringResource(R.string.place_aspect_a11y, name, ratingLabel(value))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description }
    ) {
        Text(name, modifier = Modifier.width(Sizes.LabelColumn), style = MaterialTheme.typography.bodyMedium)
        WhereverProgressBar(
            progress = { (value.toFloat() / MAX_RATING).coerceIn(0f, 1f) },
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(Spacing.S))
        Text(ratingLabel(value), style = MaterialTheme.typography.labelLarge)
    }
}

/** US-069: afluencia de hoy de 7:00 a 23:00. */
@Composable
internal fun TrafficBars(traffic: Traffic) {
    if (!traffic.enough) {
        Text(stringResource(R.string.place_traffic_unknown), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val hours = traffic.hours.drop(FIRST_TRAFFIC_HOUR).take(TRAFFIC_HOURS)
    val max = (hours.maxOrNull() ?: 0).coerceAtLeast(1)
    val busiest = FIRST_TRAFFIC_HOUR + hours.indexOf(hours.maxOrNull() ?: 0)
    val description = stringResource(R.string.place_traffic_a11y, busiest)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth().height(Sizes.ChartHeight).semantics { contentDescription = description }
        ) {
            hours.forEachIndexed { index, value ->
                val current = traffic.currentHour == FIRST_TRAFFIC_HOUR + index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(fraction = (value.toFloat() / max).coerceIn(MIN_BAR_FRACTION, 1f))
                        .clip(RoundedCornerShape(topStart = Radius.XS, topEnd = Radius.XS))
                        .background(if (current) RouteBlue else MaterialTheme.colorScheme.outline)
                )
            }
        }
        Text(
            stringResource(R.string.place_traffic_busiest, busiest),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** US-071: menú con precios. */
@Composable
internal fun Menu(sections: List<MenuSection>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
        sections.forEach { section ->
            Text(section.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            section.items.forEach { item ->
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.bodyMedium)
                        if (item.description.isNotBlank()) {
                            Text(
                                item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(stringResource(R.string.place_price, item.price), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
internal fun Features(place: PlaceDetail) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
        items(place.accessibility + place.dietOptions + place.services) { key ->
            StatusPill(featureLabel(key))
        }
    }
}

@Composable
private fun featureLabel(key: String): String = when (key) {
    "silla_ruedas" -> stringResource(R.string.feature_wheelchair)
    "apto_ninos" -> stringResource(R.string.feature_kids)
    "pet_friendly" -> stringResource(R.string.feature_pets)
    "vegetariano" -> stringResource(R.string.feature_vegetarian)
    "vegano" -> stringResource(R.string.feature_vegan)
    "sin_gluten" -> stringResource(R.string.feature_gluten_free)
    else -> key.replace('_', ' ').replaceFirstChar { it.uppercase() }
}
