package com.wherever.mobile.feature.account.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ConfirmDialog
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.ui.SecureScreen

/** Privacidad → Mis datos (US-007, specs/nv-51). */
@Composable
fun MyDataRoute(onBack: () -> Unit, viewModel: MyDataViewModel = hiltViewModel()) {
    SecureScreen()
    val state by viewModel.state.collectAsStateWithLifecycle()
    FormScreen {
        TopTitleRow(stringResource(R.string.my_data_title), onBack)
        state.message?.let {
            InfoBanner(it.asString(), icon = Icons.Outlined.CheckCircle, tag = "my_data_message")
        }
        state.error?.let { ErrorBanner(it.asString()) }
        Section(stringResource(R.string.my_data_export_title), stringResource(R.string.my_data_export_detail))
        WhereverPrimaryButton(
            text = stringResource(R.string.my_data_export_action),
            onClick = viewModel::requestExport,
            enabled = !state.busy,
            loading = state.exporting,
            modifier = Modifier.fillMaxWidth(),
            tag = "my_data_export"
        )
        Section(stringResource(R.string.my_data_history_title), stringResource(R.string.my_data_history_detail))
        WhereverOutlinedButton(
            text = if (state.clearing) {
                stringResource(R.string.my_data_history_clearing)
            } else {
                stringResource(R.string.my_data_history_action)
            },
            onClick = viewModel::askClear,
            enabled = !state.busy,
            contentColor = MaterialTheme.colorScheme.error,
            borderColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth(),
            tag = "my_data_clear"
        )
    }
    if (state.confirmClear) {
        ConfirmDialog(
            title = stringResource(R.string.my_data_history_confirm_title),
            message = stringResource(R.string.my_data_history_confirm_message),
            confirmText = stringResource(R.string.my_data_history_confirm),
            onConfirm = viewModel::confirmClear,
            onDismiss = viewModel::dismissClear
        )
    }
}

@Composable
private fun Section(title: String, detail: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
