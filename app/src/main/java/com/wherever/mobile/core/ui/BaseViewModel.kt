package com.wherever.mobile.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<State, Event>(initialState: State, injectedScope: CoroutineScope? = null) : ViewModel() {

    protected val scope: CoroutineScope = injectedScope ?: viewModelScope

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    protected fun setState(state: State) {
        _state.value = state
    }

    protected fun updateState(transform: (State) -> State) {
        _state.update(transform)
    }

    protected fun emitEvent(event: Event) {
        scope.launch { _events.send(event) }
    }
}
