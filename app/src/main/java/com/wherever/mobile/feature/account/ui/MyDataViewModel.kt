package com.wherever.mobile.feature.account.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.account.domain.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyDataUiState(
    val exporting: Boolean = false,
    val clearing: Boolean = false,
    val confirmClear: Boolean = false,
    val message: UiText? = null,
    val error: UiText? = null
) {
    val busy: Boolean
        get() = exporting || clearing
}

/** Mis datos: descargar una copia y borrar el historial (US-007, specs/nv-51). */
@HiltViewModel
class MyDataViewModel @Inject constructor(private val repository: AccountRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(MyDataUiState())
    val state: StateFlow<MyDataUiState> = mutableState.asStateFlow()

    fun requestExport() {
        if (state.value.busy) return
        mutableState.update { it.copy(exporting = true, message = null, error = null) }
        viewModelScope.launch {
            val result = repository.requestDataExport()
            mutableState.update {
                when {
                    result is Result.Success ->
                        it.copy(exporting = false, message = UiText.Res(R.string.my_data_export_sent))
                    // Una exportación por día: el 429 no es una falla, es el límite de la API.
                    (result as Result.Failure).error is AppError.RateLimited ->
                        it.copy(exporting = false, message = UiText.Res(R.string.my_data_export_once_a_day))
                    else -> it.copy(exporting = false, error = result.error.toUiText())
                }
            }
        }
    }

    fun askClear() = mutableState.update { it.copy(confirmClear = true, message = null, error = null) }

    fun dismissClear() = mutableState.update { it.copy(confirmClear = false) }

    fun confirmClear() {
        if (state.value.busy) return
        mutableState.update { it.copy(confirmClear = false, clearing = true) }
        viewModelScope.launch {
            val result = repository.clearHistory()
            mutableState.update {
                when (result) {
                    is Result.Success ->
                        it.copy(clearing = false, message = UiText.Res(R.string.my_data_history_cleared))
                    is Result.Failure -> it.copy(clearing = false, error = result.error.toUiText())
                }
            }
        }
    }
}
