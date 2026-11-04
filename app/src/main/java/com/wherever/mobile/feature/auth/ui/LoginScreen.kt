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
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onBack: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    onResetPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is LoginEvent.Success) onLoggedIn()
        }
    }
    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePassword = viewModel::togglePasswordVisibility,
        onSubmit = viewModel::submit,
        onBack = onBack,
        onCreateAccount = onCreateAccount,
        onForgotPassword = onForgotPassword,
        onResetPassword = onResetPassword
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    onResetPassword: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AuthHeader(
            title = stringResource(R.string.auth_login_title),
            subtitle = stringResource(R.string.auth_login_subtitle),
            onBack = onBack
        )
        LoginForm(
            state = state,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange,
            onTogglePassword = onTogglePassword,
            onSubmit = onSubmit
        )
        AuthFooterAction(
            text = stringResource(R.string.auth_no_account),
            actionText = stringResource(R.string.auth_action_create_account),
            onClick = onCreateAccount
        )
        AuthFooterAction(
            text = stringResource(R.string.auth_forgot_password),
            actionText = stringResource(R.string.auth_action_recover),
            onClick = onForgotPassword
        )
        AuthFooterAction(
            text = stringResource(R.string.auth_has_code),
            actionText = stringResource(R.string.auth_action_reset),
            onClick = onResetPassword
        )
    }
}

@Composable
private fun LoginForm(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onSubmit: () -> Unit
) {
    AuthTextField(
        label = stringResource(R.string.auth_field_email),
        value = state.email,
        onValueChange = onEmailChange,
        keyboardType = KeyboardType.Email,
        error = state.fieldErrors[AuthField.EMAIL],
        testTag = "login_email"
    )
    AuthTextField(
        label = stringResource(R.string.auth_field_password),
        value = state.password,
        onValueChange = onPasswordChange,
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done,
        isPassword = true,
        passwordVisible = state.passwordVisible,
        onTogglePassword = onTogglePassword,
        error = state.fieldErrors[AuthField.PASSWORD],
        testTag = "login_password"
    )
    AuthErrorBanner(error = state.error)
    WhereverPrimaryButton(
        text = if (state.isLoading) {
            stringResource(R.string.auth_action_loading)
        } else {
            stringResource(R.string.auth_action_sign_in)
        },
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("login_submit"),
        enabled = !state.isLoading
    )
}
