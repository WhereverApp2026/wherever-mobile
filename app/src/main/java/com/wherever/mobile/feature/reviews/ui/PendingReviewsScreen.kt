package com.wherever.mobile.feature.reviews.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SunsetTint
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.reviews.domain.PendingReview
import com.wherever.mobile.feature.reviews.domain.ReviewedVisit

/** 77_ResPend (specs/nv-32). */
@Composable
fun PendingReviewsRoute(
    onBack: () -> Unit,
    onReview: (PendingReview) -> Unit,
    viewModel: PendingReviewsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PendingReviewsContent(state, onBack, onReview, viewModel::load)
}

@Composable
internal fun PendingReviewsContent(
    state: PendingReviewsUiState,
    onBack: () -> Unit,
    onReview: (PendingReview) -> Unit,
    onRetry: () -> Unit
) {
    val error = state.error
    FormScreen {
        Column {
            TopTitleRow(stringResource(R.string.reviews_pending_title), onBack)
            Text(
                stringResource(R.string.reviews_pending_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        InfoBanner(
            stringResource(R.string.reviews_pending_notice),
            icon = Icons.Outlined.Lock,
            container = SunsetTint,
            contentColor = Ink
        )
        when {
            state.loading -> ListSkeleton(rows = 3)
            error != null -> ErrorState(error.asString(), onRetry)
            state.empty -> Text(
                stringResource(R.string.reviews_pending_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("pending_empty")
            )
            else -> {
                if (state.pending.isNotEmpty()) {
                    SectionTitle(stringResource(R.string.reviews_waiting))
                    PendingList(state.pending, onReview)
                }
                if (state.reviewed.isNotEmpty()) {
                    SectionTitle(stringResource(R.string.reviews_already_reviewed))
                    state.reviewed.forEach { ReviewedRow(it) }
                }
            }
        }
    }
}

/** Filas «Esperando tu opinión»; también las usa la pestaña Por reseñar de 79_MisRes. */
@Composable
internal fun PendingList(items: List<PendingReview>, onReview: (PendingReview) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        items.forEach { item -> PendingRow(item) { onReview(item) } }
    }
}

@Composable
private fun PendingRow(item: PendingReview, onReview: () -> Unit) {
    SurfaceCard(modifier = Modifier.testTag("pending_${item.checkinId}")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier.fillMaxWidth().padding(Spacing.M)
        ) {
            PlaceThumb(item.placeName, item.photoUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(item.placeName, style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.reviews_visited_on, reviewDate(item.day)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.expiresAt.isNotBlank()) {
                    Text(
                        stringResource(R.string.reviews_expires_on, reviewDate(item.expiresAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            SelectChip(
                text = stringResource(R.string.reviews_review_action),
                selected = true,
                onClick = onReview,
                tag = "pending_review_${item.checkinId}"
            )
        }
    }
}

@Composable
private fun ReviewedRow(item: ReviewedVisit) {
    SurfaceCard(modifier = Modifier.testTag("reviewed_${item.checkinId}")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier.fillMaxWidth().padding(Spacing.M)
        ) {
            PlaceThumb(item.placeName, item.photoUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(item.placeName, style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.reviews_visited_on, reviewDate(item.day)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                stringResource(R.string.reviews_reviewed_done),
                style = MaterialTheme.typography.labelLarge,
                color = Mint
            )
        }
    }
}

internal val PREVIEW_PENDING = listOf(
    PendingReview("c1", "p1", "Fonda Doña Meche", "2026-11-20", "2026-12-20"),
    PendingReview("c2", "p2", "Café Raíz", "2026-11-15", "2026-12-15"),
    PendingReview("c3", "p3", "Jardín del Cerro", "2026-11-15", "2026-12-15")
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PendingReviewsPreview() {
    WhereverTheme {
        PendingReviewsContent(
            PendingReviewsUiState(
                loading = false,
                pending = PREVIEW_PENDING,
                reviewed = listOf(ReviewedVisit("c9", "p9", "Mercado de Tula", "2026-11-01", null))
            ),
            onBack = {},
            onReview = {},
            onRetry = {}
        )
    }
}

@Preview
@Composable
internal fun PendingReviewsEmptyPreview() {
    WhereverTheme { PendingReviewsContent(PendingReviewsUiState(loading = false), {}, {}, {}) }
}
