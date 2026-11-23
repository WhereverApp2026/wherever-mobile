package com.wherever.mobile.feature.feed.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.MessageComposer
import com.wherever.mobile.core.designsystem.NavRow
import com.wherever.mobile.core.designsystem.RouteBlueTint
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverShapes
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.feed.domain.Moment
import com.wherever.mobile.feature.feed.domain.MomentAuthor

/** 17_PostDetalle. */
@Composable
fun MomentDetailRoute(
    navigation: MomentNavigation,
    viewModel: MomentDetailViewModel = hiltViewModel(),
    socialViewModel: MomentSocialViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val social by socialViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.item?.moment?.likes) { state.item?.moment?.likes?.let(socialViewModel::seedLikes) }
    val socialActions = MomentSocialActions(
        onLike = socialViewModel::toggleLike,
        onReactions = socialViewModel::openReactions,
        onCloseReactions = socialViewModel::closeReactions,
        onReply = socialViewModel::replyTo,
        onCancelReply = socialViewModel::cancelReply,
        onDelete = socialViewModel::delete,
        onMoreComments = socialViewModel::loadMoreComments,
        onRetryComments = socialViewModel::loadComments,
        onPerson = navigation.onPerson
    )
    MomentDetailContent(
        state,
        navigation,
        onRetry = viewModel::load,
        social = social,
        socialActions = socialActions,
        composer = {
            CommentComposer(social, socialViewModel::onDraft, socialViewModel::send, socialActions.onCancelReply)
        }
    )
    social.sheet?.let { ReactionsBottomSheet(it, socialActions) }
}

/** Compositor de comentarios; si se responde a alguien, lo dice arriba con «Cancelar». */
@Composable
private fun CommentComposer(
    social: MomentSocialUiState,
    onDraft: (String) -> Unit,
    onSend: () -> Unit,
    onCancelReply: () -> Unit
) {
    Column {
        social.replyTo?.let { target ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.comment_replying_to, target.author?.name.orEmpty()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                WhereverTextLink(stringResource(R.string.common_cancel), onCancelReply)
            }
        }
        social.message?.let { Text(it.asString(), style = MaterialTheme.typography.bodySmall) }
        MessageComposer(
            value = social.draft,
            onValueChange = onDraft,
            onSend = onSend,
            placeholder = stringResource(R.string.comment_hint),
            sendDescription = stringResource(R.string.comment_send),
            canSend = social.canSend,
            modifier = Modifier.testTag("comment_composer")
        )
    }
}

@Composable
internal fun MomentDetailContent(
    state: MomentDetailUiState,
    navigation: MomentNavigation,
    onRetry: () -> Unit,
    social: MomentSocialUiState? = null,
    socialActions: MomentSocialActions = MomentSocialActions(),
    composer: @Composable () -> Unit = {}
) {
    val item = state.item
    when {
        state.loading && item == null -> LoadingState()
        item == null -> FormScreen {
            TopTitleRow(stringResource(R.string.feed_post_title), navigation.onBack)
            ErrorState(
                message = (state.error ?: UiText.Res(R.string.error_unknown)).asString(),
                onRetry = onRetry,
                modifier = Modifier.testTag("moment_error")
            )
        }
        else -> FormScreen(footer = if (social != null) ({ composer() }) else null) {
            TopTitleRow(stringResource(R.string.feed_post_title), navigation.onBack)
            AuthorLine(item, onPhoto = false)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(PHOTO_RATIO)
                    .clip(WhereverShapes.Card)
                    .testTag("moment_photo")
            ) {
                if (item.moment.imageUrl != null) {
                    AsyncImage(
                        model = item.moment.imageUrl,
                        contentDescription = item.moment.text,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(PHOTO_RATIO).background(RouteBlueTint)
                    )
                } else {
                    Box(Modifier.fillMaxWidth().aspectRatio(PHOTO_RATIO).background(RouteBlueTint))
                }
            }
            social?.let { LikeBar(it, item.moment.comments, socialActions) }
            item.moment.text?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
            if (item.moment.placeId != null || item.moment.planId != null) {
                SurfaceCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.None)) {
                        item.moment.placeId?.let { placeId ->
                            NavRow(
                                item.placeName ?: stringResource(R.string.feed_see_place),
                                { navigation.onPlace(placeId) },
                                tag = "moment_detail_place"
                            )
                        }
                        item.moment.planId?.let { planId ->
                            NavRow(stringResource(R.string.feed_see_plan), {
                                navigation.onPlan(planId)
                            }, tag = "moment_detail_plan")
                        }
                    }
                }
            }
            social?.let { CommentsSection(it, socialActions) }
        }
    }
}

/** La postal es 4:3 (la sube la app en 1080 px de ancho). */
private const val PHOTO_RATIO = 4f / 3f

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MomentDetailPreview() {
    WhereverTheme {
        MomentDetailContent(
            MomentDetailUiState(loading = false, item = PREVIEW_FEED_ITEM),
            MomentNavigation({}, {}, {}),
            onRetry = {}
        )
    }
}

/** Datos de ejemplo de las vistas previas y capturas (nunca se muestran en la app). */
internal val PREVIEW_FEED_ITEM = FeedItem(
    moment = Moment(
        id = "m1",
        author = MomentAuthor("u1", "mariana", null),
        imageUrl = null,
        text = "Los tacos de guisado, buenísimos. Pidan la salsa verde.",
        placeId = "p1",
        planId = "pl1",
        createdAt = "2026-11-20T12:00:00Z",
        own = false
    ),
    authorName = "Mariana",
    authorPhoto = null,
    placeName = "Fonda Doña Meche",
    time = UiText.Res(R.string.feed_time_hours, listOf(2L))
)
