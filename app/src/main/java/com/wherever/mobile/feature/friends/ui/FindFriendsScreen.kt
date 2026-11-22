package com.wherever.mobile.feature.friends.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.friends.domain.ContactMatch
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.friends.domain.Relation

/** 07_EncuentraAmigos: «Con amigos es mejor». Los contactos se piden en contexto, al tocar el botón. */
@Composable
fun FindFriendsRoute(
    onBack: () -> Unit,
    onPerson: (String) -> Unit,
    viewModel: FindFriendsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.onPermission(it)
    }
    FindFriendsContent(
        state = state,
        onBack = onBack,
        actions = FindFriendsActions(
            onSearch = {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                    PackageManager.PERMISSION_GRANTED
                if (granted) viewModel.search() else permission.launch(Manifest.permission.READ_CONTACTS)
            },
            onRequest = viewModel::request,
            onPerson = onPerson,
            onDone = onBack
        )
    )
}

@Composable
internal fun FindFriendsContent(state: FindFriendsUiState, onBack: () -> Unit, actions: FindFriendsActions) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.find_friends_search),
                onClick = actions.onSearch,
                loading = state.searching,
                tag = "find_friends_search"
            )
            WhereverTextLink(stringResource(R.string.find_friends_later), actions.onDone)
        }
    ) {
        TopTitleRow(stringResource(R.string.find_friends_title), onBack)
        TitleBlock(
            stringResource(R.string.find_friends_headline),
            subtitle = stringResource(R.string.find_friends_body)
        )
        InfoBanner(stringResource(R.string.find_friends_privacy), icon = Icons.Outlined.Lock)
        if (state.permissionDenied) {
            InfoBanner(stringResource(R.string.find_friends_denied), tag = "find_friends_denied")
        }
        state.error?.let { ErrorBanner(it.asString()) }
        state.matches?.let { matches ->
            if (matches.isEmpty()) {
                Text(
                    stringResource(R.string.find_friends_none),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("find_friends_none")
                )
            } else {
                Text(stringResource(R.string.find_friends_here), style = MaterialTheme.typography.titleMedium)
                matches.forEach { match -> MatchRow(match, match.person.id in state.sending, actions) }
            }
        }
    }
}

@Composable
private fun MatchRow(match: ContactMatch, sending: Boolean, actions: FindFriendsActions) {
    PersonRow(
        match.person,
        onClick = { actions.onPerson(match.person.id) },
        detail = if (match.relation == Relation.NONE) null else relationLabel(match.relation)
    ) {
        if (match.relation == Relation.NONE) {
            WhereverIconButton(
                onClick = { actions.onRequest(match) },
                enabled = !sending,
                modifier = Modifier.testTag("find_friends_add_${match.person.id}")
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.friends_add_person),
                    tint = Sunset
                )
            }
        }
    }
}

private val PREVIEW_MATCHES = listOf(
    ContactMatch(Person("u1", "Mariana López", "mariana", null), Relation.NONE),
    ContactMatch(Person("u2", "Luis Mendoza", "luis", null), Relation.REQUEST_SENT),
    ContactMatch(Person("u3", "Sofía Ramírez", "sofia", null), Relation.FRIEND)
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun FindFriendsPreview() {
    WhereverTheme {
        FindFriendsContent(
            FindFriendsUiState(matches = PREVIEW_MATCHES),
            onBack = {},
            actions = FindFriendsActions({}, {}, {}, {})
        )
    }
}
