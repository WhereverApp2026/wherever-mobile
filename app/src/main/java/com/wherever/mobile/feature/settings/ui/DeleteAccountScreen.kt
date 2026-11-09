package com.wherever.mobile.feature.settings.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.WhereverDangerButton
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton

/** EliminarCuenta. Sigue a la API: se escribe ELIMINAR y hay 30 días para cancelar (duda D03). */
@Composable
fun DeleteAccountRoute(onBack: () -> Unit, onDeleted: () -> Unit, viewModel: DeleteAccountViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.deleted.collect { onDeleted() } }
    FormScreen(
        footer = {
            WhereverOutlinedButton(
                text = stringResource(R.string.delete_account_keep),
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
            WhereverDangerButton(
                text = stringResource(R.string.delete_account_action),
                onClick = viewModel::delete,
                enabled = state.canDelete,
                loading = state.deleting,
                tag = "delete_account_submit"
            )
        }
    ) {
        BackCircleButton(onClick = onBack)
        TitleBlock(
            stringResource(R.string.delete_account_title),
            subtitle = stringResource(R.string.delete_account_subtitle)
        )
        SurfaceCard {
            listOf(
                R.string.delete_account_point_grace,
                R.string.delete_account_point_sessions,
                R.string.delete_account_point_cancel
            ).forEach { point ->
                Text(
                    text = stringResource(point),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
        LabeledTextField(
            label = stringResource(R.string.delete_account_confirm_label, DELETE_CONFIRMATION),
            value = state.confirmation,
            onValueChange = viewModel::onConfirmationChange,
            placeholder = DELETE_CONFIRMATION,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            tag = "delete_account_confirmation"
        )
        state.error?.let { ErrorBanner(it.asString()) }
    }
}
