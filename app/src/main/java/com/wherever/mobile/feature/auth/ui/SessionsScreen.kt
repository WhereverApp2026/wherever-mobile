package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.components.WhereverStateView
import com.wherever.mobile.feature.auth.data.ActiveSession
import com.wherever.mobile.feature.auth.ui.components.AuthErrorBanner
import com.wherever.mobile.feature.auth.ui.components.AuthHeader

@Composable
fun SessionsScreen(onSessionExpired: () -> Unit, onLoggedOut: () -> Unit, viewModel: SessionsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SessionsEvent.SessionExpired -> onSessionExpired()
                SessionsEvent.LoggedOut -> onLoggedOut()
            }
        }
    }
    SessionsContent(
        state = state,
        onRetry = viewModel::load,
        onLogoutAll = viewModel::logoutAll
    )
}

@Composable
fun SessionsContent(state: SessionsUiState, onRetry: () -> Unit, onLogoutAll: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AuthHeader(
            title = stringResource(R.string.auth_sessions_title),
            subtitle = stringResource(R.string.auth_sessions_subtitle)
        )
        when {
            state.isLoading -> WhereverStateView(
                state = com.wherever.mobile.core.designsystem.components.WhereverContentState.Loading
            )

            state.error != null -> {
                AuthErrorBanner(error = state.error)
                WhereverStateView(
                    state = com.wherever.mobile.core.designsystem.components.WhereverContentState.Error(),
                    onRetry = onRetry
                )
            }

            state.sessions.isEmpty() -> WhereverStateView(
                state = com.wherever.mobile.core.designsystem.components.WhereverContentState.Empty
            )

            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.sessions, key = { it.id }) { session ->
                    SessionCard(session)
                }
            }
        }
        com.wherever.mobile.core.designsystem.components.WhereverPrimaryButton(
            text = if (state.isLoggingOut) {
                stringResource(R.string.auth_action_loading)
            } else {
                stringResource(R.string.auth_sessions_logout_all)
            },
            onClick = onLogoutAll,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sessions_logout_all"),
            enabled = !state.isLoggingOut
        )
    }
}

@Composable
private fun SessionCard(session: ActiveSession) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_${session.id}"),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = session.deviceName.ifBlank { stringResource(R.string.auth_sessions_unknown_device) },
                style = MaterialTheme.typography.titleLarge
            )
            if (session.isCurrent) {
                Text(
                    text = stringResource(R.string.auth_sessions_current),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            session.lastUsedAt?.let { lastUsed ->
                Text(
                    text = stringResource(R.string.auth_sessions_last_used, lastUsed),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            session.ipAddress?.let { ip ->
                Text(
                    text = stringResource(R.string.auth_sessions_ip, ip),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
