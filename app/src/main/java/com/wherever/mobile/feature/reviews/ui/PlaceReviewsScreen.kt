package com.wherever.mobile.feature.reviews.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InitialAvatar
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverBottomSheet
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.reviews.domain.MAX_STARS
import com.wherever.mobile.feature.reviews.domain.PlaceBrief
import com.wherever.mobile.feature.reviews.domain.PlaceReview
import com.wherever.mobile.feature.reviews.domain.PlaceReviewQuery
import com.wherever.mobile.feature.reviews.domain.ReportReason
import java.util.Locale
import kotlin.math.roundToInt

/** 80_ResLugar (specs/nv-32). Se abre con «Ver reseñas» de la ficha del lugar. */
@Composable
fun PlaceReviewsRoute(onBack: () -> Unit, viewModel: PlaceReviewsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            viewModel.messageShown()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        PlaceReviewsContent(
            state,
            PlaceReviewsActions(
                onBack = onBack,
                onQuery = viewModel::onQuery,
                onUseful = viewModel::toggleUseful,
                onReport = viewModel::openReport,
                onLoadMore = viewModel::loadMore,
                onRetry = viewModel::load
            )
        )
        WhereverSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(ScreenPadding))
    }
    state.report?.let { form ->
        WhereverBottomSheet(onDismissRequest = viewModel::closeReport, tag = "review_report_sheet") {
            ReportReasons(form, viewModel::onReportReason, viewModel::sendReport)
        }
    }
}

