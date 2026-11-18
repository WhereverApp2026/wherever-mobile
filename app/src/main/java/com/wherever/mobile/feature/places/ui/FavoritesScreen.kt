package com.wherever.mobile.feature.places.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.feature.places.domain.FavoritePlace

/** 35_Favoritos (US-029). Se recarga al volver de una ficha. */
@Composable
fun FavoritesRoute(onBack: () -> Unit, onPlace: (String) -> Unit, viewModel: FavoritesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.load()
        onPauseOrDispose { }
    }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        TopTitleRow(stringResource(R.string.favorites_title), onBack, Modifier.padding(ScreenPadding))
        state.error?.let { ErrorBanner(it.asString(), Modifier.padding(horizontal = ScreenPadding)) }
        when {
            state.loading -> LoadingState()
            state.items.isEmpty() -> Text(
                stringResource(R.string.favorites_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(ScreenPadding).testTag("favorites_empty")
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.id }) { favorite ->
                    FavoriteRow(favorite, onClick = {
                        onPlace(favorite.id)
                    }, onRemove = { viewModel.remove(favorite.id) })
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(favorite: FavoritePlace, onClick: () -> Unit, onRemove: () -> Unit) {
    SurfaceCard(modifier = Modifier.testTag("favorite_${favorite.id}")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp)
        ) {
            PlacePhoto(favorite.photoUrl, height = 64.dp, modifier = Modifier.width(64.dp))
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(favorite.name, style = MaterialTheme.typography.titleSmall)
                favorite.category?.let {
                    Text(
                        stringResource(categoryLabel(it)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = stringResource(R.string.place_favorite_remove),
                    tint = Sunset
                )
            }
        }
    }
}
