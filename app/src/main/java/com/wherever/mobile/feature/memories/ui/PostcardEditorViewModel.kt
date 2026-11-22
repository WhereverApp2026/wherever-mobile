package com.wherever.mobile.feature.memories.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.fieldErrors
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.memories.domain.MemoriesRepository
import com.wherever.mobile.feature.memories.domain.NewPostcard
import com.wherever.mobile.feature.memories.domain.POSTCARD_TEXT_MAX
import com.wherever.mobile.feature.memories.domain.PostcardRenderer
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import com.wherever.mobile.feature.memories.domain.Visibility
import com.wherever.mobile.feature.photos.domain.ImageState
import com.wherever.mobile.feature.photos.domain.PhotoRepository
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

/** La moderación rechazó la foto (`ImageState.REJECTED`): no se crea la postal. */
private val PHOTO_REJECTED = AppError.Unknown()

/** Lo que pasa al terminar: ir al álbum o abrir la hoja de compartir con la imagen dibujada. */
sealed interface PostcardSaved {
    data object ToAlbum : PostcardSaved

    data class Share(val fileUri: String) : PostcardSaved
}

private const val FIELD_TEXT = "texto"

data class PostcardEditorUiState(
    val source: String = "",
    val placeName: String = "",
    val date: String = "",
    val style: PostcardStyle = PostcardStyle.CLASSIC,
    val text: String = "",
    val saving: Boolean = false,
    val error: UiText? = null,
    /** Motivo de la API para el texto (p. ej. moderación), bajo el campo. */
    val textError: UiText? = null
) {
    val canSave: Boolean get() = source.isNotBlank() && text.length <= POSTCARD_TEXT_MAX && !saving
}

/**
 * 72_Postal: estilo, texto ≤ 80, Guardar en mi álbum (privada) y Compartir postal (specs/nv-31).
 * Dibuja la postal, la sube por `PhotoRepository` y la crea con `POST /postales`; si la API la rechaza,
 * borra la imagen subida para no dejarla huérfana.
 */
@HiltViewModel
class PostcardEditorViewModel @Inject constructor(
    private val repository: MemoriesRepository,
    private val renderer: PostcardRenderer,
    private val photos: PhotoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val placeId: String = savedStateHandle.get<String>("placeId").orEmpty()
    private val planId: String = savedStateHandle.get<String>("planId").orEmpty()
    private val mutableState = MutableStateFlow(
        PostcardEditorUiState(
            source = savedStateHandle.get<String>("source").orEmpty(),
            placeName = savedStateHandle.get<String>("placeName").orEmpty(),
            date = savedStateHandle.get<String>("date").orEmpty()
        )
    )
    val state: StateFlow<PostcardEditorUiState> = mutableState.asStateFlow()
    private val savedChannel = Channel<PostcardSaved>(Channel.BUFFERED)
    val saved: Flow<PostcardSaved> = savedChannel.receiveAsFlow()

    /** La postal ya creada: compartir otra vez no la vuelve a subir. */
    private var created: String? = null

    /** «Saludos desde…» de `strings.xml`, que manda la pantalla: es el título si no se escribe texto. */
    private var defaultTitle: String = ""

    fun onDefaultTitle(title: String) {
        defaultTitle = title
    }

    fun onStyle(style: PostcardStyle) = mutableState.update { it.copy(style = style, error = null) }

    fun onText(value: String) = mutableState.update {
        it.copy(text = value.take(POSTCARD_TEXT_MAX), error = null, textError = null)
    }

    fun save() = submit(share = false)

    fun share() = submit(share = true)

    private fun submit(share: Boolean) {
        val current = state.value
        if (!current.canSave) return
        mutableState.update { it.copy(saving = true, error = null, textError = null) }
        viewModelScope.launch {
            when (val result = createOnce(current)) {
                is Result.Success -> {
                    mutableState.update { it.copy(saving = false) }
                    savedChannel.send(if (share) PostcardSaved.Share(result.data) else PostcardSaved.ToAlbum)
                }
                is Result.Failure -> mutableState.update {
                    // Probado en el Xiaomi: la moderación rechaza el texto con un 422 en `texto` y solo se veía
                    // «Datos inválidos» escondido bajo los botones. El motivo va bajo el campo.
                    val textError = result.error.fieldErrors()[FIELD_TEXT]
                    val message = when {
                        textError != null -> null
                        result.error === PHOTO_REJECTED -> UiText.Res(R.string.memories_photo_rejected)
                        else -> result.error.toUiText()
                    }
                    it.copy(saving = false, error = message, textError = textError)
                }
            }
        }
    }

    /** Dibuja, sube y crea la postal privada. Devuelve el archivo dibujado (para compartir). */
    private suspend fun createOnce(current: PostcardEditorUiState): Result<String> {
        val title = current.text.ifBlank { defaultTitle.ifBlank { current.placeName } }
        val drawn = renderer.render(current.source, current.style, title, subtitleOf(current))
        return when {
            drawn !is Result.Success -> drawn
            created != null -> drawn
            else -> uploadAndCreate(current, drawn.data)
        }
    }

    private suspend fun uploadAndCreate(current: PostcardEditorUiState, file: String): Result<String> =
        when (val uploaded = photos.upload(file)) {
            is Result.Failure -> uploaded
            is Result.Success -> if (uploaded.data.state == ImageState.REJECTED) {
                Result.Failure(PHOTO_REJECTED)
            } else {
                create(current, uploaded.data.id, file)
            }
        }

    private suspend fun create(current: PostcardEditorUiState, imageId: String, file: String): Result<String> {
        val postcard = NewPostcard(
            imageId = imageId,
            style = current.style,
            text = current.text,
            visibility = Visibility.PRIVATE,
            placeId = placeId,
            planId = planId
        )
        return when (val result = repository.createPostcard(postcard)) {
            is Result.Success -> {
                created = result.data
                Result.Success(file)
            }
            is Result.Failure -> {
                repository.deleteImage(imageId)
                result
            }
        }
    }

    private fun subtitleOf(current: PostcardEditorUiState) =
        listOf(current.placeName, current.date).filter { it.isNotBlank() }.joinToString(" · ")
}
