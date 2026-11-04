package com.wherever.mobile.feature.auth.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.components.WhereverPrimaryButton

@Composable
fun LocationScreen(onContinue: () -> Unit, viewModel: LocationViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.continueFlow() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { onContinue() }
    }
    LocationContent(
        state = state,
        onAllow = {
            if (hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                viewModel.continueFlow()
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        },
        onSkip = viewModel::continueFlow
    )
}

@Composable
private fun LocationContent(state: LocationUiState, onAllow: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OnboardingHeader(
            step = 2,
            title = stringResource(R.string.onboarding_location_title),
            subtitle = stringResource(R.string.onboarding_location_subtitle)
        )
        LocationReason(stringResource(R.string.onboarding_location_reason_nearby))
        LocationReason(stringResource(R.string.onboarding_location_reason_private))
        LocationReason(stringResource(R.string.onboarding_location_reason_optional))
        if (state.isSaving) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Mint
                )
                Text(
                    text = stringResource(R.string.auth_action_loading),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        WhereverPrimaryButton(
            text = stringResource(R.string.onboarding_action_allow_location),
            onClick = onAllow,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_location_allow"),
            enabled = !state.isSaving
        )
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_location_skip"),
            enabled = !state.isSaving
        ) {
            Text(text = stringResource(R.string.onboarding_action_not_now))
        }
    }
}

@Composable
private fun LocationReason(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.onboarding_bullet),
            color = Mint,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

fun hasPermission(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
