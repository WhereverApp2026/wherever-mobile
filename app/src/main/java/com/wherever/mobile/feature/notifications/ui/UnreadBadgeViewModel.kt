package com.wherever.mobile.feature.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.notifications.domain.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Número de la campana de Inicio (`GET /notificaciones/no-leidas`). Sin red se queda el último. */
@HiltViewModel
class UnreadBadgeViewModel @Inject constructor(private val repository: NotificationsRepository) : ViewModel() {
    private val mutableCount = MutableStateFlow(0)
    val count: StateFlow<Int> = mutableCount.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            (repository.unreadCount() as? Result.Success)?.let { mutableCount.value = it.data.coerceAtLeast(0) }
        }
    }
}
