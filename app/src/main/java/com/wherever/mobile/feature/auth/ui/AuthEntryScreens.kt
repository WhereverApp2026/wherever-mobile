package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.components.WhereverPrimaryButton

@Composable
fun AuthGateScreen(state: AuthGateUiState, onUnauthenticated: () -> Unit, onAuthenticated: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(state) {
        when (state) {
            AuthGateUiState.Unauthenticated -> onUnauthenticated()
            AuthGateUiState.Authenticated -> onAuthenticated()
            AuthGateUiState.Loading -> Unit
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(RouteBlue, Ink))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.auth_gate_loading),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
fun WelcomeScreen(onCreateAccount: () -> Unit, onSignIn: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(RouteBlue, Ink)))
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.auth_welcome_title),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.auth_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WhereverPrimaryButton(
                text = stringResource(R.string.auth_action_create_account),
                onClick = onCreateAccount,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.auth_action_sign_in))
            }
        }
    }
}
