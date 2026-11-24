package com.wherever.mobile.feature.feed.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Danger
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.InlineProgress
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.UnderlineTabs
import com.wherever.mobile.core.designsystem.WhereverBottomSheet
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.feed.domain.Comment
import com.wherever.mobile.feature.feed.domain.CommentAuthor
import com.wherever.mobile.feature.feed.domain.CommentState
import com.wherever.mobile.feature.feed.domain.ReactionPerson
import com.wherever.mobile.feature.feed.domain.ReactionTab
import com.wherever.mobile.feature.profile.ui.ProfileAvatar

/** Corazón con su conteo (abre 13_Reacciones) y conteo de comentarios. */
@Composable
internal fun LikeBar(state: MomentSocialUiState, comments: Int, actions: MomentSocialActions) {
    val likes = state.likes ?: 0
    val commentCount = (comments + state.commentsDelta).coerceAtLeast(0)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        WhereverIconButton(onClick = actions.onLike, modifier = Modifier.testTag("moment_like")) {
            Icon(
                if (state.liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(if (state.liked) R.string.moment_unlike else R.string.moment_like),
                tint = if (state.liked) Danger else MaterialTheme.colorScheme.onSurface
            )
        }
        WhereverTextLink(
            pluralStringResource(R.plurals.moment_likes, likes, likes),
            onClick = { actions.onReactions(ReactionTab.ALL) },
            modifier = Modifier.testTag("moment_likes_count")
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
            modifier = Modifier.weight(1f).padding(start = Spacing.M)
        ) {
            Icon(
                painterResource(R.drawable.ic_chat),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.S)
            )
            Text(
                pluralStringResource(R.plurals.moment_comments, commentCount, commentCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun CommentsSection(state: MomentSocialUiState, actions: MomentSocialActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.M), modifier = Modifier.testTag("moment_comments")) {
        Text(stringResource(R.string.comments_title), style = MaterialTheme.typography.titleMedium)
        state.error?.let { ErrorBanner(it.asString()) }
        when {
            state.commentsLoading -> InlineProgress(Modifier.padding(Spacing.S))
            state.commentsError != null -> {
                ErrorBanner(state.commentsError.asString())
                WhereverTextLink(stringResource(R.string.common_retry), actions.onRetryComments)
            }
            state.comments.isEmpty() -> Text(
                stringResource(R.string.comments_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("comments_empty")
            )
            else -> state.comments.forEach { comment ->
                CommentItem(comment, actions, reply = false)
                comment.replies.forEach { CommentItem(it, actions, reply = true) }
            }
        }
        if (state.commentsCursor != null) {
            WhereverTextLink(stringResource(R.string.comments_more), actions.onMoreComments)
        }
    }
}

@Composable
private fun CommentItem(comment: Comment, actions: MomentSocialActions, reply: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (reply) Spacing.XXL else Spacing.None)
            .testTag("comment_${comment.id}")
    ) {
        val author = comment.author
        ProfileAvatar(author?.name ?: "?", author?.photoUrl, Sizes.IconXL)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
            if (comment.state == CommentState.DELETED || author == null) {
                Text(
                    stringResource(R.string.comment_removed),
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }
            Text(author.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
            if (comment.state == CommentState.IN_MODERATION) {
                StatusPill(stringResource(R.string.comment_moderation_label))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                if (!reply && comment.state == CommentState.PUBLISHED) {
                    WhereverTextLink(stringResource(R.string.comment_reply), { actions.onReply(comment) })
                }
                if (comment.canDelete) {
                    WhereverTextLink(
                        stringResource(R.string.comment_delete),
                        { actions.onDelete(comment) },
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/** 13_Reacciones: pestañas Todos, Me gusta y Van con las personas. */
@Composable
internal fun ReactionsBottomSheet(sheet: ReactionsSheet, actions: MomentSocialActions) {
    WhereverBottomSheet(onDismissRequest = actions.onCloseReactions, tag = "reactions_sheet") {
        ReactionsContent(sheet, actions)
    }
}

@Composable
internal fun ReactionsContent(sheet: ReactionsSheet, actions: MomentSocialActions) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.L, vertical = Spacing.M)
    ) {
        Text(stringResource(R.string.reactions_title), style = MaterialTheme.typography.titleLarge)
        val totals = sheet.data
        UnderlineTabs(
            options = listOf(
                stringResource(R.string.reactions_all, totals?.all ?: 0),
                stringResource(R.string.reactions_likes, totals?.likes ?: 0)
            ),
            selectedIndex = if (sheet.tab == ReactionTab.LIKES) 1 else 0,
            onSelect = { actions.onReactions(if (it == 1) ReactionTab.LIKES else ReactionTab.ALL) },
            tag = "reactions_tabs"
        )
        when {
            sheet.loading -> InlineProgress(Modifier.padding(Spacing.L))
            sheet.error != null -> ErrorBanner(sheet.error.asString())
            totals == null || totals.people.isEmpty() -> Text(
                stringResource(R.string.reactions_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> totals.people.forEach { ReactionRow(it, actions) }
        }
    }
}

@Composable
private fun ReactionRow(person: ReactionPerson, actions: MomentSocialActions) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = Modifier.fillMaxWidth().testTag("reaction_${person.id}")
    ) {
        ProfileAvatar(person.name, person.photoUrl, Sizes.AvatarS)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (person.isMe) stringResource(R.string.reactions_you) else person.name,
                style = MaterialTheme.typography.titleSmall
            )
            if (person.mutualFriends > 0) {
                Text(
                    pluralStringResource(R.plurals.friends_mutual, person.mutualFriends, person.mutualFriends),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!person.isMe) {
            WhereverTextLink(stringResource(R.string.reactions_see_profile), { actions.onPerson(person.id) })
        }
    }
}

private val PREVIEW_COMMENTS = listOf(
    Comment(
        "c1",
        CommentAuthor("u1", "Mariana", "mariana", null),
        null,
        "¡Se ve buenísimo! ¿Cuánto costó?",
        CommentState.PUBLISHED,
        own = false,
        canDelete = false,
        createdAt = "2026-11-23T10:00:00Z",
        replies = listOf(
            Comment(
                "c2",
                CommentAuthor("u2", "Luis", "luis", null),
                "c1",
                "Como 150 por persona",
                CommentState.IN_MODERATION,
                own = true,
                canDelete = true,
                createdAt = "2026-11-23T10:05:00Z"
            )
        )
    )
)

private const val PREVIEW_LIKES = 12
private const val PREVIEW_COMMENTS_TOTAL = 2

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MomentSocialPreview() {
    WhereverTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(Spacing.L)
        ) {
            val state =
                MomentSocialUiState(
                    liked = true,
                    likes = PREVIEW_LIKES,
                    comments = PREVIEW_COMMENTS,
                    commentsLoading = false
                )
            LikeBar(state, PREVIEW_COMMENTS_TOTAL, MomentSocialActions())
            CommentsSection(state, MomentSocialActions())
        }
    }
}
