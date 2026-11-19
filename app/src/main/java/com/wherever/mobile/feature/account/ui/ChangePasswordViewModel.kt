package com.wherever.mobile.feature.account.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.isValidPassword
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.fieldErrors
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.account.domain.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val FIELD_CURRENT = "password_actual"
private const val FIELD_NEW = "password_nuevo"

data class ChangePasswordUiState(
    val current: String = "",
    val new: String = "",
    val confirm: String = "",
    val currentError: UiText? = null,
    val newError: UiText? = null,
    val confirmError: UiText? = null,
    val error: UiText? = null,
    val submitting: Boolean = false,
    val done: Boolean = false
) {
    val canSubmit: Boolean
        get() = current.isNotEmpty() && isValidPassword(new) && confirm == new && !submitting
}

/** Cambiar contraseña (US-017, specs/nv-51). */
@HiltViewModel
class ChangePasswordViewModel @Inject constructor(private val repository: AccountRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = mutableState.asStateFlow()

    fun onCurrentChange(value: String) = mutableState.update {
        it.copy(current = value, currentError = null, error = null)
    }

    fun onNewChange(value: String) = mutableState.update {
        it.copy(new = value, newError = null, confirmError = null, error = null)
    }

    fun onConfirmChange(value: String) = mutableState.update {
        it.copy(confirm = value, confirmError = null, error = null)
    }

    fun onNewBlur() = mutableState.update {
        val invalid = it.new.isNotEmpty() && !isValidPassword(it.new)
        it.copy(newError = if (invalid) UiText.Res(R.string.validation_password) else null)
    }

    fun onConfirmBlur() = mutableState.update {
        val mismatch = it.confirm.isNotEmpty() && it.confirm != it.new
        it.copy(confirmError = if (mismatch) UiText.Res(R.string.account_password_mismatch) else null)
    }

    fun submit() {
        val current = state.value
        if (!current.canSubmit) return
        if (current.new == current.current) {
            mutableState.update { it.copy(newError = UiText.Res(R.string.account_password_same)) }
            return
        }
        mutableState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.changePassword(current.current, current.new)) {
                is Result.Success -> mutableState.update { it.copy(submitting = false, done = true) }
                is Result.Failure -> onFailure(result.error)
            }
        }
    }

    private fun onFailure(error: AppError) {
        val fields = error.fieldErrors()
        mutableState.update {
            when {
                // 401 aquí es la contraseña actual incorrecta: el Authenticator ya probó renovar el token.
                error is AppError.Unauthorized ->
                    it.copy(submitting = false, currentError = UiText.Res(R.string.account_wrong_password))
                fields.isNotEmpty() -> it.copy(
                    submitting = false,
                    currentError = fields[FIELD_CURRENT],
                    newError = fields[FIELD_NEW],
                    error = if (fields.keys.all { k -> k == FIELD_CURRENT || k == FIELD_NEW }) null else error.toUiText()
                )
                else -> it.copy(submitting = false, error = error.toUiText())
            }
        }
    }
}
