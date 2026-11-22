package com.wherever.mobile.feature.reviews.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.InlineProgress
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.Mist
import com.wherever.mobile.core.designsystem.NavRow
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.SunsetTint
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.feature.reviews.domain.COMMENT_MAX_LENGTH
import com.wherever.mobile.feature.reviews.domain.MAX_PHOTOS
import com.wherever.mobile.feature.reviews.domain.MAX_STARS
import com.wherever.mobile.feature.reviews.domain.MyReview
import com.wherever.mobile.feature.reviews.domain.ReviewPhoto
import com.wherever.mobile.feature.reviews.domain.ReviewState

/** A dónde va 69_Publicado. */
data class ReviewFormNavigation(
    val onClose: () -> Unit,
    val onMyReviews: () -> Unit,
    val onHome: () -> Unit,
    val onRepeat: (placeName: String) -> Unit
)

/** Eventos de 68_PostVisita y 78_ResCom; la pantalla solo los envía. */
internal data class ReviewFormActions(
    val onRating: (Int) -> Unit,
    val onComment: (String) -> Unit,
    val onAddPhoto: () -> Unit,
    val onRemovePhoto: (String) -> Unit,
    val onOpenDetail: () -> Unit,
    val onCloseDetail: () -> Unit,
    val onAspect: (Aspect, Int) -> Unit,
    val onDetailDone: () -> Unit,
    val onPublish: () -> Unit
)

/** 68_PostVisita → 78_ResCom → 69_Publicado (specs/nv-32). */
@Composable
fun ReviewFormRoute(navigation: ReviewFormNavigation, viewModel: ReviewFormViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onPhotoPicked(it.toString()) }
    }
    val photoMessage = state.photoMessage?.asString()
    LaunchedEffect(photoMessage) {
        if (photoMessage != null) {
            snackbar.showSnackbar(photoMessage)
            viewModel.photoMessageShown()
        }
    }
    BackHandler(enabled = state.step == ReviewStep.DETAIL) { viewModel.closeDetail() }
    val actions = ReviewFormActions(
        onRating = viewModel::onRating,
        onComment = viewModel::onComment,
        onAddPhoto = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onRemovePhoto = viewModel::onRemovePhoto,
        onOpenDetail = viewModel::openDetail,
        onCloseDetail = viewModel::closeDetail,
        onAspect = viewModel::onAspect,
        onDetailDone = viewModel::onDetailDone,
        onPublish = viewModel::publish
    )
    Box(modifier = Modifier.fillMaxSize()) {
        val published = state.published
        when {
            state.missing -> FormScreen {
                TopTitleRow(stringResource(R.string.reviews_edit_title), navigation.onClose)
                ErrorBanner(stringResource(R.string.reviews_edit_missing))
            }
            state.step == ReviewStep.PUBLISHED && published != null ->
                ReviewPublishedContent(state, published, navigation)
            state.step == ReviewStep.DETAIL -> ReviewDetailContent(state, actions)
            else -> ReviewFormContent(state, actions, navigation.onClose)
        }
        WhereverSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(ScreenPadding))
    }
}

/** 68_PostVisita */
@Composable
internal fun ReviewFormContent(state: ReviewFormUiState, actions: ReviewFormActions, onClose: () -> Unit) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(if (state.editing) R.string.reviews_save_changes else R.string.reviews_publish),
                onClick = actions.onPublish,
                enabled = state.canPublish,
                loading = state.submitting,
                tag = "review_publish"
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WhereverIconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.reviews_close))
            }
            Text(
                stringResource(if (state.editing) R.string.reviews_edit_title else R.string.reviews_form_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).padding(end = Sizes.TouchTarget)
            )
        }
        PlaceHeader(state)
        StarInput(
            value = state.rating,
            onChange = actions.onRating,
            tag = "review_star",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        LabeledTextField(
            label = stringResource(R.string.reviews_comment_label),
            value = state.comment,
            onValueChange = actions.onComment,
            placeholder = stringResource(R.string.reviews_comment_placeholder),
            singleLine = false,
            error = state.commentError?.asString(),
            supporting = stringResource(R.string.common_char_count, state.comment.length, COMMENT_MAX_LENGTH),
            tag = "review_comment"
        )
        PhotosRow(state, actions)
        SurfaceCard {
            NavRow(
                title = stringResource(R.string.reviews_detail_row),
                onClick = actions.onOpenDetail,
                value = stringResource(
                    if (state.detailComplete) R.string.reviews_detail_done else R.string.reviews_detail_missing
                ),
                tag = "review_detail_row"
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.M),
                modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.RowMinHeight).padding(horizontal = Spacing.L)
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = Mint, modifier = Modifier.size(Sizes.IconM))
                Text(stringResource(R.string.reviews_verified_row), style = MaterialTheme.typography.bodyLarge)
            }
        }
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

