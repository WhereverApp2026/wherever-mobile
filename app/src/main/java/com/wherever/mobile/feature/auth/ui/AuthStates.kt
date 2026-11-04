package com.wherever.mobile.feature.auth.ui

import com.wherever.mobile.core.network.AppError
import com.wherever.mobile.feature.auth.data.ActiveSession
import com.wherever.mobile.feature.auth.domain.AuthField
import com.wherever.mobile.feature.auth.domain.FieldError

typealias FieldErrors = Map<AuthField, FieldError>

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val fieldErrors: FieldErrors = emptyMap(),
    val error: AppError? = null
)

data class RegisterUiState(
    val name: String = "",
    val birthDate: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val acceptedTerms: Boolean = false,
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val fieldErrors: FieldErrors = emptyMap(),
    val error: AppError? = null
)

data class RecoverUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val fieldErrors: FieldErrors = emptyMap(),
    val error: AppError? = null
)

data class RecoverSentUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val resendSucceeded: Boolean = false,
    val error: AppError? = null
)

data class VerifyEmailUiState(
    val token: String = "",
    val isLoading: Boolean = false,
    val fieldErrors: FieldErrors = emptyMap(),
    val error: AppError? = null
)

data class ResetPasswordUiState(
    val token: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val fieldErrors: FieldErrors = emptyMap(),
    val error: AppError? = null
)

data class SessionsUiState(
    val isLoading: Boolean = true,
    val sessions: List<ActiveSession> = emptyList(),
    val error: AppError? = null,
    val isLoggingOut: Boolean = false
)

sealed interface AuthGateUiState {
    data object Loading : AuthGateUiState
    data object Unauthenticated : AuthGateUiState
    data object Authenticated : AuthGateUiState
}

sealed interface LoginEvent {
    data object Success : LoginEvent
}

sealed interface RegisterEvent {
    data class Success(val outcome: com.wherever.mobile.feature.auth.data.RegistrationOutcome) : RegisterEvent
}

sealed interface RecoverEvent {
    data object Sent : RecoverEvent
}

sealed interface VerifyEmailEvent {
    data object Verified : VerifyEmailEvent
}

sealed interface ResetPasswordEvent {
    data object Reset : ResetPasswordEvent
}

sealed interface SessionsEvent {
    data object SessionExpired : SessionsEvent
    data object LoggedOut : SessionsEvent
}
