package com.wherever.mobile.feature.groups.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ConfirmDialog
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.NavRow
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatDate
import com.wherever.mobile.feature.groups.domain.GroupMember
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.profile.ui.ProfileAvatar
import java.time.ZoneOffset
import kotlinx.coroutines.delay

data class GroupDetailNavigation(
    val onBack: () -> Unit = {},
    val onPlan: (String) -> Unit = {},
    val onVotes: (String) -> Unit = {},
    val onVaca: (String) -> Unit = {},
    /** 43_CrearGrupo en modo invitar: plan y grupo. */
    val onInvite: (planId: String, groupId: String) -> Unit = { _, _ -> },
    val onGone: () -> Unit = {}
)

data class GroupDetailActions(
    val onRetry: () -> Unit = {},
    val onRename: () -> Unit = {},
    val onName: (String) -> Unit = {},
    val onSaveName: () -> Unit = {},
    val onCancelName: () -> Unit = {},
    val onLeave: () -> Unit = {},
    val onRemove: (GroupMember) -> Unit = {}
)

/** 46_Grupo (specs/nv-25). */
@Composable
fun GroupDetailRoute(navigation: GroupDetailNavigation, viewModel: GroupDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Al volver de invitar o votar se recargan integrantes y votaciones.
    LifecycleResumeEffect(viewModel) {
        viewModel.load()
        onPauseOrDispose { }
    }
    LaunchedEffect(state.gone) { if (state.gone) navigation.onGone() }
    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(DETAIL_MESSAGE_MILLIS)
            viewModel.messageShown()
        }
    }
    GroupDetailContent(
        state = state,
        navigation = navigation,
        actions = GroupDetailActions(
            onRetry = viewModel::load,
            onRename = viewModel::startRename,
            onName = viewModel::onName,
            onSaveName = viewModel::saveName,
            onCancelName = viewModel::cancelRename,
            onLeave = viewModel::askLeave,
            onRemove = viewModel::askRemove
        )
    )
    if (state.confirmLeave) {
        ConfirmDialog(
            title = stringResource(R.string.group_detail_leave_title),
            message = stringResource(R.string.group_detail_leave_body),
            confirmText = stringResource(R.string.group_detail_leave),
            onConfirm = viewModel::leave,
            onDismiss = viewModel::dismissConfirm
        )
    }
    state.confirmRemove?.let { member ->
        ConfirmDialog(
            title = stringResource(R.string.group_detail_remove_title, member.name),
            message = stringResource(R.string.group_detail_remove_body),
            confirmText = stringResource(R.string.group_detail_remove),
            onConfirm = viewModel::remove,
            onDismiss = viewModel::dismissConfirm
        )
    }
}

private const val DETAIL_MESSAGE_MILLIS = 4_000L