/** Foto del lugar (o la primera de la reseña) con su nombre y el día de la visita, como en 68_PostVisita. */
@Composable
private fun PlaceHeader(state: ReviewFormUiState) {
    val photo = state.photos.firstOrNull()?.url ?: state.placePhotoUrl
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Sizes.CardImage)
            .clip(RoundedCornerShape(Radius.XL))
            .background(Ink)
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(Spacing.L)) {
            Text(
                state.placeName,
                style = MaterialTheme.typography.titleMedium,
                color = Mist
            )
            Text(
                if (state.day.isBlank()) {
                    stringResource(R.string.reviews_visit_verified_short)
                } else {
                    stringResource(R.string.reviews_visit_verified, reviewDate(state.day))
                },
                style = MaterialTheme.typography.bodySmall,
                color = Mist
            )
        }
    }
}

@Composable
private fun PhotosRow(state: ReviewFormUiState, actions: ReviewFormActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        Text(
            stringResource(R.string.reviews_photos_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
            state.photos.forEach { photo -> PhotoThumb(photo, onRemove = { actions.onRemovePhoto(photo.id) }) }
            if (state.photos.size < MAX_PHOTOS) {
                AddPhotoTile(enabled = state.canAddPhoto, uploading = state.uploading, onClick = actions.onAddPhoto)
            }
        }
    }
}

@Composable
private fun PhotoThumb(photo: ReviewPhoto, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(Sizes.Thumbnail)) {
        AsyncImage(
            model = photo.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(Radius.M))
        )
        WhereverIconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = stringResource(R.string.reviews_remove_photo),
                tint = Mist,
                modifier = Modifier.background(Ink, CircleShape).padding(Spacing.XS).size(Sizes.IconS)
            )
        }
    }
}

