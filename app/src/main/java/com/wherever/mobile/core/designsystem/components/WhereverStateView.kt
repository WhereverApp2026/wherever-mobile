package com.wherever.mobile.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wherever.mobile.R

@Composable
fun WhereverStateView(state: WhereverContentState, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (state) {
            WhereverContentState.Loading -> {
                CircularProgressIndicator()
                Text(
                    text = stringResource(R.string.state_loading),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            WhereverContentState.Empty -> Text(
                text = stringResource(R.string.state_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            WhereverContentState.Offline -> Text(
                text = stringResource(R.string.state_offline),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            is WhereverContentState.Error -> Text(
                text = state.message ?: stringResource(R.string.state_error),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }

        val canRetry = state is WhereverContentState.Error || state is WhereverContentState.Offline
        if (canRetry && onRetry != null) {
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.action_retry))
            }
        }
    }
}
