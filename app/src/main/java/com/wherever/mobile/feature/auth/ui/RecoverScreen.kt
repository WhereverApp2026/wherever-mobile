package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatCountdown

/** 03_Recuperar */
@Composable
fun RecoverRoute(onBack: () -> Unit, onSent: (String) -> Unit, viewModel: RecoverViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.sent.collect(onSent) }
    RecoverContent(state, viewModel::onEmailChange, viewModel::onEmailBlur, viewModel::submit, onBack)
}

@Composable
private fun RecoverContent(
    state: RecoverUiState,
    onEmailChange: (String) -> Unit,
    onEmailBlur: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    FormScreen(
        footer = {
            val waiting = state.retryAfterSeconds > 0
            WhereverPrimaryButton(
                text = if (waiting) {
                    stringResource(R.string.common_wait, formatCountdown(state.retryAfterSeconds))
                } else {
                    stringResource(R.string.recover_submit)
                },
                onClick = onSubmit,
                enabled = state.canSubmit,
                loading = state.submitting,
                tag = "recover_submit"
            )
            WhereverTextLink(stringResource(R.string.recover_back_to_login), onBack)
        }
    ) {
        BackCircleButton(onClick = onBack)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = RouteBlue)
        }
        TitleBlock(stringResource(R.string.recover_title), subtitle = stringResource(R.string.recover_subtitle))
        LabeledTextField(
            label = stringResource(R.string.field_email),
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.field_email_placeholder),
            leadingIcon = Icons.Filled.Email,
            error = state.emailError?.asString(),
            onBlur = onEmailBlur,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            tag = "recover_email"
        )
        InfoBanner(stringResource(R.string.recover_notice), icon = Icons.Filled.Info, tag = "recover_notice")
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

@Preview
@Composable
private fun RecoverPreview() {
    WhereverTheme(darkTheme = false) { RecoverContent(RecoverUiState(), {}, {}, {}, {}) }
}
