package com.wherever.mobile.feature.reviews.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.InitialAvatar
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.ui.formatDate
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.feature.reviews.domain.MAX_STARS
import java.time.ZoneId
import java.time.ZoneOffset

/** Zona de los días que manda la API (check-ins y vencimientos). */
private val API_ZONE: ZoneId = ZoneId.of("America/Mexico_City")

/** Largo de `AAAA-MM-DD`. */
private const val DAY_LENGTH = 10

/** «d de MMMM de yyyy» para un día (`AAAA-MM-DD`) o una fecha ISO de la API. */
internal fun reviewDate(value: String): String = if (value.length == DAY_LENGTH) {
    formatDate("${value}T12:00:00Z", ZoneOffset.UTC)
} else {
    formatDate(value, API_ZONE)
}

@StringRes
internal fun Aspect.label(): Int = when (this) {
    Aspect.TASTE -> R.string.reviews_aspect_taste
    Aspect.SERVICE -> R.string.reviews_aspect_service
    Aspect.AMBIENCE -> R.string.reviews_aspect_ambience
    Aspect.WAIT -> R.string.reviews_aspect_wait
    Aspect.VALUE -> R.string.reviews_aspect_value
}

/** Estrellas que se tocan (áreas de 48 dp). `tag` + número identifica cada una en las pruebas. */
@Composable
internal fun StarInput(
    value: Int,
    onChange: (Int) -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
    size: Dp = Sizes.IconXL
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (star in 1..MAX_STARS) {
            val description = stringResource(R.string.reviews_star_a11y, star)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Sizes.TouchTarget)
                    .selectable(selected = star == value, role = Role.RadioButton) { onChange(star) }
                    .semantics { contentDescription = description }
                    .testTag("${tag}_$star")
            ) {
                StarIcon(filled = star <= value, size = size)
            }
        }
    }
}

/** Estrellas de solo lectura. */
@Composable
internal fun StarsRow(rating: Int, modifier: Modifier = Modifier, size: Dp = Sizes.IconS) {
    val description = stringResource(R.string.reviews_star_a11y, rating)
    Row(modifier = modifier.semantics { contentDescription = description }) {
        for (star in 1..MAX_STARS) StarIcon(filled = star <= rating, size = size)
    }
}

@Composable
private fun StarIcon(filled: Boolean, size: Dp) {
    Icon(
        imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
        contentDescription = null,
        tint = if (filled) Gold else MaterialTheme.colorScheme.outline,
        modifier = Modifier.size(size)
    )
}

/** Foto del lugar o su inicial, como en las listas de la maqueta. */
@Composable
internal fun PlaceThumb(name: String, photoUrl: String?, modifier: Modifier = Modifier, size: Dp = Sizes.AvatarList) {
    if (photoUrl.isNullOrBlank()) {
        InitialAvatar(name.ifBlank { "?" }, size, modifier)
    } else {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(RoundedCornerShape(Radius.M))
        )
    }
}

/** Recuadro «Respuesta del negocio». */
@Composable
internal fun BusinessReply(title: String, text: String, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.XS),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.M))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(Spacing.M)
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
internal fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}
