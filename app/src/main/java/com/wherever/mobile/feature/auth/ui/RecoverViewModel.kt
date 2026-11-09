package com.wherever.mobile.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.isValidEmail
import com.wherever.mobile.core.ui.DEFAULT_RETRY_SECONDS
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.fieldErrors
import com.wherever.mobile.core.ui.launchCountdown
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.AuthRepository
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

data class RecoverUiState(
    val email: String = "",
    val emailError: UiText? = null,
    val error: UiText? = null,
    val submitting: Boolean = false,
    val retryAfterSeconds: Long = 0
) {
    val canSubmit: Boolean
        get() = isValidEmail(email) && !submitting && retryAfterSeconds == 0L
}

@HiltViewModel
class RecoverViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(RecoverUiState())
    val state: StateFlow<RecoverUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<String>(Channel.BUFFERED)

    /** Emite el correo al que se mandó el enlace. */
    val sent = eventChannel.receiveAsFlow()

    private var cooldown: Job? = null

    fun onEmailChange(value: String) = mutableState.update { it.copy(email = value, emailError = null, error = null) }

    fun onEmailBlur() = mutableState.update {
        val invalid = it.email.isNotBlank() && !isValidEmail(it.email)
        it.copy(emailError = if (invalid) UiText.Res(R.string.validation_email) else null)
    }

    fun submit() {
        val current = state.value
        if (!current.canSubmit) return
        mutableState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.recoverPassword(current.email)) {
                is Result.Success -> {
                    mutableState.update { it.copy(submitting = false) }
                    eventChannel.send(current.email.trim())
                }
                is Result.Failure -> onFailure(result.error)
            }
        }
    }

    private fun onFailure(error: AppError) {
        val fields = error.fieldErrors()
        mutableState.update {
            it.copy(
                submitting = false,
                emailError = fields["correo"],
                error = error.toUiText().takeIf { fields.isEmpty() }
            )
        }
        if (error is AppError.RateLimited) {
            cooldown?.cancel()
            cooldown = viewModelScope.launchCountdown(error.retryAfterSeconds ?: DEFAULT_RETRY_SECONDS) { left ->
                mutableState.update { it.copy(retryAfterSeconds = left) }
            }
        }
    }
}
