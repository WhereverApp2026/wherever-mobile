package com.wherever.mobile.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.ui.formatDate
import java.time.ZoneOffset

/** Rejilla de 3 columnas de 81_Perfil (celdas de 120 × 112). */
@Composable
internal fun ProfileGrid(tiles: List<ProfileTile>, onOpen: (ProfileTile) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.XS), modifier = Modifier.testTag("profile_grid")) {
        tiles.chunked(GRID_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.XS)) {
                row.forEach { tile -> Tile(tile, onOpen, Modifier.weight(1f)) }
                repeat(GRID_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Tile(tile: ProfileTile, onOpen: (ProfileTile) -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(TILE_RATIO)
            .clip(RoundedCornerShape(Radius.S))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(role = Role.Button, onClickLabel = tile.title) { onOpen(tile) }
            .testTag("profile_tile_${tile.id}")
    ) {
        if (tile.imageUrl != null) {
            AsyncImage(
                model = tile.imageUrl,
                contentDescription = tile.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            // Planes: sin foto en la lista; nombre y fecha sobre el tinte azul.
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.XS, Alignment.Bottom),
                modifier = Modifier.matchParentSize().padding(Spacing.S)
            ) {
                Text(
                    tile.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                tile.subtitle?.let {
                    Text(
                        formatDate("${it}T12:00:00Z", ZoneOffset.UTC),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

private const val GRID_COLUMNS = 3
private const val TILE_RATIO = 120f / 112f
