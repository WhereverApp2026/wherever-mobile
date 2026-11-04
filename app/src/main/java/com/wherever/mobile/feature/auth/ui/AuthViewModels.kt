package com.wherever.mobile.feature.auth.ui

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.BaseViewModel
import com.wherever.mobile.feature.auth.data.AuthRepository
import com.wherever.mobile.feature.auth.domain.AuthField
import com.wherever.mobile.feature.auth.domain.AuthFlowSession
import com.wherever.mobile.feature.auth.domain.AuthValidator
import com.wherever.mobile.feature.auth.domain.RegistrationForm
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : BaseViewModel<LoginUiState, LoginEvent>(LoginUiState()) {

    fun onEmailChange(value: String) {
        updateState {
            it.copy(
                email = value,
                fieldErrors = it.fieldErrors - AuthField.EMAIL,
                error = null
            )
        }
    }

    fun onPasswordChange(value: String) {
        updateState {
            it.copy(
                password = value,
                fieldErrors = it.fieldErrors - AuthField.PASSWORD,
                error = null
            )
        }
    }

    fun togglePasswordVisibility() {
        updateState { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun submit() {
        val current = state.value
        if (current.isLoading) return
        val errors = AuthValidator.login(current.email, current.password)
        if (errors.isNotEmpty()) {
            setState(current.copy(fieldErrors = errors, error = null))
            return
        }
        setState(current.copy(isLoading = true, fieldErrors = emptyMap(), error = null))
        scope.launch {
            when (val result = repository.login(current.email.trim(), current.password)) {
                is Result.Success -> {
                    setState(state.value.copy(isLoading = false))
                    emitEvent(LoginEvent.Success)
                }

                is Result.Failure ->
                    setState(state.value.copy(isLoading = false, error = result.error))
            }
        }
    }
}

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel<RegisterUiState, RegisterEvent>(RegisterUiState()) {

    fun onNameChange(value: String) = updateState { it.copy(name = value, error = null) }

    fun onBirthDateChange(value: String) = updateState { it.copy(birthDate = value, error = null) }

    fun onEmailChange(value: String) = updateState { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) = updateState { it.copy(password = value, error = null) }

    fun onPasswordConfirmationChange(value: String) = updateState { it.copy(passwordConfirmation = value, error = null) }

    fun onTermsChange(value: Boolean) = updateState { it.copy(acceptedTerms = value, error = null) }

    fun togglePasswordVisibility() = updateState { it.copy(passwordVisible = !it.passwordVisible) }

    fun submit() {
        val current = state.value
        if (current.isLoading) return
        val errors = AuthValidator.register(
            RegistrationForm(
                name = current.name,
                birthDate = current.birthDate,
                email = current.email,
                password = current.password,
                passwordConfirmation = current.passwordConfirmation,
                acceptedTerms = current.acceptedTerms
            )
        )
        if (errors.isNotEmpty()) {
            setState(current.copy(fieldErrors = errors, error = null))
            return
        }
        setState(current.copy(isLoading = true, fieldErrors = emptyMap(), error = null))
        scope.launch {
            when (
                val result = repository.register(
                    name = current.name.trim(),
                    birthDate = current.birthDate.trim(),
                    email = current.email.trim(),
                    password = current.password
                )
            ) {
                is Result.Success -> {
                    setState(state.value.copy(isLoading = false))
                    emitEvent(RegisterEvent.Success(result.value))
                }

                is Result.Failure ->
                    setState(state.value.copy(isLoading = false, error = result.error))
            }
        }
    }
}

@HiltViewModel
class RecoverViewModel @Inject constructor(private val repository: AuthRepository, private val flowSession: AuthFlowSession) :
    BaseViewModel<RecoverUiState, RecoverEvent>(RecoverUiState()) {

    fun onEmailChange(value: String) = updateState { it.copy(email = value, error = null) }

    fun submit() {
        val current = state.value
        if (current.isLoading) return
        val errors = AuthValidator.recoveryEmail(current.email)
        if (errors.isNotEmpty()) {
            setState(current.copy(fieldErrors = errors, error = null))
            return
        }
        setState(current.copy(isLoading = true, fieldErrors = emptyMap(), error = null))
        scope.launch {
            when (val result = repository.requestRecovery(current.email.trim())) {
                is Result.Success -> {
                    flowSession.setRecoveryEmail(current.email.trim())
                    setState(state.value.copy(isLoading = false))
                    emitEvent(RecoverEvent.Sent)
                }

                is Result.Failure ->
                    setState(state.value.copy(isLoading = false, error = result.error))
            }
        }
    }
}

@HiltViewModel
class RecoverSentViewModel @Inject constructor(private val repository: AuthRepository, private val flowSession: AuthFlowSession) :
    BaseViewModel<RecoverSentUiState, RecoverEvent>(
        RecoverSentUiState(email = flowSession.recoveryEmail)
    ) {

    fun resend() {
        val current = state.value
        if (current.isLoading || current.email.isBlank()) return
        setState(current.copy(isLoading = true, resendSucceeded = false, error = null))
        scope.launch {
            when (val result = repository.requestRecovery(current.email)) {
                is Result.Success ->
                    setState(state.value.copy(isLoading = false, resendSucceeded = true))

                is Result.Failure ->
                    setState(state.value.copy(isLoading = false, error = result.error))
            }
        }
    }
}

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel<VerifyEmailUiState, VerifyEmailEvent>(VerifyEmailUiState()) {

    fun onTokenChange(value: String) = updateState { it.copy(token = value, error = null) }

    fun submit() {
        val current = state.value
        if (current.isLoading) return
        val errors = AuthValidator.verifyEmail(current.token)
        if (errors.isNotEmpty()) {
            setState(current.copy(fieldErrors = errors, error = null))
            return
        }
        setState(current.copy(isLoading = true, fieldErrors = emptyMap(), error = null))
        scope.launch {
            when (val result = repository.verifyEmail(current.token.trim())) {
                is Result.Success -> {
                    setState(state.value.copy(isLoading = false))
                    emitEvent(VerifyEmailEvent.Verified)
                }

                is Result.Failure ->
                    setState(state.value.copy(isLoading = false, error = result.error))
            }
        }
    }
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel<ResetPasswordUiState, ResetPasswordEvent>(ResetPasswordUiState()) {

    fun onTokenChange(value: String) = updateState { it.copy(token = value, error = null) }

    fun onPasswordChange(value: String) = updateState { it.copy(password = value, error = null) }

    fun onPasswordConfirmationChange(value: String) = updateState { it.copy(passwordConfirmation = value, error = null) }

    fun togglePasswordVisibility() = updateState { it.copy(passwordVisible = !it.passwordVisible) }

    fun submit() {
        val current = state.value
        if (current.isLoading) return
        val errors = AuthValidator.resetPassword(current.token, current.password, current.passwordConfirmation)
        if (errors.isNotEmpty()) {
            setState(current.copy(fieldErrors = errors, error = null))
            return
        }
        setState(current.copy(isLoading = true, fieldErrors = emptyMap(), error = null))
        scope.launch {
            when (val result = repository.resetPassword(current.token.trim(), current.password)) {
                is Result.Success -> {
                    setState(state.value.copy(isLoading = false))
                    emitEvent(ResetPasswordEvent.Reset)
                }

                is Result.Failure ->
                    setState(state.value.copy(isLoading = false, error = result.error))
            }
        }
    }
}

@HiltViewModel
class SessionsViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel<SessionsUiState, SessionsEvent>(SessionsUiState()) {

    init {
        load()
    }

    fun load() {
        setState(SessionsUiState(isLoading = true))
        scope.launch {
            when (val result = repository.sessions()) {
                is Result.Success ->
                    setState(SessionsUiState(isLoading = false, sessions = result.value))

                is Result.Failure -> {
                    if (result.error is com.wherever.mobile.core.network.AppError.Unauthorized) {
                        repository.clearSession()
                        emitEvent(SessionsEvent.SessionExpired)
                    } else {
                        setState(SessionsUiState(isLoading = false, error = result.error))
                    }
                }
            }
        }
    }

    fun logoutAll() {
        if (state.value.isLoggingOut) return
        setState(state.value.copy(isLoggingOut = true))
        scope.launch {
            repository.logoutAll()
            setState(state.value.copy(isLoggingOut = false))
            emitEvent(SessionsEvent.LoggedOut)
        }
    }
}

@HiltViewModel
class AuthGateViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel<AuthGateUiState, Nothing>(AuthGateUiState.Loading) {

    init {
        check()
    }

    fun check() {
        setState(AuthGateUiState.Loading)
        if (!repository.hasSession()) {
            setState(AuthGateUiState.Unauthenticated)
            return
        }
        scope.launch {
            when (val result = repository.sessions()) {
                is Result.Success -> setState(AuthGateUiState.Authenticated)

                is Result.Failure -> {
                    if (result.error is com.wherever.mobile.core.network.AppError.Unauthorized) {
                        repository.clearSession()
                        setState(AuthGateUiState.Unauthenticated)
                    } else {
                        setState(AuthGateUiState.Authenticated)
                    }
                }
            }
        }
    }
}
