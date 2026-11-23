package com.wherever.mobile.feature.groups.ui

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.friends.ui.PersonRow

/** 43_CrearGrupo (specs/nv-25). */
@Composable
fun CreateGroupRoute(
    onBack: () -> Unit,
    onDone: (String) -> Unit,
    onAddFriends: () -> Unit,
    viewModel: CreateGroupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateGroupEvent.Done -> onDone(event.groupId)
            }
        }
    }
    CreateGroupContent(
        state,
        CreateGroupActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onName = viewModel::onName,
            onQuery = viewModel::onQuery,
            onToggle = viewModel::toggle,
            onSubmit = viewModel::submit,
            onAddFriends = onAddFriends
        )
    )
}

@Composable
internal fun CreateGroupContent(state: CreateGroupUiState, actions: CreateGroupActions) {
    val title = stringResource(if (state.inviting) R.string.create_group_invite_title else R.string.create_group_title)
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = if (state.inviting) {
                    pluralStringResource(R.plurals.create_group_send_invites, state.selected.size, state.selected.size)
                } else {
                    stringResource(R.string.create_group_submit)
                },
                onClick = actions.onSubmit,
                enabled = state.canSubmit,
                loading = state.saving,
                tag = "create_group_submit"
            )
        }
    ) {
        TopTitleRow(title, actions.onBack)
        state.planName?.let {
            TitleBlock(it, subtitle = stringResource(R.string.create_group_body, MAX_INVITEES))
        }
        if (!state.inviting) {
            LabeledTextField(
                label = stringResource(R.string.create_group_name),
                value = state.name,
                onValueChange = actions.onName,
                placeholder = stringResource(R.string.group_detail_name_hint),
                supporting = stringResource(R.string.create_group_name_optional),
                modifier = Modifier.testTag("create_group_name")
            )
        }
        state.error?.let { ErrorBanner(it.asString()) }
        when {
            state.loading -> LoadingState()
            state.loadError != null -> ErrorState(state.loadError.asString(), actions.onRetry)
            state.friends.isEmpty() -> {
                Text(
                    stringResource(R.string.create_group_no_friends),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("create_group_no_friends")
                )
                WhereverPrimaryButton(
                    text = stringResource(R.string.friends_add_title),
                    onClick = actions.onAddFriends,
                    tag = "create_group_add_friends"
                )
            }
            else -> FriendPicker(state, actions)
        }
    }
}

@Composable
private fun FriendPicker(state: CreateGroupUiState, actions: CreateGroupActions) {
    LabeledTextField(
        label = stringResource(R.string.create_group_who),
        value = state.query,
        onValueChange = actions.onQuery,
        placeholder = stringResource(R.string.create_group_search),
        leadingIcon = Icons.Outlined.Search,
        supporting = stringResource(R.string.create_group_seats, state.selected.size, state.seatsLeft),
        modifier = Modifier.testTag("create_group_search")
    )
    state.visibleFriends.forEach { person ->
        val inGroup = person.id in state.alreadyIn
        val pickable = state.canPick(person.id)
        PersonRow(
            person,
            onClick = if (pickable) ({ actions.onToggle(person) }) else null,
            detail = if (inGroup) stringResource(R.string.create_group_already_in) else null
        ) {
            Checkbox(
                checked = inGroup || person.id in state.selected,
                onCheckedChange = null,
                enabled = pickable,
                modifier = Modifier.testTag("create_group_pick_${person.id}")
            )
        }
    }
}

private val PREVIEW_FRIENDS = listOf(
    Person("u1", "Mariana López", "mariana", null),
    Person("u2", "Luis Mendoza", "luis", null),
    Person("u3", "Sofía Ramírez", "sofia", null)
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CreateGroupPreview() {
    WhereverTheme {
        CreateGroupContent(
            CreateGroupUiState(
                loading = false,
                planName = "Domingo en Coyoacán",
                name = "Los del domingo",
                friends = PREVIEW_FRIENDS,
                selected = setOf("u1", "u3")
            ),
            CreateGroupActions()
        )
    }
}

@Preview
@Composable
internal fun InviteToGroupPreview() {
    WhereverTheme {
        CreateGroupContent(
            CreateGroupUiState(
                loading = false,
                inviting = true,
                planName = "Domingo en Coyoacán",
                friends = PREVIEW_FRIENDS,
                alreadyIn = setOf("u2"),
                selected = setOf("u1")
            ),
            CreateGroupActions()
        )
    }
}
