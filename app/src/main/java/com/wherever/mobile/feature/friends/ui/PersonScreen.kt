package com.wherever.mobile.feature.friends.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ConfirmDialog
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.WhereverDangerButton
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.feature.friends.domain.PersonDetail
import com.wherever.mobile.feature.friends.domain.Relation
import com.wherever.mobile.feature.profile.ui.ProfileAvatar

/** 25_PerfilAmigo (US-233, US-238). */
@Composable
fun PersonRoute(onBack: () -> Unit, onBlocked: () -> Unit, viewModel: PersonViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.blocked) { if (state.blocked) onBlocked() }
    val detail = state.detail
    val loadError = state.loadError
    when {
        state.loading -> LoadingState()
        detail == null && loadError != null -> FormScreen {
            BackCircleButton(onClick = onBack)
            ErrorState(loadError.asString(), viewModel::load)
        }
        detail != null -> PersonContent(detail, state, viewModel, onBack)
    }
    when (state.confirm) {
        PersonConfirm.REMOVE -> ConfirmDialog(
            title = stringResource(R.string.friends_remove_title, detail?.person?.name.orEmpty()),
            message = stringResource(R.string.friends_remove_message),
            confirmText = stringResource(R.string.friends_remove),
            onConfirm = viewModel::confirm,
            onDismiss = viewModel::dismissConfirm
        )
        PersonConfirm.BLOCK -> ConfirmDialog(
            title = stringResource(R.string.friends_block_title, detail?.person?.name.orEmpty()),
            message = stringResource(R.string.friends_block_message),
            confirmText = stringResource(R.string.friends_block),
            onConfirm = viewModel::confirm,
            onDismiss = viewModel::dismissConfirm
        )
        null -> Unit
    }
}

@Composable
private fun PersonContent(detail: PersonDetail, state: PersonUiState, viewModel: PersonViewModel, onBack: () -> Unit) {
    FormScreen(footer = { Actions(detail.relation, state, viewModel) }) {
        BackCircleButton(onClick = onBack)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.S),
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileAvatar(detail.person.name, detail.person.photoUrl, size = Sizes.AvatarXL)
            Text(
                detail.person.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.testTag("person_name")
            )
            detail.person.username?.let {
                Text(
                    stringResource(R.string.friends_username_at, it),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                relationLabel(detail.relation),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("person_relation")
            )
            FriendFacts(detail)
        }
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

@Composable
private fun FriendFacts(detail: PersonDetail) {
    detail.level?.let { Text(stringResource(R.string.friends_level, it)) }
    detail.mutualFriends?.let {
        Text(
            pluralStringResource(R.plurals.friends_mutual, it, it),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    detail.mutualGroups?.takeIf { it > 0 }?.let {
        Text(
            pluralStringResource(R.plurals.friends_mutual_groups, it, it),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Actions(relation: Relation, state: PersonUiState, viewModel: PersonViewModel) {
    val working = state.working
    val hasFriendship = state.friendshipId != null
    when (relation) {
        Relation.NONE -> WhereverPrimaryButton(
            stringResource(R.string.friends_add_person),
            viewModel::add,
            loading = working,
            tag = "person_add"
        )
        Relation.REQUEST_SENT -> WhereverOutlinedButton(
            stringResource(R.string.friends_cancel_request),
            viewModel::cancelRequest,
            enabled = !working && hasFriendship,
            tag = "person_cancel"
        )
        Relation.REQUEST_RECEIVED -> {
            WhereverPrimaryButton(
                stringResource(R.string.friends_accept),
                viewModel::accept,
                enabled = hasFriendship,
                loading = working,
                tag = "person_accept"
            )
            WhereverOutlinedButton(
                stringResource(R.string.friends_reject),
                viewModel::reject,
                enabled = !working && hasFriendship,
                tag = "person_reject"
            )
        }
        Relation.FRIEND -> {
            WhereverDangerButton(
                text = stringResource(R.string.friends_remove),
                onClick = viewModel::askRemove,
                enabled = !working && hasFriendship,
                tag = "person_remove"
            )
            WhereverTextLink(
                stringResource(R.string.friends_block),
                viewModel::askBlock,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
