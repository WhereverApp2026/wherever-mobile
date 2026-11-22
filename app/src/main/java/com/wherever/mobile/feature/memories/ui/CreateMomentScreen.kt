package com.wherever.mobile.feature.memories.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.InitialAvatar
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.NavRow
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverBottomSheet
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.memories.domain.MOMENT_TEXT_MAX
import com.wherever.mobile.feature.memories.domain.Postcard
import com.wherever.mobile.feature.memories.domain.PostcardState
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import com.wherever.mobile.feature.memories.domain.Visibility

/** A dónde va 70_CrearPub. */
data class CreateMomentNavigation(
    val onCancel: () -> Unit,
    val onPublished: () -> Unit,
    val onCamera: () -> Unit,
    val onReviews: () -> Unit
)

/** Eventos de 70_CrearPub. */
internal data class CreateMomentActions(
    val onTab: (MomentTab) -> Unit,
    val onText: (String) -> Unit,
    val onChoosePostcard: () -> Unit,
    val onChooseFriends: () -> Unit,
    val onVisibility: (Visibility) -> Unit,
    val onPublish: () -> Unit,
    val onRetry: () -> Unit
)

/** 70_CrearPub (specs/nv-31). Se abre desde Perfil hasta que exista la hoja Crear (nv-17). */
@Composable
fun CreateMomentRoute(navigation: CreateMomentNavigation, viewModel: CreateMomentViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) { viewModel.published.collect { navigation.onPublished() } }
    Box(modifier = Modifier.fillMaxSize()) {
        CreateMomentContent(
            state,
            CreateMomentActions(
                onTab = viewModel::onTab,
                onText = viewModel::onText,
                onChoosePostcard = viewModel::openPostcards,
                onChooseFriends = viewModel::openFriends,
                onVisibility = viewModel::onVisibility,
                onPublish = viewModel::publish,
                onRetry = viewModel::load
            ),
            navigation
        )
        WhereverSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(ScreenPadding))
    }
    if (state.choosingPostcard) {
        WhereverBottomSheet(onDismissRequest = viewModel::closePostcards, tag = "moment_postcards") {
            Text(stringResource(R.string.memories_choose_postcard), style = MaterialTheme.typography.titleLarge)
            state.postcards.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
                    row.forEach { card ->
                        AlbumThumb(
                            card.imageUrl,
                            card.text.ifBlank { stringResource(R.string.memories_postcard_title) },
                            memoryDate(card.createdAt),
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(Radius.M))
                                .clickable(role = Role.Button) { viewModel.onPostcard(card) }
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
    if (state.choosingFriends) {
        WhereverBottomSheet(onDismissRequest = viewModel::closeFriends, tag = "moment_friends") {
            FriendsPicker(state.friends, state.tagged, viewModel::onToggleFriend)
            state.error?.let { ErrorBanner(it.asString()) }
        }
    }
}

@Composable
internal fun FriendsPicker(friends: List<Person>, tagged: Set<String>, onToggle: (String) -> Unit) {
    Text(stringResource(R.string.memories_tag_row), style = MaterialTheme.typography.titleLarge)
    friends.forEach { person ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Sizes.TouchTarget)
                .toggleable(value = person.id in tagged, role = Role.Checkbox) { onToggle(person.id) }
                .testTag("moment_friend_${person.id}")
        ) {
            InitialAvatar(person.name, Sizes.AvatarS)
            Text(person.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Checkbox(checked = person.id in tagged, onCheckedChange = null)
        }
    }
}

@Composable
internal fun CreateMomentContent(
    state: CreateMomentUiState,
    actions: CreateMomentActions,
    navigation: CreateMomentNavigation
) {
    FormScreen(
        footer = {
            if (state.tab == MomentTab.PHOTO && state.selected != null) {
                WhereverPrimaryButton(
                    text = stringResource(R.string.memories_publish),
                    onClick = actions.onPublish,
                    enabled = state.canPublish,
                    loading = state.publishing,
                    icon = Icons.AutoMirrored.Outlined.Send,
                    tag = "moment_publish"
                )
            }
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WhereverTextLink(
                stringResource(R.string.common_cancel),
                navigation.onCancel,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.memories_moment_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = Spacing.L)
            )
        }
        SegmentedControl(
            options = listOf(
                stringResource(R.string.memories_tab_photo),
                stringResource(R.string.memories_tab_plan),
                stringResource(R.string.memories_tab_review)
            ),
            selectedIndex = state.tab.ordinal,
            onSelect = { actions.onTab(MomentTab.entries[it]) }
        )
        when (state.tab) {
            MomentTab.PHOTO -> PhotoTab(state, actions, navigation)
            MomentTab.PLAN -> InfoBanner(stringResource(R.string.memories_plan_soon), tag = "moment_plan_soon")
            MomentTab.REVIEW -> Column(verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
                Text(stringResource(R.string.memories_review_body), style = MaterialTheme.typography.bodyLarge)
                WhereverPrimaryButton(
                    stringResource(R.string.memories_review_action),
                    navigation.onReviews,
                    tag = "moment_reviews"
                )
            }
        }
    }
}

