package com.wherever.mobile.feature.groups.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.AvatarStack
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.SunsetTint
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.feed.ui.relativeTime
import com.wherever.mobile.feature.groups.domain.Group
import com.wherever.mobile.feature.groups.domain.GroupMember
import com.wherever.mobile.feature.groups.domain.LastMessage
import com.wherever.mobile.feature.groups.domain.MemberStatus
import java.time.Instant
import kotlinx.coroutines.delay

/** 44_MisGrupos (specs/nv-25). */
@Composable
fun MyGroupsRoute(
    onBack: () -> Unit,
    onGroup: (String) -> Unit,
    onNewGroup: () -> Unit,
    viewModel: MyGroupsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.load()
        onPauseOrDispose { }
    }
    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(MESSAGE_MILLIS)
            viewModel.messageShown()
        }
    }
    MyGroupsContent(
        state = state,
        now = Instant.now(),
        actions = MyGroupsActions(
            onBack = onBack,
            onGroup = onGroup,
            onNewGroup = onNewGroup,
            onRetry = viewModel::load,
            onMore = viewModel::loadMore,
            onRespond = viewModel::respond
        )
    )
}

private const val MESSAGE_MILLIS = 4_000L

data class MyGroupsActions(
    val onBack: () -> Unit = {},
    val onGroup: (String) -> Unit = {},
    val onNewGroup: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onMore: () -> Unit = {},
    val onRespond: (Group, Boolean) -> Unit = { _, _ -> }
)

@Composable
fun MyGroupsContent(state: MyGroupsUiState, now: Instant, actions: MyGroupsActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = ScreenPadding, vertical = Spacing.L),
        verticalArrangement = Arrangement.spacedBy(Spacing.L)
    ) {
        TopTitleRow(stringResource(R.string.groups_title), actions.onBack)
        state.message?.let { InfoBanner(it.asString(), icon = Icons.Outlined.Check, tag = "groups_message") }
        state.error?.let { ErrorBanner(it.asString()) }
        when {
            state.loading -> ListSkeleton()
            state.loadError != null -> ErrorState(state.loadError.asString(), actions.onRetry)
            state.empty -> EmptyGroups(actions.onNewGroup)
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.M),
                modifier = Modifier.fillMaxWidth().testTag("groups_list")
            ) {
                items(state.groups, key = Group::id) { group ->
                    GroupCard(group, group.id in state.responding, now, actions)
                }
                item { NewGroupCard(actions.onNewGroup) }
                if (state.nextCursor != null) {
                    item {
                        WhereverOutlinedButton(
                            text = stringResource(R.string.groups_more),
                            onClick = actions.onMore,
                            enabled = !state.loadingMore,
                            modifier = Modifier.fillMaxWidth(),
                            tag = "groups_more"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupCard(group: Group, responding: Boolean, now: Instant, actions: MyGroupsActions) {
    val invited = group.myStatus == MemberStatus.INVITED
    SurfaceCard(modifier = Modifier.testTag("group_${group.id}")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !invited) { actions.onGroup(group.id) }
                .padding(Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.S)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarStack(group.members.map(GroupMember::name), size = Sizes.AvatarS)
                Spacer(Modifier.width(Spacing.M))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            groupTitle(group),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        val time = group.lastMessage?.sentAt ?: group.updatedAt
                        relativeTime(time, now)?.let {
                            Text(
                                it.asString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    lastMessageLine(group)?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            GroupStatus(group)
            if (invited) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    WhereverOutlinedButton(
                        text = stringResource(R.string.groups_decline),
                        onClick = { actions.onRespond(group, false) },
                        enabled = !responding,
                        modifier = Modifier.weight(1f),
                        tag = "group_decline_${group.id}"
                    )
                    WhereverPrimaryButton(
                        text = stringResource(R.string.groups_accept),
                        onClick = { actions.onRespond(group, true) },
                        loading = responding,
                        modifier = Modifier.weight(1f),
                        tag = "group_accept_${group.id}"
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupStatus(group: Group) {
    when (group.myStatus) {
        MemberStatus.INVITED -> StatusPill(
            stringResource(R.string.groups_status_invited),
            container = SunsetTint,
            contentColor = Ink
        )
        MemberStatus.CONFIRMED -> StatusPill(
            pluralStringResource(R.plurals.groups_confirmed, group.confirmed, group.confirmed),
            container = MintTint,
            contentColor = Ink
        )
        MemberStatus.DECLINED -> Unit
    }
}

@Composable
internal fun groupTitle(group: Group): String = group.name ?: membersTitle(group.members)

@Composable
private fun membersTitle(members: List<GroupMember>): String {
    val names = members.map { it.name.substringBefore(' ') }.filter { it.isNotBlank() }
    return when {
        names.isEmpty() -> stringResource(R.string.groups_untitled)
        names.size <= MAX_NAMES -> names.joinToString(", ")
        else -> stringResource(
            R.string.groups_members_more,
            names.take(MAX_NAMES).joinToString(", "),
            names.size - MAX_NAMES
        )
    }
}

private const val MAX_NAMES = 2

@Composable
private fun lastMessageLine(group: Group): String? {
    val last: LastMessage = group.lastMessage ?: return null
    val author = group.members.firstOrNull { it.userId == last.authorId }?.name?.substringBefore(' ')
    return if (author.isNullOrBlank()) last.text else stringResource(R.string.groups_last_message, author, last.text)
}

@Composable
private fun NewGroupCard(onNewGroup: () -> Unit) {
    SurfaceCard(modifier = Modifier.testTag("groups_new")) {
        Column(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onNewGroup).padding(Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.XS)
        ) {
            Text(stringResource(R.string.groups_new), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.groups_new_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyGroups(onNewGroup: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.L),
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Huge).testTag("groups_empty")
    ) {
        Text(stringResource(R.string.groups_empty_title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.groups_new_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        WhereverPrimaryButton(stringResource(R.string.groups_new), onNewGroup, tag = "groups_empty_new")
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MyGroupsPreview() {
    WhereverTheme {
        MyGroupsContent(
            MyGroupsUiState(loading = false, groups = PREVIEW_GROUPS),
            now = Instant.parse("2026-11-20T18:00:00Z"),
            actions = MyGroupsActions()
        )
    }
}

/** Datos de ejemplo de las vistas previas y capturas (nunca se muestran en la app). */
internal val PREVIEW_GROUPS = listOf(
    previewGroup("g1", "Viernes de tacos", MemberStatus.CONFIRMED, "Yo voto por los tacos", "2026-11-20T17:59:00Z"),
    previewGroup("g2", "Nosotros", MemberStatus.CONFIRMED, "¡Ya reservé!", "2026-11-19T20:00:00Z"),
    previewGroup("g3", null, MemberStatus.INVITED, null, "2026-11-16T10:00:00Z")
)

private fun previewGroup(id: String, name: String?, status: MemberStatus, last: String?, at: String) = Group(
    id = id,
    planId = "p$id",
    name = name,
    myStatus = status,
    iAmOrganizer = false,
    confirmed = 3,
    members = listOf(
        GroupMember("u1", "Jorge Rodríguez", null, true, MemberStatus.CONFIRMED),
        GroupMember("u2", "Mariana López", null, false, MemberStatus.CONFIRMED),
        GroupMember("u3", "Diego Reséndiz", null, false, MemberStatus.INVITED)
    ),
    lastMessage = last?.let { LastMessage(it, "u1", at) },
    updatedAt = at
)