@Composable
private fun AddPhotoTile(enabled: Boolean, uploading: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(Sizes.Thumbnail)
            .clip(RoundedCornerShape(Radius.M))
            .border(Strokes.Hairline, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radius.M))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .testTag("review_add_photo")
    ) {
        if (uploading) {
            InlineProgress()
        } else {
            Icon(
                Icons.Outlined.Add,
                contentDescription = stringResource(R.string.reviews_add_photo),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** 78_ResCom: los cinco aspectos de la API. Sin chips: no hay catálogo publicado (D-32b). */
@Composable
internal fun ReviewDetailContent(state: ReviewFormUiState, actions: ReviewFormActions) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.reviews_detail_done),
                onClick = actions.onDetailDone,
                enabled = state.detailComplete,
                loading = state.submitting,
                tag = "review_detail_done"
            )
        }
    ) {
        TopTitleRow(stringResource(R.string.reviews_detail_title), actions.onCloseDetail)
        Text(
            state.placeName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        InfoBanner(
            stringResource(R.string.reviews_detail_banner, state.rating),
            icon = Icons.Outlined.CheckCircle,
            container = MintTint,
            contentColor = Ink
        )
        SurfaceCard {
            Text(
                stringResource(R.string.reviews_detail_card),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = Spacing.L, end = Spacing.L, top = Spacing.M)
            )
            Aspect.entries.forEach { aspect ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = Spacing.L, end = Spacing.S)
                ) {
                    Text(
                        stringResource(aspect.label()),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    StarInput(
                        value = state.aspects[aspect] ?: 0,
                        onChange = { actions.onAspect(aspect, it) },
                        tag = "review_aspect_${aspect.apiKey}",
                        size = Sizes.IconM
                    )
                }
            }
        }
        if (!state.detailComplete) {
            Text(
                stringResource(R.string.reviews_detail_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

/** 69_Publicado */
@Composable
internal fun ReviewPublishedContent(state: ReviewFormUiState, review: MyReview, navigation: ReviewFormNavigation) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                stringResource(R.string.reviews_see_mine),
                navigation.onMyReviews,
                tag = "review_see_mine"
            )
            WhereverOutlinedButton(
                text = stringResource(R.string.reviews_back_home),
                onClick = navigation.onHome,
                modifier = Modifier.fillMaxWidth(),
                tag = "review_home"
            )
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(Sizes.AvatarM)
                .background(MintTint, CircleShape)
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = Mint,
                modifier = Modifier.size(Sizes.IconXL)
            )
        }
        Text(
            stringResource(if (state.editing) R.string.reviews_saved_title else R.string.reviews_published_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            stringResource(
                if (review.state == ReviewState.IN_MODERATION) {
                    R.string.reviews_moderation_body
                } else {
                    R.string.reviews_published_body
                }
            ),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        SurfaceCard(modifier = Modifier.testTag("review_published_card")) {
            Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                Text(state.placeName, style = MaterialTheme.typography.titleSmall)
                StarsRow(review.rating)
                if (review.comment.isNotBlank()) Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                review.photos.firstOrNull()?.let { photo ->
                    AsyncImage(
                        model = photo.url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(Sizes.CardImage).clip(RoundedCornerShape(Radius.M))
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.L))
                .background(SunsetTint)
                .clickable(role = Role.Button) { navigation.onRepeat(state.placeName) }
                .padding(Spacing.L)
                .testTag("review_repeat")
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.reviews_repeat_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink
                )
                Text(
                    stringResource(R.string.reviews_repeat_body, state.placeName),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink
                )
            }
        }
    }
}

internal val PREVIEW_FORM_ACTIONS = ReviewFormActions({}, {}, {}, {}, {}, {}, { _, _ -> }, {}, {})

private const val PREVIEW_RATING = 4

private val PREVIEW_FORM = ReviewFormUiState(
    placeName = "Fonda Doña Meche",
    day = "2026-11-20",
    rating = 4,
    comment = "Los tacos de guisado, buenísimos. Pidan la salsa verde."
)

private val PREVIEW_REVIEW = MyReview(
    id = "r1",
    placeId = "p1",
    checkinId = "c1",
    rating = PREVIEW_RATING,
    aspects = Aspect.entries.associateWith { PREVIEW_RATING },
    chips = emptyList(),
    comment = PREVIEW_FORM.comment,
    photos = emptyList(),
    state = ReviewState.PUBLISHED,
    useful = 0,
    businessReply = null,
    editable = true,
    version = 1,
    createdAt = "2026-11-20T17:02:00Z",
    placeName = PREVIEW_FORM.placeName
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ReviewFormPreview() {
    WhereverTheme { ReviewFormContent(PREVIEW_FORM, PREVIEW_FORM_ACTIONS, onClose = {}) }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ReviewDetailPreview() {
    WhereverTheme {
        ReviewDetailContent(
            PREVIEW_FORM.copy(
                aspects = mapOf(
                    Aspect.TASTE to MAX_STARS,
                    Aspect.SERVICE to PREVIEW_RATING,
                    Aspect.AMBIENCE to PREVIEW_RATING
                )
            ),
            PREVIEW_FORM_ACTIONS
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ReviewPublishedPreview() {
    WhereverTheme {
        ReviewPublishedContent(
            PREVIEW_FORM.copy(step = ReviewStep.PUBLISHED, published = PREVIEW_REVIEW),
            PREVIEW_REVIEW,
            ReviewFormNavigation({}, {}, {}, {})
        )
    }
}
