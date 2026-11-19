package com.wherever.mobile.feature.notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.notifications.PushDestination
import com.wherever.mobile.core.ui.formatDateTime
import com.wherever.mobile.feature.notifications.domain.AppNotification
import com.wherever.mobile.feature.notifications.domain.NotificationType
import java.time.ZoneId

/** 18_Notif (specs/nv-52). */
@Composable
fun NotificationsRoute(
    onBack: () -> Unit,
    onOpen: (PushDestination) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.open.collect(onOpen) }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(ScreenPadding)
        ) {
            TopTitleRow(stringResource(R.string.notifications_title), onBack, Modifier.weight(1f))
            if (state.hasUnread) {
                WhereverTextLink(stringResource(R.string.notifications_mark_all), viewModel::markAllRead)
            }
        }
        ChipGroup(modifier = Modifier.padding(horizontal = ScreenPadding)) {
            NotificationFilter.entries.forEach { filter ->
                SelectChip(
                    text = stringResource(filterLabel(filter)),
                    selected = state.filter == filter,
                    onClick = { viewModel.onFilter(filter) }
                )
            }
        }
        state.error?.let { ErrorBanner(it.asString(), Modifier.padding(ScreenPadding)) }
        val loadError = state.loadError
        when {
            state.loading -> LoadingState()
            loadError != null -> ErrorState(loadError.asString(), viewModel::retry)
            state.visible.isEmpty() && state.nextCursor == null -> Text(
                stringResource(R.string.notifications_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(ScreenPadding).testTag("notifications_empty")
            )
            else -> NotificationList(state, viewModel)
        }
    }
}

@Composable
private fun NotificationList(state: NotificationsUiState, viewModel: NotificationsViewModel) {
    val zone = remember { ZoneId.systemDefault() }
    LazyColumn(
        contentPadding = PaddingValues(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.testTag("notifications_list")
    ) {
        items(state.visible, key = { it.id }) { notification ->
            NotificationRow(
                notification = notification,
                time = formatDateTime(notification.createdAt, zone),
                onClick = { viewModel.open(notification) },
                onDelete = { viewModel.delete(notification) }
            )
        }
        if (state.nextCursor != null) {
            item {
                WhereverOutlinedButton(
                    text = if (state.loadingMore) {
                        stringResource(R.string.common_loading)
                    } else {
                        stringResource(R.string.notifications_more)
                    },
                    onClick = viewModel::loadMore,
                    enabled = !state.loadingMore,
                    modifier = Modifier.fillMaxWidth(),
                    tag = "notifications_more"
                )
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: AppNotification, time: String, onClick: () -> Unit, onDelete: () -> Unit) {
    SurfaceCard(modifier = Modifier.testTag("notification_${notification.id}")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp)
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    iconFor(notification.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                if (!notification.read) {
                    Box(
                        Modifier.size(10.dp).background(Sunset, CircleShape).testTag("notification_unread")
                    )
                }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    stringResource(textFor(notification.type)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (notification.read) FontWeight.Normal else FontWeight.SemiBold
                )
                Text(
                    time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.notifications_delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun filterLabel(filter: NotificationFilter): Int = when (filter) {
    NotificationFilter.ALL -> R.string.notifications_filter_all
    NotificationFilter.SOCIAL -> R.string.notifications_filter_social
    NotificationFilter.PLANS -> R.string.notifications_filter_plans
}

private fun iconFor(type: NotificationType): ImageVector = when (type) {
    NotificationType.FRIEND_REQUEST, NotificationType.FRIEND_ACCEPTED -> Icons.Filled.Person
    NotificationType.PLAN_INVITATION -> Icons.Filled.DateRange
    NotificationType.VOTE_OPENED, NotificationType.VOTE_CLOSED -> Icons.Filled.ThumbUp
    NotificationType.OTHER -> Icons.Filled.Notifications
}

private fun textFor(type: NotificationType): Int = when (type) {
    NotificationType.FRIEND_REQUEST -> R.string.notification_friend_request
    NotificationType.FRIEND_ACCEPTED -> R.string.notification_friend_accepted
    NotificationType.PLAN_INVITATION -> R.string.notification_plan_invitation
    NotificationType.VOTE_OPENED -> R.string.notification_vote_opened
    NotificationType.VOTE_CLOSED -> R.string.notification_vote_closed
    NotificationType.OTHER -> R.string.notification_other
}
