package com.wherever.mobile.feature.places.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.CreateOption
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.MainTab
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.WhereverBottomBar
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.feature.places.domain.SortOrder

private const val MAX_PRICE_FILTER = 3
private val PRICE_LEVELS = (1..MAX_PRICE_FILTER).toList()

/** 18_Explorar (US-027, US-028, US-034, US-187). Sin mapa: falta decidir el proveedor (D25). */
@Composable
fun ExploreRoute(
    onTab: (MainTab) -> Unit,
    onCreate: (CreateOption) -> Unit,
    onPlace: (String) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(bottomBar = { WhereverBottomBar(MainTab.EXPLORE, onSelect = onTab, onCreate = onCreate) }) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .testTag("explore_list")
        ) {
            item { SearchHeader(state.query, viewModel::onQueryChange) }
            item { FilterChips(state, viewModel) }
            if (!state.searching && state.filters.isEmpty) {
                if (state.recentSearches.isNotEmpty()) item { RecentSearches(state, viewModel) }
                if (state.recentlyViewed.isNotEmpty()) item { RecentlyViewed(state, onPlace) }
            }
            when {
                state.loading -> item { LoadingState(Modifier.padding(Spacing.XXXL)) }
                state.error != null && state.results.isEmpty() -> item {
                    ErrorState(
                        state.error?.asString().orEmpty(),
                        onRetry = viewModel::retry,
                        Modifier.padding(Spacing.L)
                    )
                }
                state.showEmpty -> item { EmptyResults(viewModel::clearFilters) }
                else -> {
                    items(state.results, key = { it.id }) { place ->
                        PlaceCardItem(
                            place = place,
                            onClick = { onPlace(place.id) },
                            modifier = Modifier.padding(horizontal = ScreenPadding)
                        )
                    }
                    if (state.nextCursor != null && !state.searching) {
                        item {
                            WhereverOutlinedButton(
                                text = stringResource(R.string.explore_load_more),
                                onClick = viewModel::loadMore,
                                enabled = !state.loadingMore,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding),
                                tag = "explore_more"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(query: String, onQueryChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = ScreenPadding, vertical = Spacing.S)) {
        Text(
            stringResource(R.string.explore_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        LabeledTextField(
            label = stringResource(R.string.explore_search_label),
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(R.string.explore_search_hint),
            leadingIcon = Icons.Outlined.Search,
            trailingContent = if (query.isNotEmpty()) {
                {
                    WhereverIconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Outlined.Clear, contentDescription = stringResource(R.string.explore_search_clear))
                    }
                }
            } else {
                null
            },
            tag = "explore_search"
        )
    }
}

@Composable
private fun FilterChips(state: ExploreUiState, viewModel: ExploreViewModel) {
    val filters = state.filters
    LazyRow(
        contentPadding = PaddingValues(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.S)
    ) {
        item {
            SelectChip(
                stringResource(R.string.explore_filter_open),
                filters.openNow,
                viewModel::onOpenNow,
                tag = "filter_open"
            )
        }
        if (state.hasLocation) {
            item {
                SelectChip(
                    stringResource(R.string.explore_filter_nearby),
                    filters.radiusMeters != null,
                    viewModel::onNearby,
                    tag = "filter_nearby"
                )
            }
        }
        items(PRICE_LEVELS) { level ->
            SelectChip(
                stringResource(R.string.explore_filter_price, priceSymbols(level)),
                filters.maxPriceLevel == level,
                { viewModel.onMaxPrice(level) },
                tag = "filter_price_$level"
            )
        }
        item {
            SelectChip(
                stringResource(R.string.explore_order_rating),
                filters.order == SortOrder.RATING,
                { viewModel.onOrder(SortOrder.RATING) },
                tag = "filter_order_rating"
            )
        }
        items(state.categories, key = { it.id }) { category ->
            SelectChip(
                category.name,
                filters.category == category.id,
                { viewModel.onCategory(category.id) },
                tag = "filter_category_${category.id}"
            )
        }
    }
}

@Composable
private fun RecentSearches(state: ExploreUiState, viewModel: ExploreViewModel) {
    Column(
        modifier = Modifier.padding(horizontal = ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.S)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.explore_recent_searches),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            WhereverTextLink(stringResource(R.string.explore_clear_recent), viewModel::clearRecentSearches)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
            items(state.recentSearches) { recent ->
                SelectChip(recent.text, selected = false, onClick = { viewModel.onRecentSearch(recent.text) })
            }
        }
    }
}

@Composable
private fun RecentlyViewed(state: ExploreUiState, onPlace: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        Text(
            stringResource(R.string.explore_recently_viewed),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = ScreenPadding)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(Spacing.M)
        ) {
            items(state.recentlyViewed, key = { "recent_${it.id}" }) { place ->
                PlaceCardItem(place = place, onClick = {
                    onPlace(place.id)
                }, modifier = Modifier.width(Sizes.EventCardWidth))
            }
        }
    }
}

@Composable
private fun EmptyResults(onClear: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = Modifier.fillMaxWidth().padding(Spacing.XXXL).testTag("explore_empty")
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.explore_empty_title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.explore_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        WhereverOutlinedButton(text = stringResource(R.string.explore_empty_action), onClick = onClear)
    }
}
