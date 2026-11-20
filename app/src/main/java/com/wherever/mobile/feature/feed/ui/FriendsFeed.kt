package com.wherever.mobile.feature.feed.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.RouteBlueTint
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverShapes
import com.wherever.mobile.core.designsystem.WhereverTextButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.friends.domain.FriendSuggestion
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.profile.ui.ProfileAvatar

/** Alfa del degradado sobre la foto para que el texto blanco se lea (contraste ≥ 4.5:1). */
private const val SCRIM_ALPHA = 0.72f

/** Después de cuántos momentos aparecen "Personas que quizá conozcas" (12_InicioMas). */
private const val SUGGESTIONS_AFTER = 2

/** 11_InicioAmigos y 12_InicioMas: feed de momentos de amigos y sugerencias de personas. */
fun LazyListScope.friendsFeedItems(state: FriendsFeedUiState, actions: FriendsFeedActions) {
    state.error?.let { error ->
        item(key = "feed_error") {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.S),
                modifier = Modifier.padding(horizontal = ScreenPadding)
            ) {
                ErrorBanner(error.asString())
                WhereverTextButton(onClick = actions.onRetry, modifier = Modifier.testTag("feed_retry")) {
                    Text(stringResource(R.string.common_retry))
                }
            }
        }
    }
    when {
        state.loading -> item(key = "feed_loading") { ListSkeleton(Modifier.padding(horizontal = ScreenPadding)) }
        state.empty -> {
            item(key = "feed_empty") { FeedEmpty(actions.onAddFriends) }
            suggestionsItem(state, actions)
        }
        else -> {
            state.items.forEachIndexed { index, item ->
                item(key = "moment_${item.moment.id}") {
                    MomentCard(item, actions, Modifier.padding(horizontal = ScreenPadding))
                }
                if (index == minOf(SUGGESTIONS_AFTER, state.items.lastIndex)) suggestionsItem(state, actions)
            }
            if (state.nextCursor != null) {
                item(key = "feed_more") {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        WhereverTextLink(
                            stringResource(R.string.feed_more),
                            onClick = actions.onLoadMore,
                            modifier = Modifier.testTag("feed_more")
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.suggestionsItem(state: FriendsFeedUiState, actions: FriendsFeedActions) {
    if (state.suggestions.isEmpty()) return
    item(key = "feed_suggestions") { Suggestions(state, actions) }
}

@Composable
private fun FeedEmpty(onAddFriends: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding).testTag("feed_empty")
    ) {
        Text(
            stringResource(R.string.feed_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.feed_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        WhereverOutlinedButton(
            text = stringResource(R.string.feed_add_friends),
            onClick = onAddFriends,
            tag = "feed_add_friends"
        )
    }
}

/** Tarjeta de un momento: foto con autor y lugar encima, texto y "Planear aquí" (11_InicioAmigos). */
@Composable
internal fun MomentCard(item: FeedItem, actions: FriendsFeedActions, modifier: Modifier = Modifier) {
    val moment = item.moment
    SurfaceCard(modifier = modifier.testTag("moment_${moment.id}")) {
        Column(modifier = Modifier.clickable { actions.onMoment(moment.id) }) {
            Box(modifier = Modifier.fillMaxWidth().height(Sizes.HeroImage).clip(WhereverShapes.Card)) {
                if (moment.imageUrl != null) {
                    AsyncImage(
                        model = moment.imageUrl,
                        contentDescription = moment.text,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(Sizes.HeroImage)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(Sizes.HeroImage).background(RouteBlueTint))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Sizes.HeroImage)
                        .background(
                            Brush.verticalGradient(
                                listOf(Ink.copy(alpha = SCRIM_ALPHA), Color.Transparent, Ink.copy(alpha = SCRIM_ALPHA))
                            )
                        )
                )
                AuthorLine(item, Modifier.align(Alignment.TopStart).padding(Spacing.M))
                item.placeName?.let { name ->
                    Text(
                        name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.BottomStart).padding(Spacing.L)
                    )
                }
            }
            moment.text?.let { text ->
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.M)
                )
            }
        }
        if (moment.placeId != null || moment.planId != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.L, Alignment.End),
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.S)
            ) {
                moment.planId?.let { planId ->
                    WhereverTextLink(
                        stringResource(R.string.feed_see_plan),
                        onClick = { actions.onPlan(planId) },
                        modifier = Modifier.testTag("moment_plan_${moment.id}")
                    )
                }
                moment.placeId?.let { placeId ->
                    WhereverTextLink(
                        stringResource(R.string.feed_plan_here),
                        onClick = { actions.onPlace(placeId) },
                        modifier = Modifier.testTag("moment_place_${moment.id}")
                    )
                }
            }
        }
    }
}

