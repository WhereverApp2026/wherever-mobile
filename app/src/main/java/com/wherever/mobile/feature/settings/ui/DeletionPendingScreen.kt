package com.wherever.mobile.feature.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink

/** Sin frame en la maqueta (duda D13). */
@Composable
fun DeletionPendingRoute(
    onCancelled: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: DeletionPendingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                DeletionPendingEvent.CANCELLED -> onCancelled()
                DeletionPendingEvent.LOGGED_OUT -> onLoggedOut()
            }
        }
    }
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.deletion_pending_cancel),
                onClick = viewModel::cancelDeletion,
                loading = state.working,
                tag = "deletion_pending_cancel"
            )
            WhereverTextLink(stringResource(R.string.settings_logout), viewModel::logout, enabled = !state.working)
        }
    ) {
        TitleBlock(
            stringResource(R.string.deletion_pending_title),
            subtitle = stringResource(R.string.deletion_pending_body, state.deleteDate)
        )
        state.error?.let { ErrorBanner(it.asString()) }
    }
}
