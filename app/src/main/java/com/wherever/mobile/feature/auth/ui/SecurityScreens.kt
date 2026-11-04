package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
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
fun VerifyEmailScreen(onVerified: () -> Unit, onBack: () -> Unit, viewModel: VerifyEmailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is VerifyEmailEvent.Verified) onVerified()
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
            title = stringResource(R.string.auth_verify_title),
            subtitle = stringResource(R.string.auth_verify_subtitle),
            onBack = onBack
        )
        AuthTextField(
            label = stringResource(R.string.auth_field_token),
            value = state.token,
            onValueChange = viewModel::onTokenChange,
            imeAction = ImeAction.Done,
            error = state.fieldErrors[AuthField.TOKEN],
            testTag = "verify_token"
        )
        AuthErrorBanner(error = state.error)
        WhereverPrimaryButton(
            text = if (state.isLoading) {
                stringResource(R.string.auth_action_loading)
            } else {
                stringResource(R.string.auth_action_verify)
            },
            onClick = viewModel::submit,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("verify_submit"),
            enabled = !state.isLoading
        )
        AuthFooterAction(
            text = stringResource(R.string.auth_verify_later),
            actionText = stringResource(R.string.auth_action_sign_in),
            onClick = onBack
        )
    }
}

@Composable
fun ResetPasswordScreen(onReset: () -> Unit, onBack: () -> Unit, viewModel: ResetPasswordViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is ResetPasswordEvent.Reset) onReset()
        }
    }
    ResetPasswordContent(state = state, onBack = onBack, viewModel = viewModel)
}

@Composable
private fun ResetPasswordContent(state: ResetPasswordUiState, onBack: () -> Unit, viewModel: ResetPasswordViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AuthHeader(
            title = stringResource(R.string.auth_reset_title),
            subtitle = stringResource(R.string.auth_reset_subtitle),
            onBack = onBack
        )
        AuthTextField(
            label = stringResource(R.string.auth_field_token),
            value = state.token,
            onValueChange = viewModel::onTokenChange,
            error = state.fieldErrors[AuthField.TOKEN],
            testTag = "reset_token"
        )
        AuthTextField(
            label = stringResource(R.string.auth_field_new_password),
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            isPassword = true,
            passwordVisible = state.passwordVisible,
            onTogglePassword = viewModel::togglePasswordVisibility,
            error = state.fieldErrors[AuthField.PASSWORD],
            testTag = "reset_password"
        )
        AuthTextField(
            label = stringResource(R.string.auth_field_password_confirmation),
            value = state.passwordConfirmation,
            onValueChange = viewModel::onPasswordConfirmationChange,
            imeAction = ImeAction.Done,
            isPassword = true,
            passwordVisible = state.passwordVisible,
            onTogglePassword = viewModel::togglePasswordVisibility,
            error = state.fieldErrors[AuthField.PASSWORD_CONFIRMATION],
            testTag = "reset_password_confirmation"
        )
        AuthErrorBanner(error = state.error)
        WhereverPrimaryButton(
            text = if (state.isLoading) {
                stringResource(R.string.auth_action_loading)
            } else {
                stringResource(R.string.auth_action_reset)
            },
            onClick = viewModel::submit,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_submit"),
            enabled = !state.isLoading
        )
    }
}
