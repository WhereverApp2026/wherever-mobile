package com.wherever.mobile.feature.auth.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.auth.domain.PendingEmailLink
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class VerifyEmailPhase { VERIFYING, VERIFIED, LINK_EXPIRED, ERROR }

data class VerifyEmailUiState(val phase: VerifyEmailPhase = VerifyEmailPhase.VERIFYING, val error: UiText? = null)

/** Verificar correo desde el enlace (nv-08, D11): `POST /auth/verificar-correo {token}`. */
@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val pendingLink: PendingEmailLink,
    private val repository: AuthRepository
) : ViewModel() {
    private val token: String = pendingLink.verifyToken().orEmpty()
    private val mutableState = MutableStateFlow(VerifyEmailUiState())
    val state: StateFlow<VerifyEmailUiState> = mutableState.asStateFlow()

    init {
        verify()
    }

    fun verify() {
        if (token.isBlank()) {
            mutableState.value = VerifyEmailUiState(VerifyEmailPhase.LINK_EXPIRED)
            return
        }
        mutableState.value = VerifyEmailUiState()
        viewModelScope.launch {
            mutableState.value = when (val result = repository.verifyEmail(token)) {
                is Result.Success -> {
                    pendingLink.clear()
                    VerifyEmailUiState(VerifyEmailPhase.VERIFIED)
                }
                is Result.Failure -> if (result.error.isBadLink()) {
                    VerifyEmailUiState(VerifyEmailPhase.LINK_EXPIRED)
                } else {
                    VerifyEmailUiState(VerifyEmailPhase.ERROR, result.error.toUiText())
                }
            }
        }
    }
}

@Composable
fun VerifyEmailRoute(onContinue: () -> Unit, viewModel: VerifyEmailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    VerifyEmailContent(state, onContinue = onContinue, onRetry = viewModel::verify)
}

@Composable
internal fun VerifyEmailContent(state: VerifyEmailUiState, onContinue: () -> Unit, onRetry: () -> Unit) {
    FormScreen(
        footer = {
            when (state.phase) {
                VerifyEmailPhase.ERROR -> WhereverPrimaryButton(
                    stringResource(R.string.common_retry),
                    onRetry,
                    tag = "verify_email_retry"
                )
                VerifyEmailPhase.VERIFYING -> Unit
                else -> WhereverPrimaryButton(
                    stringResource(R.string.common_continue),
                    onContinue,
                    tag = "verify_email_continue"
                )
            }
        }
    ) {
        TitleBlock(stringResource(R.string.verify_email_title))
        when (state.phase) {
            VerifyEmailPhase.VERIFYING -> LoadingState()
            VerifyEmailPhase.VERIFIED -> InfoBanner(
                stringResource(R.string.verify_email_done),
                icon = Icons.Outlined.CheckCircle,
                tag = "verify_email_done"
            )
            VerifyEmailPhase.LINK_EXPIRED -> InfoBanner(
                stringResource(R.string.verify_email_expired),
                icon = Icons.Outlined.Info,
                tag = "verify_email_expired"
            )
            VerifyEmailPhase.ERROR -> state.error?.let { ErrorBanner(it.asString()) }
        }
    }
}
