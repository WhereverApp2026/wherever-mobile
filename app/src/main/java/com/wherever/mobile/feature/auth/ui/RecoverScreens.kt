package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.components.WhereverPrimaryButton
import com.wherever.mobile.feature.auth.domain.AuthField
import com.wherever.mobile.feature.auth.ui.components.AuthErrorBanner
import com.wherever.mobile.feature.auth.ui.components.AuthFooterAction
import com.wherever.mobile.feature.auth.ui.components.AuthHeader
import com.wherever.mobile.feature.auth.ui.components.AuthTextField

@Composable
fun RecoverScreen(onSent: () -> Unit, onBack: () -> Unit, onOpenReset: () -> Unit, viewModel: RecoverViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is RecoverEvent.Sent) onSent()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AuthHeader(
            title = stringResource(R.string.auth_recover_title),
            subtitle = stringResource(R.string.auth_recover_subtitle),
            onBack = onBack
        )
        AuthTextField(
            label = stringResource(R.string.auth_field_email),
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            error = state.fieldErrors[AuthField.EMAIL],
            testTag = "recover_email"
        )
        AuthErrorBanner(error = state.error)
        WhereverPrimaryButton(
            text = if (state.isLoading) {
                stringResource(R.string.auth_action_loading)
            } else {
                stringResource(R.string.auth_action_send_recovery)
            },
            onClick = viewModel::submit,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recover_submit"),
            enabled = !state.isLoading
        )
        AuthFooterAction(
            text = stringResource(R.string.auth_has_code),
            actionText = stringResource(R.string.auth_action_reset),
            onClick = onOpenReset
        )
    }
}

@Composable
fun RecoverSentScreen(onOpenReset: () -> Unit, onBackToLogin: () -> Unit, viewModel: RecoverSentViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AuthHeader(
            title = stringResource(R.string.auth_recover_sent_title),
            subtitle = stringResource(R.string.auth_recover_sent_subtitle, state.email),
            onBack = onBackToLogin
        )
        if (state.resendSucceeded) {
            Text(
                text = stringResource(R.string.auth_recover_resent),
                modifier = Modifier.testTag("recover_resent_message")
            )
        }
        AuthErrorBanner(error = state.error)
        WhereverPrimaryButton(
            text = if (state.isLoading) {
                stringResource(R.string.auth_action_loading)
            } else {
                stringResource(R.string.auth_action_resend)
            },
            onClick = viewModel::resend,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recover_resend"),
            enabled = !state.isLoading && state.email.isNotBlank()
        )
        WhereverPrimaryButton(
            text = stringResource(R.string.auth_action_have_code),
            onClick = onOpenReset,
            modifier = Modifier.fillMaxWidth()
        )
        AuthFooterAction(
            text = stringResource(R.string.auth_remembered),
            actionText = stringResource(R.string.auth_action_sign_in),
            onClick = onBackToLogin
        )
    }
}