/** "Mariana estuvo aquí · Hace 2 h" sobre la foto. */
@Composable
internal fun AuthorLine(item: FeedItem, modifier: Modifier = Modifier, onPhoto: Boolean = true) {
    val name = item.authorName ?: item.moment.author.username?.let { stringResource(R.string.friends_username_at, it) }
        ?: stringResource(R.string.feed_a_friend)
    val color = if (onPhoto) Color.White else MaterialTheme.colorScheme.onSurface
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.S),
        modifier = modifier
    ) {
        ProfileAvatar(name, item.authorPhoto, size = Sizes.IconXXL)
        Column {
            Text(
                stringResource(if (item.placeName != null) R.string.feed_was_here else R.string.feed_shared, name),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            item.time?.let { time ->
                Text(
                    time.asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (onPhoto) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** "Personas que quizá conozcas" con Agregar → Pendiente (12_InicioMas y su variante). */
@Composable
private fun Suggestions(state: FriendsFeedUiState, actions: FriendsFeedActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.M), modifier = Modifier.testTag("feed_suggestions")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding)
        ) {
            Text(
                stringResource(R.string.feed_suggestions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            WhereverTextLink(stringResource(R.string.feed_see_more), onClick = actions.onAddFriends)
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(Spacing.M)
        ) {
            items(state.suggestions, key = { it.person.id }) { suggestion ->
                SuggestionCard(suggestion, state, actions)
            }
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: FriendSuggestion, state: FriendsFeedUiState, actions: FriendsFeedActions) {
    val person = suggestion.person
    val sent = person.id in state.sent
    SurfaceCard(modifier = Modifier.width(Sizes.EventCardWidth).testTag("suggestion_${person.id}")) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.S),
            modifier = Modifier.fillMaxWidth().clickable { actions.onPerson(person.id) }.padding(Spacing.L)
        ) {
            ProfileAvatar(person.name, person.photoUrl, size = Sizes.AvatarM)
            Text(
                person.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                pluralStringResource(R.plurals.feed_mutual, suggestion.mutualFriends, suggestion.mutualFriends),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (sent) {
                Text(
                    stringResource(R.string.friends_request_pending),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Spacing.M).testTag("suggestion_sent_${person.id}")
                )
            } else {
                WhereverPrimaryButton(
                    text = stringResource(R.string.friends_add),
                    onClick = { actions.onRequest(person) },
                    loading = person.id in state.sending,
                    tag = "suggestion_add_${person.id}"
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun FriendsFeedPreview() {
    WhereverTheme {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Spacing.L),
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            friendsFeedItems(
                FriendsFeedUiState(
                    loading = false,
                    items = listOf(PREVIEW_FEED_ITEM),
                    suggestions = listOf(
                        FriendSuggestion(Person("u2", "Luis Mendoza", "luis", null), 3),
                        FriendSuggestion(Person("u3", "Sofía Ramírez", "sofia", null), 2)
                    ),
                    sent = setOf("u3")
                ),
                FriendsFeedActions({}, {}, {}, {}, {}, {}, {}, {})
            )
        }
    }
}

@Preview
@Composable
internal fun FriendsFeedEmptyPreview() {
    WhereverTheme {
        LazyColumn(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            friendsFeedItems(FriendsFeedUiState(loading = false), FriendsFeedActions({}, {}, {}, {}, {}, {}, {}, {}))
        }
    }
}
