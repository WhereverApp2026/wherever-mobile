package com.wherever.mobile.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.isValidEmail
import com.wherever.mobile.core.common.isValidPassword
import com.wherever.mobile.core.ui.DEFAULT_RETRY_SECONDS
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.fieldErrors
import com.wherever.mobile.core.ui.launchCountdown
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.auth.domain.Registration
import com.wherever.mobile.feature.auth.domain.isAdult
import com.wherever.mobile.feature.auth.domain.isValidName
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EmailCheck { UNKNOWN, CHECKING, AVAILABLE, TAKEN }

data class RegisterUiState(
    val name: String = "",
    val birthDate: LocalDate? = null,
    val email: String = "",
    val password: String = "",
    val acceptsTerms: Boolean = false,
    val nameError: UiText? = null,
    val birthDateError: UiText? = null,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val emailCheck: EmailCheck = EmailCheck.UNKNOWN,
    val error: UiText? = null,
    val submitting: Boolean = false,
    val retryAfterSeconds: Long = 0,
    val isFormValid: Boolean = false,
    val maxBirthYear: Int = 0
) {
    val canSubmit: Boolean
        get() = isFormValid &&
            acceptsTerms &&
            !submitting &&
            retryAfterSeconds == 0L &&
            emailCheck != EmailCheck.TAKEN
}

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository: AuthRepository, private val clock: Clock) :
    ViewModel() {
    private val mutableState = MutableStateFlow(RegisterUiState(maxBirthYear = LocalDate.now(clock).year))
    val state: StateFlow<RegisterUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<Unit>(Channel.BUFFERED)
    val registered = eventChannel.receiveAsFlow()

    private var emailCheckJob: Job? = null
    private var cooldown: Job? = null

    /** Los errores se muestran en vivo una vez que el campo tiene algo escrito. */
    fun onNameChange(value: String) = change {
        copy(name = value, nameError = if (value.isNotEmpty() && !isValidName(value)) INVALID_NAME else null)
    }

    fun onBirthDateChange(value: LocalDate) = change {
        copy(birthDate = value, birthDateError = if (isAdult(value, clock)) null else UNDERAGE)
    }

    fun onEmailChange(value: String) {
        emailCheckJob?.cancel()
        change { copy(email = value, emailError = null, emailCheck = EmailCheck.UNKNOWN) }
    }

    fun onPasswordChange(value: String) = change {
        copy(
            password = value,
            passwordError = if (value.isNotEmpty() &&
                !isValidPassword(value)
            ) {
                WEAK_PASSWORD
            } else {
                null
            }
        )
    }

    fun onTermsChange(value: Boolean) = change { copy(acceptsTerms = value) }

    /** `GET /usuarios/correo-disponible` al salir del campo. Si falla, decide el 409 al enviar. */
    fun onEmailBlur() {
        val email = state.value.email
        if (email.isBlank()) return
        if (!isValidEmail(email)) {
            change { copy(emailError = INVALID_EMAIL) }
            return
        }
        emailCheckJob?.cancel()
        change { copy(emailCheck = EmailCheck.CHECKING) }
        emailCheckJob = viewModelScope.launch {
            when (val result = repository.isEmailAvailable(email)) {
                is Result.Success -> change {
                    if (result.data) {
                        copy(emailCheck = EmailCheck.AVAILABLE)
                    } else {
                        copy(emailCheck = EmailCheck.TAKEN, emailError = EMAIL_TAKEN)
                    }
                }
                is Result.Failure -> change {
                    copy(
                        emailCheck = EmailCheck.UNKNOWN,
                        emailError = result.error.fieldErrors()["correo"] ?: emailError
                    )
                }
            }
        }
    }

    fun submit() {
        val current = state.value
        val birthDate = current.birthDate
        if (!current.canSubmit || birthDate == null) return
        change { copy(submitting = true, error = null) }
        viewModelScope.launch {
            val registration = Registration(
                name = current.name,
                birthDate = birthDate.toString(),
                email = current.email,
                password = current.password,
                acceptsTerms = current.acceptsTerms
            )
            when (val result = repository.register(registration)) {
                is Result.Success -> {
                    change { copy(submitting = false, password = "") }
                    eventChannel.send(Unit)
                }
                is Result.Failure -> onFailure(result.error)
            }
        }
    }

    private fun onFailure(error: AppError) {
        val fields = error.fieldErrors()
        change {
            copy(
                submitting = false,
                nameError = fields["nombre"] ?: nameError,
                birthDateError = fields["fecha_nacimiento"] ?: birthDateError,
                emailError = if (error is AppError.Conflict) EMAIL_TAKEN else fields["correo"] ?: emailError,
                emailCheck = if (error is AppError.Conflict) EmailCheck.TAKEN else emailCheck,
                passwordError = fields["password"] ?: passwordError,
                error = error.toUiText().takeIf { fields.isEmpty() && error !is AppError.Conflict }
            )
        }
        if (error is AppError.RateLimited) {
            cooldown?.cancel()
            cooldown = viewModelScope.launchCountdown(error.retryAfterSeconds ?: DEFAULT_RETRY_SECONDS) { left ->
                change { copy(retryAfterSeconds = left) }
            }
        }
    }

    private fun change(transform: RegisterUiState.() -> RegisterUiState) = mutableState.update { previous ->
        val next = previous.transform()
        val birthDate = next.birthDate
        next.copy(
            isFormValid = isValidName(next.name) &&
                birthDate != null &&
                isAdult(birthDate, clock) &&
                isValidEmail(next.email) &&
                isValidPassword(next.password)
        )
    }

    private companion object {
        val INVALID_NAME = UiText.Res(R.string.validation_name)
        val UNDERAGE = UiText.Res(R.string.validation_adult)
        val INVALID_EMAIL = UiText.Res(R.string.validation_email)
        val WEAK_PASSWORD = UiText.Res(R.string.validation_password)
        val EMAIL_TAKEN = UiText.Res(R.string.register_error_email_taken)
    }
}
