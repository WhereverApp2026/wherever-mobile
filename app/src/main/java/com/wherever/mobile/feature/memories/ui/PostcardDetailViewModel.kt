package com.wherever.mobile.feature.memories.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.memories.domain.MemoriesRepository
import com.wherever.mobile.feature.memories.domain.PostcardGallery
import com.wherever.mobile.feature.memories.domain.PostcardView
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PostcardDetailUiState(
    val loading: Boolean = true,
    val view: PostcardView? = null,
    /** 404: ajena o ya borrada. No hay Reintentar: no va a volver. */
    val gone: Boolean = false,
    val error: UiText? = null,
    val confirmDelete: Boolean = false,
    val deleting: Boolean = false,
    val saving: Boolean = false,
    val message: UiText? = null
)

/** 75_PostalVer y sus variantes `PostalVer · *` (specs/nv-31). */
@HiltViewModel
class PostcardDetailViewModel @Inject constructor(
    private val repository: MemoriesRepository,
    private val gallery: PostcardGallery,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val id: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(PostcardDetailUiState())
    val state: StateFlow<PostcardDetailUiState> = mutableState.asStateFlow()
    private val deletedChannel = Channel<Unit>(Channel.BUFFERED)

    /** La postal se borró: la pantalla regresa al álbum. */
    val deleted: Flow<Unit> = deletedChannel.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.postcard(id)) {
                is Result.Success -> mutableState.update { it.copy(loading = false, view = result.data) }
                is Result.Failure -> mutableState.update {
                    if (result.error is AppError.NotFound) {
                        it.copy(loading = false, gone = true)
                    } else {
                        it.copy(loading = false, error = result.error.toUiText())
                    }
                }
            }
        }
    }

    fun askDelete() = mutableState.update { if (it.view == null) it else it.copy(confirmDelete = true) }

    fun cancelDelete() = mutableState.update { it.copy(confirmDelete = false) }

    fun confirmDelete() {
        val postcard = state.value.view?.postcard ?: return
        if (state.value.deleting) return
        mutableState.update { it.copy(confirmDelete = false, deleting = true) }
        viewModelScope.launch {
            when (val result = repository.deletePostcard(postcard)) {
                is Result.Success -> {
                    mutableState.update { it.copy(deleting = false) }
                    deletedChannel.send(Unit)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(deleting = false, message = result.error.toUiText())
                }
            }
        }
    }

    /** Descargar: guarda la imagen publicada en la galería. */
    fun download() {
        val url = state.value.view?.postcard?.imageUrl ?: return
        if (state.value.saving) return
        mutableState.update { it.copy(saving = true) }
        viewModelScope.launch {
            val result = gallery.save(url)
            mutableState.update {
                it.copy(
                    saving = false,
                    message = when {
                        result is Result.Success -> UiText.Res(R.string.memories_downloaded)
                        (result as Result.Failure).error is AppError.Forbidden ->
                            UiText.Res(R.string.memories_download_use_share)
                        else -> result.error.toUiText()
                    }
                )
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