@Composable
internal fun GroupDetailContent(
    state: GroupDetailUiState,
    navigation: GroupDetailNavigation,
    actions: GroupDetailActions
) {
    val group = state.group
    if (group == null) {
        FormScreen {
            TopTitleRow(stringResource(R.string.group_detail_title), navigation.onBack)
            if (state.loadError != null) {
                ErrorState(state.loadError.asString(), actions.onRetry)
            } else {
                LoadingState()
            }
        }
        return
    }
    FormScreen(
        footer = {
            if (group.iAmOrganizer) {
                WhereverPrimaryButton(
                    text = stringResource(R.string.group_detail_invite),
                    onClick = { navigation.onInvite(group.planId, group.id) },
                    enabled = !state.busy,
                    tag = "group_invite"
                )
            } else {
                WhereverTextLink(
                    stringResource(R.string.group_detail_leave),
                    onClick = actions.onLeave,
                    enabled = !state.busy,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    ) {
        TopTitleRow(stringResource(R.string.group_detail_title), navigation.onBack)
        NameCard(state, actions)
        state.message?.let { InfoBanner(it.asString(), icon = Icons.Outlined.Check, tag = "group_message") }
        state.error?.let { ErrorBanner(it.asString()) }
        state.plan?.let { PlanCard(it, navigation.onPlan) }
        SurfaceCard {
            Column {
                NavRow(
                    title = stringResource(R.string.group_detail_votes),
                    onClick = { navigation.onVotes(group.id) },
                    value = if (state.openVotes > 0) {
                        pluralStringResource(R.plurals.group_detail_open_votes, state.openVotes, state.openVotes)
                    } else {
                        null
                    },
                    tag = "group_votes"
                )
                NavRow(
                    title = stringResource(R.string.vaca_title),
                    onClick = { navigation.onVaca(group.id) },
                    tag = "group_vaca"
                )
            }
        }
        Text(
            pluralStringResource(R.plurals.groups_people, state.members.size, state.members.size),
            style = MaterialTheme.typography.titleMedium
        )
        state.members.forEach { member ->
            MemberRow(member, canRemove = group.iAmOrganizer && !member.organizer && !state.busy, actions.onRemove)
        }
    }
}

@Composable
private fun NameCard(state: GroupDetailUiState, actions: GroupDetailActions) {
    val group = state.group ?: return
    val draft = state.nameDraft
    if (draft != null) {
        LabeledTextField(
            label = stringResource(R.string.group_detail_name),
            value = draft,
            onValueChange = actions.onName,
            placeholder = stringResource(R.string.group_detail_name_hint),
            supporting = stringResource(R.string.group_detail_name_count, draft.length, MAX_GROUP_NAME),
            modifier = Modifier.testTag("group_name_field")
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
            WhereverOutlinedButton(
                text = stringResource(R.string.common_cancel),
                onClick = actions.onCancelName,
                modifier = Modifier.weight(1f)
            )
            WhereverPrimaryButton(
                text = stringResource(R.string.common_save),
                onClick = actions.onSaveName,
                enabled = state.canSaveName,
                loading = state.busy,
                modifier = Modifier.weight(1f),
                tag = "group_name_save"
            )
        }
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            state.group.name ?: state.plan?.name ?: groupTitle(group),
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).testTag("group_name")
        )
        if (group.iAmOrganizer) {
            WhereverIconButton(onClick = actions.onRename, modifier = Modifier.testTag("group_rename")) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.group_detail_rename))
            }
        }
    }
}

@Composable
private fun PlanCard(plan: Plan, onPlan: (String) -> Unit) {
    SurfaceCard(modifier = Modifier.testTag("group_plan")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Text(plan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(
                    R.string.groups_chat_plan_line,
                    formatDate("${plan.date}T12:00:00Z", ZoneOffset.UTC),
                    pluralStringResource(R.plurals.groups_stops, plan.stops.size, plan.stops.size),
                    plan.costPerPerson
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            WhereverOutlinedButton(
                text = stringResource(R.string.groups_chat_see_plan),
                onClick = { onPlan(plan.id) },
                tag = "group_see_plan"
            )
        }
    }
}

@Composable
private fun MemberRow(member: GroupMember, canRemove: Boolean, onRemove: (GroupMember) -> Unit) {
    SurfaceCard(modifier = Modifier.testTag("member_${member.userId}")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier.fillMaxWidth().padding(Spacing.M)
        ) {
            ProfileAvatar(member.name, member.photoUrl, size = Sizes.AvatarList)
            Text(
                member.name.ifBlank { stringResource(R.string.group_detail_unknown_member) },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            when {
                member.organizer -> StatusPill(stringResource(R.string.group_detail_organizer))
                member.status == MemberStatus.INVITED -> StatusPill(
                    stringResource(R.string.group_detail_invited),
                    container = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (canRemove) {
                WhereverIconButton(
                    onClick = { onRemove(member) },
                    modifier = Modifier.testTag("member_remove_${member.userId}")
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.group_detail_remove_a11y, member.name)
                    )
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun GroupDetailPreview() {
    WhereverTheme {
        GroupDetailContent(
            GroupDetailUiState(
                loading = false,
                group = PREVIEW_GROUPS.first().copy(iAmOrganizer = true),
                votes = PREVIEW_VOTES
            ),
            GroupDetailNavigation(),
            GroupDetailActions()
        )
    }
}
