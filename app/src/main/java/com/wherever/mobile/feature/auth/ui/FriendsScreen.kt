package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.components.WhereverPrimaryButton
import com.wherever.mobile.feature.auth.domain.FriendSuggestion
import com.wherever.mobile.feature.auth.ui.components.AuthErrorBanner

@Composable
fun FriendsScreen(onContinue: () -> Unit, viewModel: FriendsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { onContinue() }
    }
    FriendsContent(
        state = state,
        onSend = viewModel::sendRequest,
        onRetry = viewModel::load,
        onContinue = viewModel::continueFlow
    )
}

@Composable
fun FriendsContent(state: FriendsUiState, onSend: (String) -> Unit, onRetry: () -> Unit, onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OnboardingHeader(
            step = 3,
            title = stringResource(R.string.onboarding_friends_title),
            subtitle = stringResource(R.string.onboarding_friends_subtitle)
        )
        FriendsBody(state = state, onSend = onSend, onRetry = onRetry)
        WhereverPrimaryButton(
            text = if (state.isContinuing) {
                stringResource(R.string.auth_action_loading)
            } else {
                stringResource(R.string.onboarding_action_continue)
            },
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_friends_continue"),
            enabled = !state.isContinuing
        )
        TextButton(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_friends_skip"),
            enabled = !state.isContinuing
        ) {
            Text(text = stringResource(R.string.onboarding_action_skip))
        }
    }
}

@Composable
private fun FriendsBody(state: FriendsUiState, onSend: (String) -> Unit, onRetry: () -> Unit) {
    if (state.isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = Mint
        )
    }
    AuthErrorBanner(error = state.error)
    if (state.error != null) {
        TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.action_retry))
        }
    }
    if (!state.isLoading && state.suggestions.isEmpty()) {
        Text(
            text = stringResource(R.string.onboarding_friends_empty),
            style = MaterialTheme.typography.bodyLarge
        )
    }
    state.suggestions.forEach { suggestion ->
        FriendSuggestionCard(
            suggestion = suggestion,
            isSent = suggestion.id in state.sentIds,
            isSending = suggestion.id in state.sendingIds,
            onSend = { onSend(suggestion.id) }
        )
    }
}

@Composable
private fun FriendSuggestionCard(suggestion: FriendSuggestion, isSent: Boolean, isSending: Boolean, onSend: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = suggestion.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                suggestion.detail?.let { detail ->
                    Text(text = detail, style = MaterialTheme.typography.bodyMedium)
                }
                suggestion.mutualFriends?.takeIf { it > 0 }?.let { count ->
                    Text(
                        text = pluralStringResource(R.plurals.onboarding_friends_mutual, count, count),
                        style = MaterialTheme.typography.bodySmall,
                        color = Mint
                    )
                }
            }
            OutlinedButton(
                onClick = onSend,
                enabled = !isSent && !isSending,
                modifier = Modifier.testTag("onboarding_friend_${suggestion.id}")
            ) {
                Text(
                    text = stringResource(
                        when {
                            isSent -> R.string.onboarding_friends_sent
                            isSending -> R.string.auth_action_loading
                            else -> R.string.onboarding_friends_add
                        }
                    )
                )
            }
        }
    }
}
