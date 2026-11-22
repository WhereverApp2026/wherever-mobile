package com.wherever.mobile.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.RouteBlueTint
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverShapes
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.feature.feed.ui.FeedItem
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.places.ui.ratingLabel
import com.wherever.mobile.feature.profile.ui.ProfileAvatar
import kotlin.math.ceil

// Parte baja de las tarjetas con foto en Figma: tinta 0 % → 78 %.
private const val CARD_SCRIM = 0.78f
private const val SCRIM_START = 0.4f

/** Unos 80 m por minuto a pie. */
private const val METERS_PER_WALK_MINUTE = 80.0

/** Minutos a pie redondeados hacia arriba (mínimo 1). */
internal fun walkMinutes(meters: Double): Int = ceil(meters / METERS_PER_WALK_MINUTE).toInt().coerceAtLeast(1)

/** "Cerca de ti" con "Ver todo", o "Planes de tus amigos" con "Ver más". */
@Composable
internal fun SectionHeader(title: String, action: String, onAction: () -> Unit, tag: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        WhereverTextLink(action, onAction, modifier = Modifier.testTag(tag))
    }
}

/** Carrusel "Cerca de ti" (10_Inicio). Mantener presionada una tarjeta = "No me interesa" (US-053). */
@Composable
internal fun NearbyCarousel(places: List<PlaceCard>, onPlace: (String) -> Unit, onNotInterested: (PlaceCard) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = Modifier.testTag("home_nearby")
    ) {
        items(places, key = { it.id }) { place ->
            NearbyPlaceCard(
                place = place,
                onClick = { onPlace(place.id) },
                onNotInterested = { onNotInterested(place) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

/** Tarjeta-foto de 160 × 184: etiqueta "Para ti" o "Cerca", nombre y "8 min a pie · 4.8". */
@Composable
internal fun NearbyPlaceCard(
    place: PlaceCard,
    onClick: () -> Unit,
    onNotInterested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notInterested = stringResource(R.string.home_not_interested)
    Box(
        modifier = modifier
            .size(width = Sizes.NearbyCardWidth, height = Sizes.NearbyCardHeight)
            .clip(WhereverShapes.Card)
            .background(RouteBlueTint)
            .combinedClickable(onClick = onClick, onLongClick = onNotInterested)
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(notInterested) {
                        onNotInterested()
                        true
                    }
                )
            }
            .testTag("home_place_${place.id}")
    ) {
        if (place.coverUrl != null) {
            AsyncImage(
                model = place.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Icon(
                Icons.Outlined.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center).size(Sizes.IconXL)
            )
        }
        Box(
            modifier = Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    SCRIM_START to Color.Transparent,
                    1f to Ink.copy(alpha = CARD_SCRIM)
                )
            )
        )
        PhotoTag(
            stringResource(if (place.why != null) R.string.home_tag_for_you else R.string.home_tag_near),
            Modifier.align(Alignment.TopStart).padding(Spacing.S)
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(Spacing.M)) {
            Text(
                place.name,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                nearbyMeta(place),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun nearbyMeta(place: PlaceCard): String {
    val rating = ratingLabel(place.rating)
    return place.distanceMeters?.let { meters ->
        val minutes = walkMinutes(meters)
        pluralStringResource(R.plurals.home_walk_minutes, minutes, minutes) + " · " + rating
    } ?: rating
}

@Composable
private fun PhotoTag(text: String, modifier: Modifier = Modifier) {
    Surface(shape = CircleShape, color = Color.White, contentColor = Ink, modifier = modifier) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = Spacing.S, vertical = Spacing.XS)
        )
    }
}

/** "Planes de tus amigos" (10_Inicio): momentos de amigos que traen un plan. */
@Composable
internal fun FriendPlansCarousel(items: List<FeedItem>, onPlan: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = Modifier.testTag("home_friend_plans")
    ) {
        items(items, key = { it.moment.id }) { item ->
            item.moment.planId?.let { planId -> FriendPlanCard(item) { onPlan(planId) } }
        }
    }
}

@Composable
private fun FriendPlanCard(item: FeedItem, onClick: () -> Unit) {
    val moment = item.moment
    val author = item.authorName ?: moment.author.username ?: stringResource(R.string.feed_a_friend)
    SurfaceCard(
        modifier = Modifier
            .width(Sizes.FriendPlanCardWidth)
            .clickable(onClick = onClick)
            .testTag("home_friend_plan_${moment.id}")
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(Sizes.FriendPlanPhoto).background(RouteBlueTint)) {
            moment.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
            Box(
                modifier = Modifier.matchParentSize().background(
                    Brush.verticalGradient(SCRIM_START to Color.Transparent, 1f to Ink.copy(alpha = CARD_SCRIM))
                )
            )
            ProfileAvatar(
                author,
                item.authorPhoto,
                size = Sizes.IconXL,
                modifier = Modifier.align(Alignment.TopStart).padding(Spacing.M)
            )
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(Spacing.M)) {
                Text(
                    item.placeName ?: moment.text ?: stringResource(R.string.feed_see_plan),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    listOfNotNull(author, item.time?.asString()).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.M, vertical = Spacing.S)
        ) {
            Text(
                if (item.placeName != null) moment.text.orEmpty() else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    stringResource(R.string.feed_see_plan),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.S)
                )
            }
        }
    }
}
