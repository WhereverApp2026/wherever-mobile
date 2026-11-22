package com.wherever.mobile.feature.reviews.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ConfirmDialog
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.GoldTint
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.feature.reviews.domain.MAX_STARS
import com.wherever.mobile.feature.reviews.domain.MyReview
import com.wherever.mobile.feature.reviews.domain.PendingReview
import com.wherever.mobile.feature.reviews.domain.ReviewState

/** 79_MisRes, con «confirmar eliminar» y «reseña eliminada» (specs/nv-32). */
@Composable
fun MyReviewsRoute(
    onBack: () -> Unit,
    onEdit: (MyReview) -> Unit,
    onReview: (PendingReview) -> Unit,
    viewModel: MyReviewsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val deleted = stringResource(R.string.reviews_deleted)
    val undo = stringResource(R.string.reviews_undo)
    val undoable = state.undoable
    LaunchedEffect(undoable?.id) {
        if (undoable == null) {
            snackbar.currentSnackbarData?.dismiss()
        } else {
            // El ViewModel decide cuándo vence el plazo; el aviso dura hasta entonces.
            val result = snackbar.showSnackbar(deleted, actionLabel = undo, duration = SnackbarDuration.Indefinite)
            if (result == SnackbarResult.ActionPerformed) viewModel.undo()
        }
    }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            viewModel.messageShown()
        }
    }
    Scaffold(snackbarHost = { WhereverSnackbarHost(snackbar) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MyReviewsContent(
                state,
                MyReviewsActions(
                    onBack = onBack,
                    onTab = viewModel::onTab,
                    onEdit = onEdit,
                    onDelete = viewModel::askDelete,
                    onReview = onReview,
                    onLoadMore = viewModel::loadMore,
                    onRetry = viewModel::load
                )
            )
        }
    }
    state.confirmDelete?.let { review ->
        ConfirmDialog(
            title = stringResource(R.string.reviews_delete_title),
            message = stringResource(R.string.reviews_delete_message, review.placeName),
            confirmText = stringResource(R.string.reviews_delete),
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
internal fun MyReviewsContent(state: MyReviewsUiState, actions: MyReviewsActions) {
    val error = state.error
    FormScreen {
        TopTitleRow(stringResource(R.string.reviews_mine_title), actions.onBack)
        SegmentedControl(
            options = listOf(
                stringResource(R.string.reviews_tab_published, state.reviews.size),
                stringResource(R.string.reviews_tab_pending, state.pending.size)
            ),
            selectedIndex = state.tab.ordinal,
            onSelect = { actions.onTab(MyReviewsTab.entries[it]) }
        )
        when {
            state.loading -> ListSkeleton(rows = 3)
            error != null -> ErrorState(error.asString(), actions.onRetry)
            state.tab == MyReviewsTab.PENDING -> if (state.pending.isEmpty()) {
                EmptyText(stringResource(R.string.reviews_pending_empty))
            } else {
                PendingList(state.pending, actions.onReview)
            }
            state.reviews.isEmpty() -> EmptyText(stringResource(R.string.reviews_mine_empty))
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    state.reviews.forEach { review -> MyReviewCard(review, actions) }
                }
                if (state.cursor != null) {
                    WhereverTextLink(
                        stringResource(R.string.reviews_load_more),
                        actions.onLoadMore,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.testTag("my_reviews_empty")
    )
}

@Composable
private fun MyReviewCard(review: MyReview, actions: MyReviewsActions) {
    SurfaceCard(modifier = Modifier.testTag("my_review_${review.id}")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.M)
            ) {
                PlaceThumb(review.placeName, review.placePhotoUrl)
                Column(modifier = Modifier.weight(1f)) {
                    Text(review.placeName, style = MaterialTheme.typography.titleSmall)
                    StarsRow(review.rating)
                }
                StateLabel(review.state)
            }
            if (review.comment.isNotBlank()) Text(review.comment, style = MaterialTheme.typography.bodyMedium)
            review.businessReply?.let { BusinessReply(stringResource(R.string.reviews_business_reply), it) }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.L), modifier = Modifier.fillMaxWidth()) {
                if (review.editable) {
                    WhereverTextLink(stringResource(R.string.reviews_edit), { actions.onEdit(review) })
                }
                WhereverTextLink(
                    stringResource(R.string.reviews_delete),
                    { actions.onDelete(review) },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StateLabel(state: ReviewState) {
    when (state) {
        ReviewState.PUBLISHED ->
            StatusPill(stringResource(R.string.reviews_state_published), container = MintTint, contentColor = Ink)
        ReviewState.IN_MODERATION ->
            StatusPill(stringResource(R.string.reviews_state_moderation), container = GoldTint, contentColor = Ink)
        ReviewState.HIDDEN -> StatusPill(stringResource(R.string.reviews_state_hidden))
    }
}

private fun previewReview(id: String, place: String, rating: Int, comment: String, reply: String? = null) = MyReview(
    id = id,
    placeId = "p$id",
    checkinId = "c$id",
    rating = rating,
    aspects = Aspect.entries.associateWith { rating },
    chips = emptyList(),
    comment = comment,
    photos = emptyList(),
    state = ReviewState.PUBLISHED,
    useful = 0,
    businessReply = reply,
    editable = true,
    version = 1,
    createdAt = "2026-11-20T17:02:00Z",
    placeName = place
)

private const val PREVIEW_GOOD = 4
private const val PREVIEW_OK = 3

private val PREVIEW_ACTIONS = MyReviewsActions({}, {}, {}, {}, {}, {}, {})

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MyReviewsPreview() {
    WhereverTheme {
        MyReviewsContent(
            MyReviewsUiState(
                loading = false,
                reviews = listOf(
                    previewReview(
                        id = "1",
                        place = "Café Raíz",
                        rating = PREVIEW_GOOD,
                        comment = "El café de olla estuvo buenísimo y nos atendieron muy rápido.",
                        reply = "¡Gracias por venir! Te esperamos pronto."
                    ),
                    previewReview(
                        id = "2",
                        place = "Mercado de Tula",
                        rating = MAX_STARS,
                        comment = "Muy buen ambiente para ir en familia."
                    ),
                    previewReview(
                        id = "3",
                        place = "Fonda Doña Lupe",
                        rating = PREVIEW_OK,
                        comment = "La comida rica, pero tardaron en darnos mesa."
                    )
                ),
                pending = PREVIEW_PENDING
            ),
            PREVIEW_ACTIONS
        )
    }
}

@Preview
@Composable
internal fun MyReviewsConfirmPreview() {
    WhereverTheme {
        ConfirmDialog(
            title = stringResource(R.string.reviews_delete_title),
            message = stringResource(R.string.reviews_delete_message, "Café Raíz"),
            confirmText = stringResource(R.string.reviews_delete),
            onConfirm = {},
            onDismiss = {}
        )
    }
}
