package com.wherever.mobile.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.health.HealthRepository
import com.wherever.mobile.core.network.AppError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DebugConnectionUiState(
    val isLoading: Boolean = true,
    val healthCode: Int? = null,
    val readyCode: Int? = null,
    val error: AppError? = null
)

@HiltViewModel
class DebugConnectionViewModel @Inject constructor(private val healthRepository: HealthRepository) : ViewModel() {

    private val _state = MutableStateFlow(DebugConnectionUiState())
    val state: StateFlow<DebugConnectionUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = DebugConnectionUiState(isLoading = true)
            val result = healthRepository.check()
            _state.value = DebugConnectionUiState(
                isLoading = false,
                healthCode = result.healthCode,
                readyCode = result.readyCode,
                error = result.error
            )
        }
    }
}
