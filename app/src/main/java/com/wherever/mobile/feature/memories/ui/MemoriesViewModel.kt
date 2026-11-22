package com.wherever.mobile.feature.memories.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.memories.domain.MemoriesRepository
import com.wherever.mobile.feature.memories.domain.Memory
import com.wherever.mobile.feature.memories.domain.Postcard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AlbumTab { ALL, BY_PLAN }

data class MemoriesUiState(
    val tab: AlbumTab = AlbumTab.ALL,
    val loading: Boolean = true,
    val postcards: List<Postcard> = emptyList(),
    val postcardsCursor: String? = null,
    val memories: List<Memory> = emptyList(),
    val memoriesCursor: String? = null,
    val loadingMore: Boolean = false,
    val error: UiText? = null,
    val message: UiText? = null
) {
    /** 76_RecVacio: sin postales ni recuerdos. */
    val empty: Boolean get() = !loading && error == null && postcards.isEmpty() && memories.isEmpty()
}

/** 73_Album (Todas), 74_RecPlan (Por plan) y 76_RecVacio (specs/nv-31). */
@HiltViewModel
class MemoriesViewModel @Inject constructor(private val repository: MemoriesRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(MemoriesUiState())
    val state: StateFlow<MemoriesUiState> = mutableState.asStateFlow()

    private var resumedOnce = false

    init {
        load()
    }

    /** El primer resume coincide con el `init`; los siguientes (al volver) recargan sin esqueleto. */
    fun onResume() {
        if (!resumedOnce) {
            resumedOnce = true
            return
        }
        load(silent = true)
    }

    fun load(silent: Boolean = false) {
        if (!silent) mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val memories = async { repository.memories() }
            when (val postcards = repository.postcards()) {
                is Result.Success -> {
                    // «Por plan» es complementaria: si falla, Todas se muestra igual.
                    val plans = (memories.await() as? Result.Success)?.data
                    mutableState.update {
                        it.copy(
                            loading = false,
                            postcards = postcards.data.items,
                            postcardsCursor = postcards.data.nextCursor,
                            memories = plans?.items.orEmpty(),
                            memoriesCursor = plans?.nextCursor
                        )
                    }
                }
                is Result.Failure -> {
                    memories.cancel()
                    // Una recarga silenciosa que falla no tapa lo que ya se ve.
                    if (!silent) mutableState.update { it.copy(loading = false, error = postcards.error.toUiText()) }
                }
            }
        }
    }

    fun onTab(tab: AlbumTab) = mutableState.update { it.copy(tab = tab) }

    /** Siguiente página de la pestaña visible. */
    fun loadMore() {
        val current = state.value
        if (current.loadingMore || current.loading) return
        val cursor = (if (current.tab == AlbumTab.ALL) current.postcardsCursor else current.memoriesCursor) ?: return
        mutableState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            if (current.tab == AlbumTab.ALL) {
                when (val page = repository.postcards(cursor)) {
                    is Result.Success -> mutableState.update {
                        it.copy(
                            loadingMore = false,
                            postcards = it.postcards + page.data.items,
                            postcardsCursor = page.data.nextCursor
                        )
                    }
                    is Result.Failure -> mutableState.update {
                        it.copy(loadingMore = false, message = page.error.toUiText())
                    }
                }
            } else {
                when (val page = repository.memories(cursor)) {
                    is Result.Success -> mutableState.update {
                        it.copy(
                            loadingMore = false,
                            memories = it.memories + page.data.items,
                            memoriesCursor = page.data.nextCursor
                        )
                    }
                    is Result.Failure -> mutableState.update {
                        it.copy(loadingMore = false, message = page.error.toUiText())
                    }
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