@Composable
private fun PhotoTab(state: CreateMomentUiState, actions: CreateMomentActions, navigation: CreateMomentNavigation) {
    val loadError = state.loadError
    val selected = state.selected
    when {
        state.loading -> ListSkeleton(rows = 2)
        loadError != null -> ErrorState(loadError.asString(), actions.onRetry)
        selected == null -> Column(verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Text(
                stringResource(R.string.memories_no_postcards),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("moment_no_postcards")
            )
            WhereverPrimaryButton(stringResource(R.string.memories_take_postcard), navigation.onCamera)
        }
        else -> {
            PostcardPreview(
                photo = selected.imageUrl,
                style = selected.style,
                title = selected.text.ifBlank { stringResource(R.string.memories_postcard_title) },
                subtitle = memoryDate(selected.createdAt)
            )
            if (state.postcards.size > 1) {
                WhereverTextLink(stringResource(R.string.memories_change_postcard), actions.onChoosePostcard)
            }
            LabeledTextField(
                label = stringResource(R.string.memories_moment_label),
                value = state.text,
                onValueChange = actions.onText,
                placeholder = stringResource(R.string.memories_moment_placeholder),
                singleLine = false,
                supporting = stringResource(R.string.common_char_count, state.text.length, MOMENT_TEXT_MAX),
                tag = "moment_text"
            )
            SurfaceCard {
                if (state.friends.isNotEmpty()) {
                    NavRow(
                        title = stringResource(R.string.memories_tag_row),
                        onClick = actions.onChooseFriends,
                        value = if (state.tagged.isEmpty()) {
                            stringResource(R.string.memories_tag_none)
                        } else {
                            stringResource(R.string.memories_tag_count, state.tagged.size)
                        },
                        tag = "moment_tag"
                    )
                }
                Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    Text(stringResource(R.string.memories_visibility_row), style = MaterialTheme.typography.bodyLarge)
                    SegmentedControl(
                        options = listOf(
                            stringResource(R.string.memories_visibility_friends),
                            stringResource(R.string.memories_visibility_public)
                        ),
                        selectedIndex = if (state.visibility == Visibility.PUBLIC) 1 else 0,
                        onSelect = { actions.onVisibility(if (it == 1) Visibility.PUBLIC else Visibility.FRIENDS) }
                    )
                }
            }
            state.error?.let { ErrorBanner(it.asString()) }
        }
    }
}

private val PREVIEW_POSTCARD = Postcard(
    id = "c1",
    imageUrl = "https://ejemplo.invalid/postal.jpg",
    style = PostcardStyle.CLASSIC,
    text = "Domingo de jardín y aire fresco",
    visibility = Visibility.PRIVATE,
    state = PostcardState.READY,
    planId = null,
    placeId = null,
    version = 1,
    createdAt = "2026-10-11T18:00:00Z"
)

private val PREVIEW_MOMENT_ACTIONS = CreateMomentActions({}, {}, {}, {}, {}, {}, {})
private val PREVIEW_MOMENT_NAVIGATION = CreateMomentNavigation({}, {}, {}, {})

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CreateMomentPreview() {
    WhereverTheme {
        CreateMomentContent(
            CreateMomentUiState(
                loading = false,
                postcards = listOf(PREVIEW_POSTCARD),
                selected = PREVIEW_POSTCARD,
                text = "Cero prisa. Ya saben dónde es.",
                friends = listOf(Person("u1", "Mariana", "mariana", null), Person("u2", "Jorge", "jorge", null)),
                tagged = setOf("u1", "u2")
            ),
            PREVIEW_MOMENT_ACTIONS,
            PREVIEW_MOMENT_NAVIGATION
        )
    }
}
