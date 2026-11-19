package com.wherever.mobile.feature.auth.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.maskEmail
import com.wherever.mobile.core.ui.DEFAULT_RETRY_SECONDS
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.launchCountdown
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.auth.domain.EmailLink
import com.wherever.mobile.feature.auth.domain.PendingEmailLink
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val RESEND_SECONDS = 60L

/** Qué correo se está esperando. Decide el endpoint de reenvío. */
enum class CheckEmailMode { RECOVERY, VERIFICATION }

data class CheckEmailUiState(
    val mode: CheckEmailMode,
    val maskedEmail: String,
    val secondsLeft: Long = 0,
    val sending: Boolean = false,
    val resent: Boolean = false,
    val error: UiText? = null,
    val pastedLink: String = "",
    val linkError: UiText? = null
) {
    val canResend: Boolean
        get() = secondsLeft == 0L && !sending
}

@HiltViewModel
class CheckEmailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AuthRepository,
    private val pendingLink: PendingEmailLink
) : ViewModel() {
    private val email: String = savedStateHandle["email"] ?: ""
    private val mode = CheckEmailMode.valueOf(savedStateHandle["mode"] ?: CheckEmailMode.RECOVERY.name)

    private val mutableState = MutableStateFlow(CheckEmailUiState(mode, maskEmail(email)))
    val state: StateFlow<CheckEmailUiState> = mutableState.asStateFlow()

    private var timer: Job? = null

    init {
        // Recuperar ya mandó el correo en la pantalla anterior. Verificar llega desde un aviso, sin envío previo.
        if (mode == CheckEmailMode.RECOVERY) startTimer(RESEND_SECONDS)
    }

    private val linkChannel = Channel<EmailLink>(Channel.BUFFERED)

    /** Enlace pegado y válido (nv-08): la pantalla navega a NuevaContrasena o a verificar. */
    val links = linkChannel.receiveAsFlow()

    fun onLinkChange(value: String) = mutableState.update { it.copy(pastedLink = value, linkError = null) }

    fun openPastedLink() {
        val link = EmailLink.parse(state.value.pastedLink)
        if (link == null) {
            mutableState.update { it.copy(linkError = UiText.Res(R.string.check_email_link_invalid)) }
            return
        }
        pendingLink.put(link)
        mutableState.update { it.copy(pastedLink = "") }
        viewModelScope.launch { linkChannel.send(link) }
    }

    fun resend() {
        if (!state.value.canResend) return
        mutableState.update { it.copy(sending = true, resent = false, error = null) }
        viewModelScope.launch {
            val result = when (mode) {
                CheckEmailMode.RECOVERY -> repository.recoverPassword(email)
                CheckEmailMode.VERIFICATION -> repository.resendVerification()
            }
            when (result) {
                is Result.Success -> {
                    mutableState.update { it.copy(sending = false, resent = true) }
                    startTimer(RESEND_SECONDS)
                }
                is Result.Failure -> onFailure(result.error)
            }
        }
    }

    private fun onFailure(error: AppError) {
        mutableState.update { it.copy(sending = false, error = error.toUiText()) }
        if (error is AppError.RateLimited) startTimer(error.retryAfterSeconds ?: DEFAULT_RETRY_SECONDS)
    }

    private fun startTimer(seconds: Long) {
        timer?.cancel()
        timer = viewModelScope.launchCountdown(seconds) { left ->
            mutableState.update { it.copy(secondsLeft = left) }
        }
    }
}
