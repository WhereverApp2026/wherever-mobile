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
import com.wherever.mobile.feature.auth.domain.PostAuthDestination
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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val error: UiText? = null,
    val submitting: Boolean = false,
    val retryAfterSeconds: Long = 0
) {
    val canSubmit: Boolean
        get() = isValidEmail(email) && password.isNotEmpty() && !submitting && retryAfterSeconds == 0L
}

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<PostAuthDestination>(Channel.BUFFERED)
    val destinations = eventChannel.receiveAsFlow()

    private var cooldown: Job? = null

    fun onEmailChange(value: String) = mutableState.update { it.copy(email = value, emailError = null, error = null) }

    fun onPasswordChange(value: String) =
        mutableState.update { it.copy(password = value, passwordError = null, error = null) }

    fun onEmailBlur() = mutableState.update {
        it.copy(emailError = if (it.email.isBlank() || isValidEmail(it.email)) null else INVALID_EMAIL)
    }

    fun submit() {
        val current = state.value
        if (!current.canSubmit) return
        mutableState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.login(current.email, current.password)) {
                is Result.Success -> {
                    mutableState.update { it.copy(submitting = false, password = "") }
                    eventChannel.send(result.data)
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
                passwordError = fields["password"],
                error = when (error) {
                    is AppError.Locked -> UiText.Res(R.string.login_error_locked)
                    is AppError.Unauthorized -> error.toUiText().takeIf { error.apiMessage != null }
                        ?: UiText.Res(R.string.login_error_credentials)
                    else -> error.toUiText().takeIf { fields.isEmpty() }
                }
            )
        }
        if (error is AppError.RateLimited) startCooldown(error.retryAfterSeconds ?: DEFAULT_RETRY_SECONDS)
    }

    private fun startCooldown(seconds: Long) {
        cooldown?.cancel()
        cooldown = viewModelScope.launchCountdown(seconds) { left ->
            mutableState.update { it.copy(retryAfterSeconds = left) }
        }
    }

    private companion object {
        val INVALID_EMAIL = UiText.Res(R.string.validation_email)
    }
}
