package com.wherever.mobile.feature.friends.ui

import android.content.Intent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.NavRow
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.friends.domain.Relation

/** 26_AgregarAmigos (US-232, US-234, US-235). */
@Composable
fun AddFriendRoute(onBack: () -> Unit, onPerson: (String) -> Unit, viewModel: AddFriendViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FormScreen {
        TopTitleRow(stringResource(R.string.friends_add_title), onBack)
        state.message?.let { InfoBanner(it.asString(), icon = Icons.Filled.Check, tag = "add_friend_message") }
        state.error?.let { ErrorBanner(it.asString()) }
        MyIdentity(state, viewModel)
        LabeledTextField(
            label = stringResource(R.string.friends_search_label),
            value = state.query,
            onValueChange = viewModel::onQuery,
            placeholder = stringResource(R.string.friends_search_hint),
            leadingIcon = Icons.Filled.Search,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
            tag = "add_friend_query"
        )
        WhereverOutlinedButton(
            text = if (state.searching) {
                stringResource(R.string.common_loading)
            } else {
                stringResource(R.string.friends_search_action)
            },
            onClick = viewModel::search,
            enabled = state.canSearch,
            tag = "add_friend_search"
        )
        state.results?.let { results ->
            if (results.isEmpty()) {
                Text(
                    stringResource(R.string.friends_search_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("add_friend_no_results")
                )
            }
            results.forEach { person -> AddablePerson(person, state, viewModel, onPerson) }
        }
        CodeSection(state, viewModel, onPerson)
        if (state.suggestions.isNotEmpty()) {
            Text(stringResource(R.string.friends_suggestions), style = MaterialTheme.typography.titleMedium)
            state.suggestions.forEach { suggestion ->
                AddablePerson(
                    suggestion.person,
                    state,
                    viewModel,
                    onPerson,
                    detail = pluralStringResource(
                        R.plurals.friends_mutual,
                        suggestion.mutualFriends,
                        suggestion.mutualFriends
                    )
                )
            }
        }
    }
    if (state.editingUsername) UsernameDialog(state, viewModel)
}

@Composable
private fun MyIdentity(state: AddFriendUiState, viewModel: AddFriendViewModel) {
    val context = LocalContext.current
    val shareText = state.myCode?.let { stringResource(R.string.friends_share_text, it) }
    SurfaceCard {
        NavRow(
            stringResource(R.string.friends_my_username),
            viewModel::editUsername,
            value = state.myUsername?.let { stringResource(R.string.friends_username_at, it) }
                ?: stringResource(R.string.friends_username_missing),
            tag = "add_friend_username"
        )
        state.myCode?.let { code ->
            NavRow(
                stringResource(R.string.friends_my_code),
                {
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, shareText)
                    context.startActivity(Intent.createChooser(send, null))
                },
                value = code,
                tag = "add_friend_my_code"
            )
        }
    }
    if (state.myCode != null) {
        InfoBanner(stringResource(R.string.friends_my_code_hint), icon = Icons.Filled.Share)
    }
}

@Composable
private fun CodeSection(state: AddFriendUiState, viewModel: AddFriendViewModel, onPerson: (String) -> Unit) {
    LabeledTextField(
        label = stringResource(R.string.friends_code_label),
        value = state.code,
        onValueChange = viewModel::onCode,
        placeholder = stringResource(R.string.friends_code_hint),
        error = state.codeError?.asString(),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(onSearch = { viewModel.findByCode() }),
        tag = "add_friend_code"
    )
    WhereverOutlinedButton(
        text = stringResource(R.string.friends_code_action),
        onClick = viewModel::findByCode,
        enabled = state.code.isNotBlank(),
        tag = "add_friend_code_search"
    )
    state.codeResult?.let { found ->
        if (found.relation == Relation.NONE) {
            AddablePerson(found.person, state, viewModel, onPerson)
        } else {
            PersonRow(found.person, onClick = { onPerson(found.person.id) }, detail = relationLabel(found.relation))
        }
    }
}

@Composable
private fun AddablePerson(
    person: Person,
    state: AddFriendUiState,
    viewModel: AddFriendViewModel,
    onPerson: (String) -> Unit,
    detail: String? = null
) {
    val sent = person.id in state.sent
    PersonRow(
        person,
        onClick = { onPerson(person.id) },
        detail = if (sent) stringResource(R.string.friends_request_pending) else detail
    ) {
        if (!sent) {
            IconButton(
                onClick = { viewModel.request(person) },
                enabled = person.id !in state.sending,
                modifier = Modifier.testTag("add_friend_request_${person.id}")
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.friends_add_person), tint = Sunset)
            }
        }
    }
}

@Composable
internal fun relationLabel(relation: Relation): String = stringResource(
    when (relation) {
        Relation.FRIEND -> R.string.friends_relation_friend
        Relation.REQUEST_SENT -> R.string.friends_request_pending
        Relation.REQUEST_RECEIVED -> R.string.friends_relation_received
        Relation.NONE -> R.string.friends_relation_none
    }
)

@Composable
private fun UsernameDialog(state: AddFriendUiState, viewModel: AddFriendViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::dismissUsername,
        title = { Text(stringResource(R.string.friends_my_username)) },
        text = {
            LabeledTextField(
                label = stringResource(R.string.friends_username_label),
                value = state.usernameDraft,
                onValueChange = viewModel::onUsername,
                error = state.usernameError?.asString(),
                supporting = stringResource(R.string.friends_username_rule),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.saveUsername() }),
                tag = "username_field"
            )
        },
        confirmButton = {
            TextButton(
                onClick = viewModel::saveUsername,
                enabled = state.canSaveUsername,
                modifier = Modifier.testTag("username_save")
            ) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = {
            TextButton(onClick = viewModel::dismissUsername) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}
