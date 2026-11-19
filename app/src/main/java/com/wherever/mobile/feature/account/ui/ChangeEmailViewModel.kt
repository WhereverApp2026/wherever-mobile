package com.wherever.mobile.feature.account.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.isValidEmail
import com.wherever.mobile.core.common.withoutSpaces
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.fieldErrors
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.account.domain.AccountRepository
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val FIELD_EMAIL = "correo_nuevo"
private const val FIELD_PASSWORD = "password"

data class ChangeEmailUiState(
    val currentEmail: String = "",
    val email: String = "",
    val password: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val error: UiText? = null,
    val submitting: Boolean = false,
    /** Correo al que se mandó la verificación; no nulo = terminado. */
    val sentTo: String? = null
) {
    val canSubmit: Boolean
        get() = isValidEmail(email) && !email.equals(currentEmail, ignoreCase = true) &&
            password.isNotEmpty() && !submitting
}

/** Cambiar correo (US-017, specs/nv-51). */
@HiltViewModel
class ChangeEmailViewModel @Inject constructor(
    private val repository: AccountRepository,
    profileRepository: ProfileRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(ChangeEmailUiState())
    val state: StateFlow<ChangeEmailUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.observeProfile().collect { profile ->
                mutableState.update { it.copy(currentEmail = profile?.email.orEmpty()) }
            }
        }
    }

    fun onEmailChange(value: String) = mutableState.update {
        it.copy(email = value.withoutSpaces(), emailError = null, error = null)
    }

    fun onEmailBlur() = mutableState.update {
        val error = when {
            it.email.isBlank() -> null
            !isValidEmail(it.email) -> UiText.Res(R.string.validation_email)
            it.email.equals(it.currentEmail, ignoreCase = true) -> UiText.Res(R.string.account_email_same)
            else -> null
        }
        it.copy(emailError = error)
    }

    fun onPasswordChange(value: String) = mutableState.update {
        it.copy(password = value, passwordError = null, error = null)
    }

    fun submit() {
        val current = state.value
        if (!current.canSubmit) return
        mutableState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.changeEmail(current.email, current.password)) {
                is Result.Success -> mutableState.update { it.copy(submitting = false, sentTo = current.email.trim()) }
                is Result.Failure -> onFailure(result.error)
            }
        }
    }

    private fun onFailure(error: AppError) {
        val fields = error.fieldErrors()
        mutableState.update {
            when {
                error is AppError.Unauthorized ->
                    it.copy(submitting = false, passwordError = UiText.Res(R.string.account_wrong_password))
                error is AppError.Conflict ->
                    it.copy(submitting = false, emailError = UiText.Res(R.string.account_email_taken))
                fields.isNotEmpty() -> it.copy(
                    submitting = false,
                    emailError = fields[FIELD_EMAIL],
                    passwordError = fields[FIELD_PASSWORD],
                    error = if (fields.keys.all { k -> k == FIELD_EMAIL || k == FIELD_PASSWORD }) null else error.toUiText()
                )
                else -> it.copy(submitting = false, error = error.toUiText())
            }
        }
    }
}
