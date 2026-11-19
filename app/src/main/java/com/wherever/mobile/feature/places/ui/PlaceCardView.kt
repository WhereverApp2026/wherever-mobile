package com.wherever.mobile.feature.places.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.RouteBlueTint
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.feature.places.domain.PlaceCard
import java.util.Locale

private const val METERS_PER_KM = 1000.0
private const val MAX_PRICE_LEVEL = 4

@StringRes
fun categoryLabel(id: String): Int = when (id) {
    "restaurante" -> R.string.category_restaurant
    "cafeteria" -> R.string.category_cafe
    "bar" -> R.string.category_bar
    "entretenimiento" -> R.string.category_entertainment
    "servicios" -> R.string.category_services
    "comercio" -> R.string.category_shop
    else -> R.string.category_other
}

/** `$` a `$$$$` según `nivel_precio` (1 a 4). */
fun priceSymbols(level: Int): String = "$".repeat(level.coerceIn(1, MAX_PRICE_LEVEL))

/** "350 m" o "1.2 km". */
fun distanceLabel(meters: Double): String = if (meters < METERS_PER_KM) {
    "${meters.toInt()} m"
} else {
    String.format(Locale.US, "%.1f km", meters / METERS_PER_KM)
}

fun ratingLabel(rating: Double): String = String.format(Locale.US, "%.1f", rating)

/** Foto de portada o un fondo con ícono si el negocio no tiene foto. */
@Composable
fun PlacePhoto(url: String?, height: Dp, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(RouteBlueTint)
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(height)
            )
        } else {
            Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun RatingRow(rating: Double, reviews: Int, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.place_rating_a11y, ratingLabel(rating), reviews)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description }
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
        Text(ratingLabel(rating), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Text(
            stringResource(R.string.place_reviews_count, reviews),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Una línea: giro · $$ · 1.2 km · Abierto. */
@Composable
fun PlaceMetaLine(category: String, priceLevel: Int, distanceMeters: Double?, openNow: Boolean) {
    val parts = buildList {
        add(stringResource(categoryLabel(category)))
        if (priceLevel > 0) add(priceSymbols(priceLevel))
        distanceMeters?.let { add(distanceLabel(it)) }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            parts.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Text(
            stringResource(if (openNow) R.string.place_open else R.string.place_closed),
            style = MaterialTheme.typography.labelMedium,
            color = if (openNow) Mint else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Tarjeta de lugar para listas. `extra` va debajo (p. ej. "por qué" y "No me interesa" en Inicio). */
@Composable
fun PlaceCardItem(
    place: PlaceCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extra: @Composable (() -> Unit)? = null
) {
    SurfaceCard(modifier = modifier.testTag("place_card_${place.id}")) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.clickable(onClick = onClick).padding(12.dp)
        ) {
            PlacePhoto(place.coverUrl, height = 132.dp)
            Text(
                place.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            PlaceMetaLine(place.category, place.priceLevel, place.distanceMeters, place.openNow)
            RatingRow(place.rating, place.reviews)
            extra?.invoke()
        }
    }
}

/** Esqueleto de la tarjeta mientras carga (US-025). TalkBack lo anuncia como "Cargando". */
@Composable
fun PlaceCardSkeleton(modifier: Modifier = Modifier) {
    val loading = stringResource(R.string.common_loading)
    SurfaceCard(modifier = modifier.semantics { contentDescription = loading }.testTag("place_skeleton")) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(12.dp)) {
            val shade = MaterialTheme.colorScheme.surfaceVariant
            Box(Modifier.fillMaxWidth().height(132.dp).clip(RoundedCornerShape(16.dp)).background(shade))
            Box(Modifier.fillMaxWidth(fraction = 0.6f).height(18.dp).clip(RoundedCornerShape(6.dp)).background(shade))
            Box(Modifier.fillMaxWidth(fraction = 0.4f).height(14.dp).clip(RoundedCornerShape(6.dp)).background(shade))
        }
    }
}
