package com.wherever.mobile.feature.memories.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.InkMuted
import com.wherever.mobile.core.designsystem.Mist
import com.wherever.mobile.core.designsystem.OutlineSoft
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.ui.formatDate
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import java.time.ZoneId
import java.time.ZoneOffset

/** Proporción de la foto en la vista previa de 72_Postal y 75_PostalVer. */
private const val PHOTO_RATIO = 4f / 3f

/** Proporción de la postal tipo pasaporte (la imagen final es 4:5). */
private const val PASSPORT_RATIO = 4f / 5f

private const val DAY_LENGTH = 10

/** «d de MMMM de yyyy» para un día (`AAAA-MM-DD`) o una fecha ISO de la API. */
internal fun memoryDate(value: String): String = when {
    value.isBlank() -> ""
    value.length == DAY_LENGTH -> formatDate("${value}T12:00:00Z", ZoneOffset.UTC)
    else -> formatDate(value, ZoneId.of("America/Mexico_City"))
}

@StringRes
internal fun PostcardStyle.label(): Int = when (this) {
    PostcardStyle.CLASSIC -> R.string.memories_style_classic
    PostcardStyle.POLAROID -> R.string.memories_style_polaroid
    PostcardStyle.PASSPORT -> R.string.memories_style_passport
}

/**
 * Vista previa de la postal en el estilo elegido, como en 72_Postal: foto, «Saludos desde…», lugar y fecha, y
 * el sello. Es lo mismo que dibuja `AndroidPostcardRenderer` para subirla.
 */
@Composable
internal fun PostcardPreview(
    photo: Any?,
    style: PostcardStyle,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    when (style) {
        PostcardStyle.PASSPORT -> Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(PASSPORT_RATIO)
                .clip(RoundedCornerShape(Radius.XL))
                .background(Ink)
        ) {
            Photo(photo, Modifier.fillMaxSize())
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Ink.copy(alpha = SHADE))
                    .padding(Spacing.L)
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Mist)
                if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Mist)
            }
            Stamp(Sunset, Modifier.align(Alignment.TopEnd).padding(Spacing.L))
        }
        else -> Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
            horizontalAlignment = if (style ==
                PostcardStyle.POLAROID
            ) {
                Alignment.CenterHorizontally
            } else {
                Alignment.Start
            },
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.XL))
                // La postal es papel: blanca en claro y en oscuro, con borde fino como en 72_Postal.
                .background(Color.White)
                .border(Strokes.Hairline, OutlineSoft, RoundedCornerShape(Radius.XL))
                .padding(Spacing.M)
        ) {
            Photo(
                photo,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (style == PostcardStyle.POLAROID) 1f else PHOTO_RATIO)
                    .clip(RoundedCornerShape(Radius.M))
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = if (style == PostcardStyle.POLAROID) {
                        Alignment.CenterHorizontally
                    } else {
                        Alignment.Start
                    }
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        textAlign = if (style == PostcardStyle.POLAROID) TextAlign.Center else TextAlign.Start
                    )
                    if (subtitle.isNotBlank()) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = InkMuted)
                    }
                }
                if (style == PostcardStyle.CLASSIC) Stamp(RouteBlue)
            }
        }
    }
}

/** Alfa de la franja oscura bajo el texto de la postal pasaporte. */
private const val SHADE = 0.6f

@Composable
private fun Photo(photo: Any?, modifier: Modifier) {
    // El fondo se ve mientras carga o si la imagen falla.
    Box(modifier.background(InkMuted)) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun Stamp(color: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .sizeIn(minWidth = Sizes.BadgeL, minHeight = Sizes.BadgeL)
            .border(Strokes.Thin, color, RoundedCornerShape(Radius.S))
            .padding(Spacing.XS)
    ) {
        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = color, modifier = Modifier.size(Sizes.IconM))
        // Sin cortar: en el teléfono «Wherever» se partía en «Whereve / r» dentro del sello de 64 dp.
        Text(
            stringResource(R.string.memories_stamp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            softWrap = false
        )
    }
}

/** Miniatura de una postal del álbum: foto, nombre y fecha. */
@Composable
internal fun AlbumThumb(photo: String?, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
        Photo(
            photo,
            Modifier.fillMaxWidth().height(Sizes.CardImage).clip(RoundedCornerShape(Radius.M))
        )
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
