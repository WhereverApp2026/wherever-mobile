package com.wherever.mobile.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.BuildConfig
import com.wherever.mobile.R
import com.wherever.mobile.core.network.AppError

@Composable
fun DebugConnectionScreen(viewModel: DebugConnectionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DebugConnectionContent(
        state = state,
        apiBaseUrl = BuildConfig.API_BASE_URL,
        onRefresh = viewModel::refresh
    )
}

@Composable
fun DebugConnectionContent(state: DebugConnectionUiState, apiBaseUrl: String, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.debug_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(R.string.debug_api_base, apiBaseUrl),
            style = MaterialTheme.typography.bodyMedium
        )

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            StatusRow(label = stringResource(R.string.debug_health), code = state.healthCode)
            StatusRow(label = stringResource(R.string.debug_ready), code = state.readyCode)
            state.error?.let { error ->
                Text(
                    text = stringResource(error.messageResource()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            OutlinedButton(onClick = onRefresh) {
                Text(text = stringResource(R.string.debug_refresh))
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, code: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        val status = if (code != null && code in 200..299) {
            "$code ${stringResource(R.string.debug_status_ok)}"
        } else if (code != null) {
            "$code ${stringResource(R.string.debug_status_failure)}"
        } else {
            stringResource(R.string.debug_status_failure)
        }
        Text(
            text = status,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun AppError.messageResource(): Int = when (this) {
    AppError.Network -> R.string.error_network
    AppError.Timeout -> R.string.error_timeout
    AppError.Unauthorized -> R.string.error_unauthorized
    is AppError.Validation -> R.string.error_validation
    is AppError.RateLimited -> R.string.auth_error_rate_limited
    AppError.Server -> R.string.error_server
    is AppError.Unknown -> R.string.error_unknown
}
