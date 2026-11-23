package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BusinessPhoto
import com.wherever.mobile.feature.business.domain.BusinessProfileRepository
import com.wherever.mobile.feature.business.domain.MAX_PHOTOS
import com.wherever.mobile.feature.business.domain.MIN_PHOTOS
import com.wherever.mobile.feature.business.domain.PhotoType
import com.wherever.mobile.feature.photos.domain.ImageState
import com.wherever.mobile.feature.photos.domain.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GalleryUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    /** Orden que se ve (la primera es la portada); se guarda con «Guardar cambios». */
    val photos: List<BusinessPhoto> = emptyList(),
    val savedOrder: List<String> = emptyList(),
    val keepsMinPhotos: Boolean = false,
    val addOpen: Boolean = false,
    val uploading: Boolean = false,
    val saving: Boolean = false,
    val busy: Set<String> = emptySet(),
    val message: UiText? = null
) {
    val canAdd: Boolean get() = photos.size < MAX_PHOTOS && !uploading

    /** En revisión o aprobado no baja de 3. */
    val canDelete: Boolean get() = !keepsMinPhotos || photos.size > MIN_PHOTOS

    val orderChanged: Boolean get() = photos.map { it.id } != savedOrder

    val canSave: Boolean get() = orderChanged && !saving && !uploading
}

/** 87_NegGaleria y 88_NegAgregarFoto (specs/nv-38): ordenar, portada, borrar y agregar (máximo 10). */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: BusinessProfileRepository,
    private val uploads: PhotoRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(GalleryUiState())
    val state: StateFlow<GalleryUiState> = mutableState.asStateFlow()

    private var businessId: String? = null

    fun start(id: String) {
        if (businessId == id) return
        businessId = id
        load()
    }

    fun load() {
        val id = businessId ?: return
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.profile(id)) {
                is Result.Success -> mutableState.update {
                    it.copy(
                        loading = false,
                        photos = result.data.photos,
                        savedOrder = result.data.photos.map { p -> p.id },
                        keepsMinPhotos = result.data.keepsMinPhotos
                    )
                }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }

    /** Arrastrar: mueve la foto de [from] a [to] (solo en la pantalla hasta «Guardar cambios»). */
    fun move(from: Int, to: Int) = mutableState.update { state ->
        val photos = state.photos
        if (from !in photos.indices || to !in photos.indices || from == to) return@update state
        state.copy(photos = photos.toMutableList().apply { add(to, removeAt(from)) })
    }

    fun makeCover(photo: BusinessPhoto) = move(state.value.photos.indexOf(photo), 0)

    fun save() {
        val id = businessId ?: return
        val current = state.value
        if (!current.canSave) return
        mutableState.update { it.copy(saving = true) }
        viewModelScope.launch {
            when (val result = repository.reorderPhotos(id, current.photos.map { it.id })) {
                is Result.Success -> mutableState.update {
                    val photos = result.data.ifEmpty { current.photos }
                    it.copy(
                        saving = false,
                        photos = photos,
                        savedOrder = photos.map { p -> p.id },
                        message = UiText.Res(R.string.gallery_saved)
                    )
                }
                is Result.Failure -> mutableState.update { it.copy(saving = false, message = result.error.toUiText()) }
            }
        }
    }

    fun delete(photo: BusinessPhoto) {
        val id = businessId ?: return
        val current = state.value
        if (photo.id in current.busy) return
        if (!current.canDelete) {
            mutableState.update { it.copy(message = UiText.Res(R.string.gallery_min_photos)) }
            return
        }
        mutableState.update { it.copy(busy = it.busy + photo.id) }
        viewModelScope.launch {
            when (val result = repository.deletePhoto(id, photo.id)) {
                is Result.Success -> mutableState.update {
                    it.copy(
                        busy = it.busy - photo.id,
                        photos = it.photos - photo,
                        savedOrder = it.savedOrder - photo.id,
                        message = UiText.Res(R.string.gallery_deleted)
                    )
                }
                is Result.Failure -> mutableState.update {
                    it.copy(busy = it.busy - photo.id, message = result.error.toUiText())
                }
            }
        }
    }

    fun openAdd() {
        if (state.value.canAdd) mutableState.update { it.copy(addOpen = true) }
    }

    fun closeAdd() = mutableState.update { it.copy(addOpen = false) }

    /** Cámara o galería: se sube con `PhotoRepository` y se registra con `POST fotos`. */
    fun onPicked(source: String?) {
        val id = businessId ?: return
        mutableState.update { it.copy(addOpen = false) }
        if (source == null || !state.value.canAdd) return
        mutableState.update { it.copy(uploading = true, message = null) }
        viewModelScope.launch {
            val message: UiText = when (val uploaded = uploads.upload(source)) {
                is Result.Failure -> uploaded.error.toUiText()
                is Result.Success -> if (uploaded.data.state == ImageState.REJECTED) {
                    UiText.Res(R.string.photo_rejected)
                } else {
                    when (val added = repository.addPhoto(id, uploaded.data.id, PhotoType.INTERIOR)) {
                        is Result.Success -> {
                            mutableState.update { state ->
                                // Lo que ya se movió y no se guardó se respeta; la nueva va al final.
                                val known = state.photos.map { it.id }.toSet()
                                val fresh = added.data.filter { it.id !in known }
                                state.copy(
                                    photos = state.photos + fresh,
                                    savedOrder =
                                    state.savedOrder + fresh.map { it.id }
                                )
                            }
                            UiText.Res(R.string.gallery_added)
                        }
                        is Result.Failure -> added.error.toUiText()
                    }
                }
            }
            mutableState.update { it.copy(uploading = false, message = message) }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
