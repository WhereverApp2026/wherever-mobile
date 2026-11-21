package com.wherever.mobile.feature.friends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.feature.friends.domain.Friendship
import com.wherever.mobile.feature.friends.domain.FriendshipList

/** 24_Amigos (US-233). */
@Composable
fun FriendsRoute(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onPerson: (String) -> Unit,
    onGroups: () -> Unit,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.load()
        onPauseOrDispose { }
    }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(ScreenPadding)) {
            TopTitleRow(stringResource(R.string.friends_title), onBack, Modifier.weight(1f))
            WhereverTextLink(stringResource(R.string.groups_title), onGroups, Modifier.testTag("friends_groups"))
            WhereverTextLink(stringResource(R.string.friends_add), onAdd)
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier.padding(horizontal = ScreenPadding)
        ) {
            SegmentedControl(
                options = listOf(
                    stringResource(R.string.friends_tab_friends),
                    if (state.pending > 0) {
                        stringResource(R.string.friends_tab_received_count, state.pending)
                    } else {
                        stringResource(R.string.friends_tab_received)
                    },
                    stringResource(R.string.friends_tab_sent)
                ),
                selectedIndex = state.tab.ordinal,
                onSelect = { viewModel.onTab(FriendshipList.entries[it]) }
            )
            if (state.tab == FriendshipList.FRIENDS && state.items.isNotEmpty()) {
                LabeledTextField(
                    label = stringResource(R.string.friends_filter),
                    value = state.query,
                    onValueChange = viewModel::onQuery,
                    leadingIcon = Icons.Outlined.Search,
                    tag = "friends_filter"
                )
            }
            state.error?.let { ErrorBanner(it.asString()) }
        }
        val loadError = state.loadError
        when {
            state.loading -> ListSkeleton()
            loadError != null -> ErrorState(loadError.asString(), viewModel::load)
            state.visible.isEmpty() -> EmptyFriends(state.tab, onAdd)
            else -> LazyColumn(
                contentPadding = PaddingValues(ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.M),
                modifier = Modifier.testTag("friends_list")
            ) {
                items(state.visible, key = { it.id }) { friendship ->
                    FriendshipRow(friendship, state.tab, friendship.id in state.busy, viewModel, onPerson)
                }
            }
        }
    }
}

@Composable
private fun FriendshipRow(
    friendship: Friendship,
    tab: FriendshipList,
    busy: Boolean,
    viewModel: FriendsViewModel,
    onPerson: (String) -> Unit
) {
    PersonRow(friendship.person, onClick = { onPerson(friendship.person.id) }) {
        when (tab) {
            FriendshipList.FRIENDS -> Unit
            FriendshipList.RECEIVED -> {
                WhereverIconButton(
                    onClick = { viewModel.reject(friendship) },
                    enabled = !busy,
                    modifier = Modifier.testTag("friends_reject_${friendship.id}")
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.friends_reject),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                WhereverIconButton(
                    onClick = { viewModel.accept(friendship) },
                    enabled = !busy,
                    modifier = Modifier.testTag("friends_accept_${friendship.id}")
                ) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.friends_accept),
                        tint = Sunset
                    )
                }
            }
            FriendshipList.SENT -> if (!busy) {
                WhereverTextLink(stringResource(R.string.friends_cancel_request), { viewModel.cancel(friendship) })
            }
        }
    }
}

@Composable
private fun EmptyFriends(tab: FriendshipList, onAdd: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.L),
        modifier = Modifier.padding(ScreenPadding).testTag("friends_empty")
    ) {
        Text(
            stringResource(
                when (tab) {
                    FriendshipList.FRIENDS -> R.string.friends_empty
                    FriendshipList.RECEIVED -> R.string.friends_empty_received
                    FriendshipList.SENT -> R.string.friends_empty_sent
                }
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (tab == FriendshipList.FRIENDS) {
            WhereverPrimaryButton(stringResource(R.string.friends_add), onAdd, modifier = Modifier.fillMaxWidth())
        }
    }
}
