package com.wherever.mobile.feature.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ConfirmDialog
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.PhaseCrossfade
import com.wherever.mobile.core.designsystem.ScreenPhase
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverDangerButton
import com.wherever.mobile.core.designsystem.WhereverTextLink

/** Sin frame en la maqueta (duda D07). */
@Composable
fun SessionsRoute(onBack: () -> Unit, onAllClosed: () -> Unit, viewModel: SessionsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmAll by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel) { viewModel.allClosed.collect { onAllClosed() } }
    val phase = when {
        state.loading -> ScreenPhase.LOADING
        state.loadFailed -> ScreenPhase.ERROR
        else -> ScreenPhase.CONTENT
    }
    PhaseCrossfade(phase) { shown ->
        when (shown) {
            ScreenPhase.LOADING -> LoadingState()
            ScreenPhase.ERROR -> ErrorState(state.error?.asString().orEmpty(), viewModel::load)
            ScreenPhase.CONTENT -> SessionsContent(state, onBack, onCloseAll = { confirmAll = true }, viewModel::close)
        }
    }
    if (confirmAll) {
        ConfirmDialog(
            title = stringResource(R.string.sessions_close_all_title),
            message = stringResource(R.string.sessions_close_all_message),
            confirmText = stringResource(R.string.sessions_close_all),
            onConfirm = {
                confirmAll = false
                viewModel.closeAll()
            },
            onDismiss = { confirmAll = false }
        )
    }
}

@Composable
private fun SessionsContent(
    state: SessionsUiState,
    onBack: () -> Unit,
    onCloseAll: () -> Unit,
    onClose: (String) -> Unit
) {
    FormScreen(
        footer = {
            WhereverDangerButton(
                text = stringResource(R.string.sessions_close_all),
                onClick = onCloseAll,
                loading = state.closingAll,
                tag = "sessions_close_all"
            )
        }
    ) {
        TopTitleRow(stringResource(R.string.settings_sessions), onBack)
        state.error?.let { ErrorBanner(it.asString()) }
        state.sessions.forEach { session ->
            SessionCard(session, closing = state.closingId == session.id, onClose = { onClose(session.id) })
        }
    }
}

@Composable
private fun SessionCard(session: SessionItem, closing: Boolean, onClose: () -> Unit) {
    SurfaceCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = session.device, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = stringResource(R.string.sessions_last_used, session.lastUsed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            when {
                // La sesión actual se cierra con "Cerrar sesión" en Ajustes, que además limpia el teléfono.
                session.isCurrent -> StatusPill(stringResource(R.string.sessions_current))
                closing -> CircularProgressIndicator()
                else -> WhereverTextLink(stringResource(R.string.sessions_close), onClose)
            }
        }
    }
}
