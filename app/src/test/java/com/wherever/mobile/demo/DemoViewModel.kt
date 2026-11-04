package com.wherever.mobile.demo

import com.wherever.mobile.core.ui.BaseViewModel
import com.wherever.mobile.core.ui.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface DemoEvent {
    data class Message(val text: String) : DemoEvent
}

class DemoViewModel(private val repository: DemoRepository, scope: CoroutineScope) :
    BaseViewModel<UiState<List<DemoItem>>, DemoEvent>(UiState.Loading, scope) {

    fun load() {
        scope.launch {
            setState(UiState.Loading)
            repository.refresh()
            val items = repository.observe().first().getOrNull().orEmpty()
            setState(if (items.isEmpty()) UiState.Empty else UiState.Content(items))
        }
    }

    fun announce(message: String) {
        emitEvent(DemoEvent.Message(message))
    }
}
