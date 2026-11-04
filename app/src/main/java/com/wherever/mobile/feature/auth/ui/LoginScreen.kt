package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LogoMark
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatCountdown
import com.wherever.mobile.feature.auth.domain.PostAuthDestination

/** 03_Login */
@Composable
fun LoginRoute(
    onBack: () -> Unit,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit,
    onLoggedIn: (PostAuthDestination) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.destinations.collect(onLoggedIn) }
    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onEmailBlur = viewModel::onEmailBlur,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        onBack = onBack,
        onForgotPassword = onForgotPassword,
        onCreateAccount = onCreateAccount
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onEmailBlur: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit
) {
    FormScreen(
        footer = {
            val waiting = state.retryAfterSeconds > 0
            WhereverPrimaryButton(
                text = if (waiting) {
                    stringResource(R.string.common_wait, formatCountdown(state.retryAfterSeconds))
                } else {
                    stringResource(R.string.login_submit)
                },
                onClick = onSubmit,
                enabled = state.canSubmit,
                loading = state.submitting,
                tag = "login_submit"
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.login_no_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                WhereverTextLink(stringResource(R.string.login_create_account), onCreateAccount)
            }
        }
    ) {
        BackCircleButton(onClick = onBack)
        LogoMark()
        TitleBlock(stringResource(R.string.login_title), subtitle = stringResource(R.string.login_subtitle))
        LabeledTextField(
            label = stringResource(R.string.field_email),
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.field_email_placeholder),
            leadingIcon = Icons.Filled.Email,
            error = state.emailError?.asString(),
            onBlur = onEmailBlur,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            tag = "login_email"
        )
        LabeledTextField(
            label = stringResource(R.string.field_password),
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.login_password_placeholder),
            leadingIcon = Icons.Filled.Lock,
            error = state.passwordError?.asString(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            tag = "login_password"
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            WhereverTextLink(stringResource(R.string.login_forgot), onForgotPassword)
        }
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

@Preview
@Composable
private fun LoginPreview() {
    WhereverTheme(darkTheme = false) {
        LoginContent(LoginUiState(email = "tu@ejemplo.com"), {}, {}, {}, {}, {}, {}, {})
    }
}
