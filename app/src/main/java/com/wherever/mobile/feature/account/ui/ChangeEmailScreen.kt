package com.wherever.mobile.feature.account.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.rememberFormSubmit

/** Ajustes → Cambiar correo (US-017, specs/nv-51). */
@Composable
fun ChangeEmailRoute(onBack: () -> Unit, viewModel: ChangeEmailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val submit = rememberFormSubmit(viewModel::submit)
    val sentTo = state.sentTo
    FormScreen(
        footer = {
            if (sentTo != null) {
                WhereverPrimaryButton(stringResource(R.string.common_done), onBack, tag = "change_email_done")
            } else {
                WhereverPrimaryButton(
                    text = stringResource(R.string.account_email_submit),
                    onClick = submit,
                    enabled = state.canSubmit,
                    loading = state.submitting,
                    tag = "change_email_submit"
                )
            }
        }
    ) {
        TopTitleRow(stringResource(R.string.account_email_title), onBack)
        if (sentTo != null) {
            InfoBanner(
                stringResource(R.string.account_email_done, sentTo),
                icon = Icons.Outlined.CheckCircle,
                tag = "change_email_success"
            )
            return@FormScreen
        }
        if (state.currentEmail.isNotEmpty()) {
            InfoBanner(stringResource(R.string.account_email_current, state.currentEmail))
        }
        LabeledTextField(
            label = stringResource(R.string.account_email_new),
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = stringResource(R.string.field_email_placeholder),
            leadingIcon = Icons.Outlined.Email,
            error = state.emailError?.asString(),
            onBlur = viewModel::onEmailBlur,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            tag = "change_email_new"
        )
        PasswordField(
            label = stringResource(R.string.account_password_current),
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            error = state.passwordError?.asString(),
            supporting = stringResource(R.string.account_email_password_why),
            imeAction = ImeAction.Done,
            onDone = { if (state.canSubmit) submit() },
            tag = "change_email_password"
        )
        state.error?.let { ErrorBanner(it.asString()) }
    }
}
