package com.wherever.mobile.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.photos.domain.ImageState
import com.wherever.mobile.feature.photos.domain.PhotoRepository
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfilePhotoUiState(
    val choosing: Boolean = false,
    val uploading: Boolean = false,
    /** Lo que se eligió, para mostrarlo mientras sube. */
    val preview: String? = null,
    val message: UiText? = null
)

/** Foto de perfil (US-017): galería o cámara → comprimir → subir → `PATCH /usuarios/yo {foto_url}`. */
@HiltViewModel
class ProfilePhotoViewModel @Inject constructor(
    private val photos: PhotoRepository,
    private val profiles: ProfileRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(ProfilePhotoUiState())
    val state: StateFlow<ProfilePhotoUiState> = mutableState.asStateFlow()

    fun openChooser() = mutableState.update { it.copy(choosing = true) }

    fun closeChooser() = mutableState.update { it.copy(choosing = false) }

    /** `null` = la persona canceló la galería o la cámara. */
    fun onPicked(source: String?) {
        mutableState.update { it.copy(choosing = false) }
        if (source == null || state.value.uploading) return
        mutableState.update { it.copy(uploading = true, preview = source, message = null) }
        viewModelScope.launch {
            val message = when (val uploaded = photos.upload(source)) {
                is Result.Failure -> uploaded.error.photoMessage()
                is Result.Success -> when (uploaded.data.state) {
                    ImageState.REJECTED -> UiText.Res(R.string.photo_rejected)
                    ImageState.IN_REVIEW -> UiText.Res(R.string.photo_in_review)
                    ImageState.PUBLISHED -> uploaded.data.url?.let { url ->
                        when (val saved = profiles.updatePhoto(url)) {
                            is Result.Success -> UiText.Res(R.string.photo_saved)
                            is Result.Failure -> saved.error.toUiText()
                        }
                    } ?: UiText.Res(R.string.photo_in_review)
                }
            }
            mutableState.update { it.copy(uploading = false, preview = null, message = message) }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}

/** Una `Validation` sin mensaje viene del teléfono: la foto no se pudo leer o pesa demasiado. */
private fun AppError.photoMessage(): UiText = when {
    this is AppError.Validation && apiMessage == null -> UiText.Res(R.string.photo_unreadable)
    this is AppError.Server && apiMessage == null -> UiText.Res(R.string.photo_upload_failed)
    else -> toUiText()
}
