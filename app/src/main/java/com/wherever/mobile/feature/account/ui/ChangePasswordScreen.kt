package com.wherever.mobile.feature.account.ui

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

/** Ajustes → Cambiar contraseña (US-017, specs/nv-51). */
@Composable
fun ChangePasswordRoute(onBack: () -> Unit, viewModel: ChangePasswordViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val submit = rememberFormSubmit(viewModel::submit)
    FormScreen(
        footer = {
            if (state.done) {
                WhereverPrimaryButton(stringResource(R.string.common_done), onBack, tag = "change_password_done")
            } else {
                WhereverPrimaryButton(
                    text = stringResource(R.string.account_password_submit),
                    onClick = submit,
                    enabled = state.canSubmit,
                    loading = state.submitting,
                    tag = "change_password_submit"
                )
            }
        }
    ) {
        TopTitleRow(stringResource(R.string.account_password_title), onBack)
        if (state.done) {
            InfoBanner(
                stringResource(R.string.account_password_done),
                icon = Icons.Outlined.CheckCircle,
                tag = "change_password_success"
            )
            return@FormScreen
        }
        PasswordField(
            label = stringResource(R.string.account_password_current),
            value = state.current,
            onValueChange = viewModel::onCurrentChange,
            error = state.currentError?.asString(),
            tag = "change_password_current"
        )
        PasswordField(
            label = stringResource(R.string.account_password_new),
            value = state.new,
            onValueChange = viewModel::onNewChange,
            error = state.newError?.asString(),
            supporting = stringResource(R.string.validation_password),
            onBlur = viewModel::onNewBlur,
            tag = "change_password_new"
        )
        PasswordField(
            label = stringResource(R.string.account_password_confirm),
            value = state.confirm,
            onValueChange = viewModel::onConfirmChange,
            error = state.confirmError?.asString(),
            onBlur = viewModel::onConfirmBlur,
            imeAction = ImeAction.Done,
            onDone = { if (state.canSubmit) submit() },
            tag = "change_password_confirm"
        )
        InfoBanner(stringResource(R.string.account_password_sessions_note))
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

@Composable
internal fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    tag: String,
    supporting: String? = null,
    onBlur: () -> Unit = {},
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {}
) {
    LabeledTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        leadingIcon = Icons.Outlined.Lock,
        error = error,
        supporting = supporting,
        onBlur = onBlur,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        visualTransformation = PasswordVisualTransformation(),
        tag = tag
    )
}
