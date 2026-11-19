package com.wherever.mobile.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.isValidPassword
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.fieldErrors
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.AuthRepository
import com.wherever.mobile.feature.auth.domain.PendingEmailLink
import com.wherever.mobile.feature.auth.domain.PostAuthDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val FIELD_NEW = "password_nuevo"
private const val FIELD_TOKEN = "token"

data class NewPasswordUiState(
    val password: String = "",
    val confirm: String = "",
    val passwordError: UiText? = null,
    val confirmError: UiText? = null,
    val error: UiText? = null,
    /** El enlace venció, ya se usó o no es válido: hay que pedir otro. */
    val linkExpired: Boolean = false,
    val submitting: Boolean = false
) {
    val canSubmit: Boolean
        get() = isValidPassword(password) && confirm == password && !submitting && !linkExpired
}

/** NuevaContrasena (nv-08): `POST /auth/restablecer` con el token del enlace; entra con la sesión nueva. */
@HiltViewModel
class NewPasswordViewModel @Inject constructor(
    private val pendingLink: PendingEmailLink,
    private val repository: AuthRepository
) : ViewModel() {
    private val token: String = pendingLink.resetToken().orEmpty()
    private val mutableState = MutableStateFlow(NewPasswordUiState(linkExpired = token.isBlank()))
    val state: StateFlow<NewPasswordUiState> = mutableState.asStateFlow()

    private val doneChannel = Channel<PostAuthDestination>(Channel.BUFFERED)
    val done = doneChannel.receiveAsFlow()

    fun onPasswordChange(value: String) = mutableState.update {
        it.copy(password = value, passwordError = null, confirmError = null, error = null)
    }

    fun onConfirmChange(value: String) = mutableState.update {
        it.copy(confirm = value, confirmError = null, error = null)
    }

    fun onPasswordBlur() = mutableState.update {
        val invalid = it.password.isNotEmpty() && !isValidPassword(it.password)
        it.copy(passwordError = if (invalid) UiText.Res(R.string.validation_password) else null)
    }

    fun onConfirmBlur() = mutableState.update {
        val mismatch = it.confirm.isNotEmpty() && it.confirm != it.password
        it.copy(confirmError = if (mismatch) UiText.Res(R.string.account_password_mismatch) else null)
    }

    fun submit() {
        val current = state.value
        if (!current.canSubmit) return
        mutableState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.resetPassword(token, current.password)) {
                is Result.Success -> {
                    pendingLink.clear()
                    mutableState.update { it.copy(submitting = false) }
                    doneChannel.send(result.data)
                }
                is Result.Failure -> onFailure(result.error)
            }
        }
    }

    private fun onFailure(error: AppError) {
        val fields = error.fieldErrors()
        mutableState.update {
            when {
                fields[FIELD_NEW] != null -> it.copy(submitting = false, passwordError = fields[FIELD_NEW])
                // 400/422 sin error de la contraseña: el token venció, ya se usó o está mal.
                fields.containsKey(FIELD_TOKEN) || error.isBadLink() ->
                    it.copy(submitting = false, linkExpired = true)
                else -> it.copy(submitting = false, error = error.toUiText())
            }
        }
    }
}

/** 400 (sin código conocido), 410 y 422 sin campo: el enlace de un solo uso ya no sirve. */
internal fun AppError.isBadLink(): Boolean =
    this is AppError.Validation || this is AppError.Gone || this is AppError.Unknown
