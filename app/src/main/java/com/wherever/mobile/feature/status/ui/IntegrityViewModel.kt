package com.wherever.mobile.feature.status.ui

import androidx.lifecycle.ViewModel
import com.wherever.mobile.BuildConfig
import com.wherever.mobile.core.security.DeviceSignalsReader
import com.wherever.mobile.core.security.IntegrityAction
import com.wherever.mobile.core.security.IntegrityPolicy
import com.wherever.mobile.core.security.IntegrityVerdict
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class IntegrityUiState(val verdict: IntegrityVerdict, val warningDismissed: Boolean = false) {
    val blocked: Boolean
        get() = verdict.action == IntegrityAction.BLOCK

    val showWarning: Boolean
        get() = verdict.action == IntegrityAction.WARN && !warningDismissed
}

/** Root, depurador y re-empaquetado (constitución §6): en debug avisa; en release bloquea. */
@HiltViewModel
class IntegrityViewModel @Inject constructor(reader: DeviceSignalsReader) : ViewModel() {
    private val mutableState = MutableStateFlow(
        IntegrityUiState(IntegrityPolicy.evaluate(reader.read(), BuildConfig.DEBUG, BuildConfig.RELEASE_CERT_SHA256))
    )
    val state: StateFlow<IntegrityUiState> = mutableState.asStateFlow()

    fun dismissWarning() = mutableState.update { it.copy(warningDismissed = true) }
}
