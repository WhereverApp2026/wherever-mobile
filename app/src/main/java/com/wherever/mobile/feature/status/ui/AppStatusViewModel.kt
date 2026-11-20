package com.wherever.mobile.feature.status.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.di.AppVersion
import com.wherever.mobile.core.network.ConnectivityMonitor
import com.wherever.mobile.feature.status.domain.AppStatusRepository
import com.wherever.mobile.feature.status.domain.isOlder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Qué explica la pantalla de estado (89_SinConexion y sus variantes sin frame, specs/nv-16). */
enum class StatusNotice { STILL_OFFLINE, SERVER_DOWN }

data class AppStatusUiState(
    /** Se muestra 89_SinConexion sobre la app. */
    val offline: Boolean = false,
    /** El servidor avisa mantenimiento (`mantenimiento: true`): misma pantalla, otro texto. */
    val maintenance: Boolean = false,
    val checking: Boolean = false,
    val notice: StatusNotice? = null,
    /** `version_minima_app` es mayor que la instalada: hay que actualizar. */
    val updateRequired: Boolean = false
) {
    val blocking: Boolean get() = offline || maintenance
}

/** Cambiar de wifi a datos pierde la red un instante: solo se avisa si dura. */
const val OFFLINE_DEBOUNCE_MS = 1_500L

@OptIn(FlowPreview::class)
@HiltViewModel
class AppStatusViewModel @Inject constructor(
    private val monitor: ConnectivityMonitor,
    private val repository: AppStatusRepository,
    @AppVersion private val version: String
) : ViewModel() {
    private val mutableState = MutableStateFlow(AppStatusUiState())
    val state: StateFlow<AppStatusUiState> = mutableState.asStateFlow()

    /** "Atrás" cierra el aviso hasta que la red vuelva y se vuelva a perder. */
    private var dismissed = false
    private var checkedVersion = false

    init {
        viewModelScope.launch {
            monitor.online.debounce { online -> if (online) 0L else OFFLINE_DEBOUNCE_MS }.collect { online ->
                if (online) {
                    dismissed = false
                    mutableState.update { it.copy(offline = false, notice = null) }
                    if (!checkedVersion && !state.value.checking) check(fromRetry = false)
                } else if (!dismissed) {
                    mutableState.update { it.copy(offline = true, notice = null) }
                }
            }
        }
    }

    fun retry() {
        if (state.value.checking) return
        if (!monitor.isOnline()) {
            mutableState.update { it.copy(notice = StatusNotice.STILL_OFFLINE) }
            return
        }
        check(fromRetry = true)
    }

    fun dismiss() {
        dismissed = true
        mutableState.update { it.copy(offline = false, notice = null) }
    }

    private fun check(fromRetry: Boolean) {
        mutableState.update { it.copy(checking = true, notice = null) }
        viewModelScope.launch {
            when (val result = repository.status()) {
                is Result.Success -> {
                    checkedVersion = true
                    val status = result.data
                    mutableState.update {
                        it.copy(
                            checking = false,
                            offline = false,
                            maintenance = status.maintenance,
                            updateRequired = isOlder(version, status.minimumVersion)
                        )
                    }
                }
                is Result.Failure -> mutableState.update {
                    // Al arrancar, un fallo no bloquea: cada pantalla ya muestra su propio error.
                    if (!fromRetry) {
                        it.copy(checking = false)
                    } else {
                        val notice = if (result.error == AppError.Network) {
                            StatusNotice.STILL_OFFLINE
                        } else {
                            StatusNotice.SERVER_DOWN
                        }
                        it.copy(checking = false, notice = notice)
                    }
                }
            }
        }
    }
}