@Composable
internal fun PlaceReviewsContent(state: PlaceReviewsUiState, actions: PlaceReviewsActions) {
    val error = state.error
    FormScreen {
        Column {
            TopTitleRow(state.placeName, actions.onBack)
            Text(
                stringResource(R.string.reviews_place_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        state.place?.let { Summary(it) }
        Filters(state.query, actions.onQuery)
        when {
            state.loading -> ListSkeleton(rows = 3)
            error != null -> ErrorState(error.asString(), actions.onRetry)
            state.empty -> Text(
                stringResource(R.string.reviews_place_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("place_reviews_empty")
            )
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    state.reviews.forEach { review ->
                        PlaceReviewCard(review, state.placeName, review.id in state.usefulMarked, actions)
                    }
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

/**
 * Promedio, estrellas y total. La distribución por estrellas de la maqueta no se dibuja: `GET /lugares/{id}`
 * no la publica (D-32c).
 */
@Composable
private fun Summary(place: PlaceBrief) {
    SurfaceCard(modifier = Modifier.testTag("place_reviews_summary")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.L),
            modifier = Modifier.fillMaxWidth().padding(Spacing.L)
        ) {
            Text(
                String.format(Locale.forLanguageTag("es-MX"), "%.1f", place.average),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Column {
                StarsRow(place.average.roundToInt().coerceIn(0, MAX_STARS))
                Text(
                    stringResource(R.string.reviews_place_count, place.total),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Filters(query: PlaceReviewQuery, onQuery: (PlaceReviewQuery) -> Unit) {
    ChipGroup {
        SelectChip(
            stringResource(R.string.reviews_filter_recent),
            query.recent,
            { onQuery(query.copy(recent = !query.recent)) },
            tag = "place_reviews_recent"
        )
        SelectChip(
            stringResource(R.string.reviews_filter_photos),
            query.withPhotos,
            { onQuery(query.copy(withPhotos = !query.withPhotos)) },
            tag = "place_reviews_photos"
        )
        SelectChip(
            stringResource(R.string.reviews_filter_five),
            query.stars == MAX_STARS,
            { onQuery(query.copy(stars = if (query.stars == MAX_STARS) null else MAX_STARS)) },
            tag = "place_reviews_five"
        )
    }
}

@Composable
private fun PlaceReviewCard(
    review: PlaceReview,
    placeName: String,
    markedUseful: Boolean,
    actions: PlaceReviewsActions
) {
    val author = review.author.ifBlank { stringResource(R.string.reviews_anonymous) }
    SurfaceCard(modifier = Modifier.testTag("place_review_${review.id}")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.M)
            ) {
                InitialAvatar(author, Sizes.AvatarS)
                Column(modifier = Modifier.weight(1f)) {
                    Text(author, style = MaterialTheme.typography.titleSmall)
                    Text(
                        reviewDate(review.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StarsRow(review.rating)
            }
            StatusPill(stringResource(R.string.reviews_visit_verified_short), container = MintTint, contentColor = Ink)
            if (review.comment.isNotBlank()) Text(review.comment, style = MaterialTheme.typography.bodyMedium)
            review.businessReply?.let {
                BusinessReply(stringResource(R.string.reviews_business_reply_named, placeName), it)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                WhereverIconButton(
                    onClick = { actions.onUseful(review) },
                    modifier = Modifier.testTag("place_review_useful_${review.id}")
                ) {
                    Icon(
                        if (markedUseful) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(
                            if (markedUseful) R.string.reviews_useful_a11y_on else R.string.reviews_useful_a11y_off
                        ),
                        tint = if (markedUseful) Sunset else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(Sizes.IconM)
                    )
                }
                Text(
                    stringResource(R.string.reviews_useful, review.useful),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).testTag("place_review_useful_count_${review.id}")
                )
                WhereverTextLink(
                    stringResource(R.string.reviews_report),
                    { actions.onReport(review) },
                    modifier = Modifier.testTag("place_review_report_${review.id}"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val REASONS = listOf(
    ReportReason.OFFENSIVE to R.string.reviews_reason_offensive,
    ReportReason.FAKE to R.string.reviews_reason_fake,
    ReportReason.SPAM to R.string.reviews_reason_spam,
    ReportReason.PERSONAL_DATA to R.string.reviews_reason_personal,
    ReportReason.OTHER to R.string.reviews_reason_other
)

/** Motivos de «Reportar»: uno obligatorio. */
@Composable
internal fun ReportReasons(form: ReviewReportForm, onReason: (ReportReason) -> Unit, onSend: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.S),
        modifier = Modifier.testTag("review_report_reasons")
    ) {
        Text(
            stringResource(R.string.reviews_report_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        REASONS.forEach { (reason, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Sizes.TouchTarget)
                    .selectable(selected = form.reason == reason, role = Role.RadioButton) { onReason(reason) }
                    .testTag("review_reason_${reason.apiKey}")
            ) {
                RadioButton(selected = form.reason == reason, onClick = null)
                Text(stringResource(label), modifier = Modifier.padding(start = Spacing.M))
            }
        }
        form.error?.let { ErrorBanner(it.asString()) }
        WhereverPrimaryButton(
            text = stringResource(R.string.reviews_report_send),
            onClick = onSend,
            enabled = form.canSend,
            loading = form.sending,
            tag = "review_report_send"
        )
    }
}

private const val PREVIEW_USEFUL = 12
private const val PREVIEW_AVERAGE = 4.8
private const val PREVIEW_TOTAL = 126

internal val PREVIEW_PLACE_REVIEWS = listOf(
    PlaceReview(
        id = "r1",
        author = "mariana_g",
        authorPhotoUrl = null,
        rating = MAX_STARS,
        comment = "Café de olla buenísimo y el trato fue muy amable.",
        photos = emptyList(),
        useful = PREVIEW_USEFUL,
        businessReply = "¡Gracias por venir! Nos alegra que te gustara.",
        createdAt = "2026-11-19T18:00:00Z"
    ),
    PlaceReview(
        id = "r2",
        author = "jorge_r",
        authorPhotoUrl = null,
        rating = MAX_STARS - 1,
        comment = "Buen lugar para ir temprano, la terraza es lo mejor.",
        photos = emptyList(),
        useful = PREVIEW_USEFUL,
        businessReply = null,
        createdAt = "2026-11-14T18:00:00Z"
    )
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PlaceReviewsPreview() {
    WhereverTheme {
        PlaceReviewsContent(
            PlaceReviewsUiState(
                placeName = "Café Raíz",
                place = PlaceBrief("p1", "Café Raíz", null, average = PREVIEW_AVERAGE, total = PREVIEW_TOTAL),
                query = PlaceReviewQuery(recent = true),
                loading = false,
                reviews = PREVIEW_PLACE_REVIEWS,
                usefulMarked = setOf("r1")
            ),
            PlaceReviewsActions({}, {}, {}, {}, {}, {})
        )
    }
}
