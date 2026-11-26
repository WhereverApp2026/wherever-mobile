package com.wherever.mobile.feature.places.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.feature.places.domain.tileFor
import com.wherever.mobile.feature.places.domain.tileUrl
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Las teselas de OSM miden 256 px. */
private const val TILE_PX = 256

/**
 * Portada de mapa para un lugar sin foto (specs/nv-21, D-21a y D-21b): teselas de OpenStreetMap a su tamaño
 * real, centradas en `location`, con un pin y la atribución que pide OSM.
 */
@Composable
fun PlaceMapCover(location: GeoPoint, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val tile = remember(location) { tileFor(location.lat, location.lng) }
    val headers = remember(context) { NetworkHeaders.Builder().set("User-Agent", context.packageName).build() }
    BoxWithConstraints(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant).testTag("place_map_cover")
    ) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        // Píxel del lugar dentro de la tesela central; la central se corre para que el lugar quede al centro.
        val centerX = width / 2 - (tile.fx * TILE_PX).roundToInt()
        val centerY = height / 2 - (tile.fy * TILE_PX).roundToInt()
        val left = ceil(centerX.toDouble() / TILE_PX).toInt()
        val right = ceil((width - centerX - TILE_PX).toDouble() / TILE_PX).toInt()
        val top = ceil(centerY.toDouble() / TILE_PX).toInt()
        val bottom = ceil((height - centerY - TILE_PX).toDouble() / TILE_PX).toInt()
        val tileDp: Dp = with(density) { TILE_PX.toDp() }
        for (dy in -top..bottom) {
            for (dx in -left..right) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(tileUrl(tile.zoom, tile.x + dx, tile.y + dy))
                        .httpHeaders(headers)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .offset { IntOffset(centerX + dx * TILE_PX, centerY + dy * TILE_PX) }
                        .size(tileDp)
                )
            }
        }
        Icon(
            Icons.Filled.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            // La punta del pin queda sobre el lugar.
            modifier = Modifier.align(Alignment.Center).offset(y = -Sizes.IconXL / 2).size(Sizes.IconXL)
        )
        Text(
            stringResource(R.string.map_attribution_short),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = ATTRIBUTION_ALPHA))
                .padding(horizontal = Spacing.XS)
        )
    }
}

private const val ATTRIBUTION_ALPHA = 0.8f
