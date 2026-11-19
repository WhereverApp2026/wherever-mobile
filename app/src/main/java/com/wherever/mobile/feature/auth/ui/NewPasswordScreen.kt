package com.wherever.mobile.feature.auth.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.rememberFormSubmit
import com.wherever.mobile.feature.account.ui.PasswordField
import com.wherever.mobile.feature.auth.domain.PostAuthDestination

/** NuevaContrasena (nv-08): se llega desde el enlace del correo o pegándolo en Revisa tu correo. */
@Composable
fun NewPasswordRoute(
    onDone: (PostAuthDestination) -> Unit,
    onRequestAnother: () -> Unit,
    viewModel: NewPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.done.collect(onDone) }
    NewPasswordContent(
        state = state,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmChange = viewModel::onConfirmChange,
        onPasswordBlur = viewModel::onPasswordBlur,
        onConfirmBlur = viewModel::onConfirmBlur,
        onSubmit = viewModel::submit,
        onRequestAnother = onRequestAnother
    )
}

@Composable
internal fun NewPasswordContent(
    state: NewPasswordUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onPasswordBlur: () -> Unit,
    onConfirmBlur: () -> Unit,
    onSubmit: () -> Unit,
    onRequestAnother: () -> Unit
) {
    val submit = rememberFormSubmit(onSubmit)
    FormScreen(
        footer = {
            if (state.linkExpired) {
                WhereverPrimaryButton(
                    text = stringResource(R.string.new_password_request_another),
                    onClick = onRequestAnother,
                    tag = "new_password_request_another"
                )
            } else {
                WhereverPrimaryButton(
                    text = stringResource(R.string.new_password_submit),
                    onClick = submit,
                    enabled = state.canSubmit,
                    loading = state.submitting,
                    tag = "new_password_submit"
                )
            }
        }
    ) {
        TitleBlock(
            title = stringResource(R.string.new_password_title),
            subtitle = stringResource(R.string.new_password_subtitle)
        )
        if (state.linkExpired) {
            InfoBanner(
                stringResource(R.string.new_password_link_expired),
                icon = Icons.Filled.Info,
                tag = "new_password_expired"
            )
            return@FormScreen
        }
        PasswordField(
            label = stringResource(R.string.account_password_new),
            value = state.password,
            onValueChange = onPasswordChange,
            error = state.passwordError?.asString(),
            supporting = stringResource(R.string.validation_password),
            onBlur = onPasswordBlur,
            tag = "new_password_field"
        )
        PasswordField(
            label = stringResource(R.string.account_password_confirm),
            value = state.confirm,
            onValueChange = onConfirmChange,
            error = state.confirmError?.asString(),
            onBlur = onConfirmBlur,
            imeAction = ImeAction.Done,
            onDone = { if (state.canSubmit) submit() },
            tag = "new_password_confirm"
        )
        InfoBanner(stringResource(R.string.new_password_sessions_note))
        state.error?.let { ErrorBanner(it.asString()) }
    }
}
