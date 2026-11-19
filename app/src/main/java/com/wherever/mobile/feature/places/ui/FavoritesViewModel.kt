package com.wherever.mobile.feature.places.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.places.domain.FavoritePlace
import com.wherever.mobile.feature.places.domain.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val loading: Boolean = true,
    val items: List<FavoritePlace> = emptyList(),
    val error: UiText? = null
)

/** US-029: lugares guardados. */
@HiltViewModel
class FavoritesViewModel @Inject constructor(private val repository: FavoritesRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(FavoritesUiState())
    val state: StateFlow<FavoritesUiState> = mutableState.asStateFlow()

    fun load() {
        mutableState.update { it.copy(loading = it.items.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = repository.favorites()) {
                is Result.Success -> mutableState.update { it.copy(loading = false, items = result.data) }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }

    fun remove(id: String) {
        val before = state.value.items
        mutableState.update { it.copy(items = before.filterNot { f -> f.id == id }) }
        viewModelScope.launch {
            val result = repository.setFavorite(id, false)
            if (result is Result.Failure) {
                mutableState.update {
                    it.copy(items = before, error = result.error.toUiText())
                }
            }
        }
    }
}
