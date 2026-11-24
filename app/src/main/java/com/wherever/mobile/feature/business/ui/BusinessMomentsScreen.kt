package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.WhereverShapes
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatDate
import com.wherever.mobile.feature.business.domain.BusinessMoment
import java.time.ZoneId

/** Dos columnas, como la maqueta. */
private const val GRID_COLUMNS = 2

/** NegMomentos (specs/nv-36). */
@Composable
fun BusinessMomentsRoute(
    businessId: String,
    onBack: () -> Unit,
    viewModel: BusinessMomentsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(businessId) { viewModel.start(businessId) }
    BusinessMomentsContent(
        state,
        BusinessMomentsActions(
            onBack = onBack,
            onTab = viewModel::selectTab,
            onThank = viewModel::thank,
            onFeature = viewModel::toggleFeatured,
            onRetry = viewModel::load,
            onMessageShown = viewModel::messageShown
        )
    )
}

@Composable
internal fun BusinessMomentsContent(
    state: BusinessMomentsUiState,
    actions: BusinessMomentsActions,
    zone: ZoneId = ZoneId.systemDefault()
) {
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            actions.onMessageShown()
        }
    }
    val error = state.error
    FormScreen(footer = { WhereverSnackbarHost(snackbar) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onBack)
            Spacer(Modifier.width(Spacing.M))
            Text(stringResource(R.string.moments_title), style = MaterialTheme.typography.headlineMedium)
        }
        Text(
            stringResource(R.string.moments_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        when {
            state.loading -> ListSkeleton(rows = 3)
            error != null -> ErrorState(error.asString(), actions.onRetry, Modifier.heightIn(max = Sizes.HeroImage))
            else -> {
                SegmentedControl(
                    options = listOf(
                        stringResource(R.string.moments_tab_new, state.newCount),
                        stringResource(R.string.moments_tab_featured, state.featured.size)
                    ),
                    selectedIndex = state.tab.ordinal,
                    onSelect = { actions.onTab(MomentsTab.entries[it]) },
                    modifier = Modifier.testTag("moments_tabs")
                )
                if (state.visible.isEmpty()) {
                    Text(
                        stringResource(
                            if (state.tab ==
                                MomentsTab.NEW
                            ) {
                                R.string.moments_empty_new
                            } else {
                                R.string.moments_empty_featured
                            }
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("moments_empty")
                    )
                }
                state.visible.chunked(GRID_COLUMNS).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
                        row.forEach { moment ->
                            MomentTile(moment, moment.id in state.busy, zone, actions, Modifier.weight(1f))
                        }
                        repeat(GRID_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                Text(
                    stringResource(R.string.moments_privacy),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MomentTile(
    moment: BusinessMoment,
    busy: Boolean,
    zone: ZoneId,
    actions: BusinessMomentsActions,
    modifier: Modifier
) {
    Column(modifier = modifier.testTag("moment_${moment.id}"), verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(WhereverShapes.Card)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (moment.imageUrl != null) {
                AsyncImage(
                    model = moment.imageUrl,
                    contentDescription = moment.text,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                )
            }
            if (!moment.thanked) {
                StatusPill(
                    stringResource(R.string.moments_badge_new),
                    container = Sunset,
                    contentColor = Ink,
                    modifier = Modifier.padding(Spacing.S)
                )
            }
        }
        Text(
            stringResource(R.string.biz_reviews_author, moment.author, formatDate(moment.createdAt, zone)),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            WhereverTextLink(
                text = stringResource(if (moment.thanked) R.string.moments_thanked_label else R.string.moments_thank),
                onClick = { actions.onThank(moment) },
                enabled = !moment.thanked && !busy,
                modifier = Modifier.testTag("moment_thank_${moment.id}")
            )
            Spacer(Modifier.weight(1f))
            Switch(
                checked = moment.featured,
                onCheckedChange = { actions.onFeature(moment) },
                enabled = !busy,
                modifier = Modifier.testTag("moment_feature_${moment.id}")
            )
        }
    }
}

@Preview(name = "NegMomentos claro")
@Preview(name = "NegMomentos oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BusinessMomentsPreview() {
    WhereverTheme {
        BusinessMomentsContent(
            BusinessMomentsUiState(
                loading = false,
                all = sampleMoments(),
                featured = sampleMoments().filter {
                    it.featured
                }
            ),
            BusinessMomentsActions(),
            ZoneId.of("America/Mexico_City")
        )
    }
}
