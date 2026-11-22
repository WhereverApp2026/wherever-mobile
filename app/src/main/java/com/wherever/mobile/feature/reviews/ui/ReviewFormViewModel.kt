package com.wherever.mobile.feature.reviews.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.fieldErrors
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.photos.domain.ImageState
import com.wherever.mobile.feature.photos.domain.PhotoRepository
import com.wherever.mobile.feature.reviews.domain.Aspect
import com.wherever.mobile.feature.reviews.domain.COMMENT_MAX_LENGTH
import com.wherever.mobile.feature.reviews.domain.MAX_PHOTOS
import com.wherever.mobile.feature.reviews.domain.MAX_STARS
import com.wherever.mobile.feature.reviews.domain.MyReview
import com.wherever.mobile.feature.reviews.domain.ReviewDraft
import com.wherever.mobile.feature.reviews.domain.ReviewPhoto
import com.wherever.mobile.feature.reviews.domain.ReviewRepository
import com.wherever.mobile.feature.reviews.domain.isComplete
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val FIELD_COMMENT = "comentario"

/** Paso visible: 68_PostVisita, 78_ResCom o 69_Publicado. */
enum class ReviewStep { FORM, DETAIL, PUBLISHED }

data class ReviewFormUiState(
    val placeName: String = "",
    val placePhotoUrl: String? = null,
    val day: String = "",
    val editing: Boolean = false,
    val rating: Int = 0,
    val comment: String = "",
    val photos: List<ReviewPhoto> = emptyList(),
    val aspects: Map<Aspect, Int> = emptyMap(),
    val step: ReviewStep = ReviewStep.FORM,
    val uploading: Boolean = false,
    val submitting: Boolean = false,
    /** Edición sin la reseña en memoria (se abrió sin pasar por Mis reseñas). */
    val missing: Boolean = false,
    val error: UiText? = null,
    val commentError: UiText? = null,
    val photoMessage: UiText? = null,
    val published: MyReview? = null
) {
    val canAddPhoto: Boolean get() = photos.size < MAX_PHOTOS && !uploading && !submitting
    val detailComplete: Boolean get() = aspects.isComplete()

    /** Estrellas obligatorias y comentario ≤ 500; los aspectos se piden en 78_ResCom al publicar. */
    val canPublish: Boolean
        get() = rating in 1..MAX_STARS && comment.length <= COMMENT_MAX_LENGTH && !uploading && !submitting
}

/** 68_PostVisita → 78_ResCom → 69_Publicado (specs/nv-32). Crear con `checkinId`; editar con `reviewId`. */
@HiltViewModel
class ReviewFormViewModel @Inject constructor(
    private val repository: ReviewRepository,
    private val photos: PhotoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val checkinId: String = savedStateHandle.get<String>("checkinId").orEmpty()
    private val placeId: String = savedStateHandle.get<String>("placeId").orEmpty()
    private val reviewId: String = savedStateHandle.get<String>("reviewId").orEmpty()
    private var editing: MyReview? = null

    private val mutableState = MutableStateFlow(
        ReviewFormUiState(
            placeName = savedStateHandle.get<String>("placeName").orEmpty(),
            day = savedStateHandle.get<String>("day").orEmpty(),
            editing = reviewId.isNotBlank()
        )
    )
    val state: StateFlow<ReviewFormUiState> = mutableState.asStateFlow()

    init {
        if (reviewId.isNotBlank()) loadForEdit() else loadPlace(placeId)
    }

    private fun loadForEdit() {
        val review = repository.cached(reviewId)
        if (review == null) {
            mutableState.update { it.copy(missing = true) }
            return
        }
        editing = review
        mutableState.update {
            it.copy(
                placeName = review.placeName,
                placePhotoUrl = review.placePhotoUrl,
                rating = review.rating,
                comment = review.comment,
                photos = review.photos,
                aspects = review.aspects
            )
        }
    }

    private fun loadPlace(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            val place = (repository.place(id) as? Result.Success)?.data ?: return@launch
            mutableState.update {
                it.copy(placeName = it.placeName.ifBlank { place.name }, placePhotoUrl = place.photoUrl)
            }
        }
    }

    fun onRating(value: Int) = mutableState.update { it.copy(rating = value.coerceIn(1, MAX_STARS), error = null) }

    fun onComment(value: String) = mutableState.update {
        it.copy(comment = value.take(COMMENT_MAX_LENGTH), commentError = null, error = null)
    }

    fun onAspect(aspect: Aspect, value: Int) = mutableState.update {
        it.copy(aspects = it.aspects + (aspect to value.coerceIn(1, MAX_STARS)))
    }

    fun openDetail() = mutableState.update { it.copy(step = ReviewStep.DETAIL) }

    fun closeDetail() = mutableState.update { it.copy(step = ReviewStep.FORM) }

    /** «Listo» de 78_ResCom: con todo calificado publica; si falta algo, regresa a 68. */
    fun onDetailDone() {
        if (state.value.detailComplete && state.value.canPublish) submit() else closeDetail()
    }

    /** «Publicar»: la API pide los cinco aspectos; si faltan, abre 78_ResCom antes de enviar. */
    fun publish() {
        val current = state.value
        if (!current.canPublish) return
        if (!current.detailComplete) {
            mutableState.update { it.copy(step = ReviewStep.DETAIL) }
            return
        }
        submit()
    }

    fun onPhotoPicked(source: String) {
        if (!state.value.canAddPhoto) return
        mutableState.update { it.copy(uploading = true, photoMessage = null) }
        viewModelScope.launch {
            when (val result = photos.upload(source)) {
                is Result.Success -> {
                    val image = result.data
                    mutableState.update {
                        when (image.state) {
                            ImageState.PUBLISHED -> it.copy(
                                uploading = false,
                                photos = it.photos + ReviewPhoto(image.id, image.thumbnailUrl ?: image.url.orEmpty())
                            )
                            ImageState.IN_REVIEW ->
                                it.copy(uploading = false, photoMessage = UiText.Res(R.string.reviews_photo_in_review))
                            ImageState.REJECTED ->
                                it.copy(uploading = false, photoMessage = UiText.Res(R.string.reviews_photo_rejected))
                        }
                    }
                }
                is Result.Failure -> mutableState.update {
                    it.copy(uploading = false, photoMessage = result.error.toUiText())
                }
            }
        }
    }

    fun onRemovePhoto(id: String) = mutableState.update { it.copy(photos = it.photos.filterNot { p -> p.id == id }) }

    fun photoMessageShown() = mutableState.update { it.copy(photoMessage = null) }

    private fun submit() {
        val current = state.value
        val draft = ReviewDraft(
            checkinId = editing?.checkinId ?: checkinId,
            rating = current.rating,
            aspects = current.aspects,
            comment = current.comment,
            photoIds = current.photos.map { it.id }
        )
        mutableState.update { it.copy(submitting = true, error = null, commentError = null) }
        viewModelScope.launch {
            val review = editing
            val result = if (review != null) repository.update(review, draft) else repository.create(draft)
            when (result) {
                is Result.Success -> {
                    editing = result.data.takeIf { review != null }
                    mutableState.update {
                        it.copy(submitting = false, step = ReviewStep.PUBLISHED, published = result.data)
                    }
                }
                is Result.Failure -> onFailure(result.error)
            }
        }
    }

    /** 422 de moderación: se explica sin perder lo escrito. 409 al editar: se recarga la versión. */
    private suspend fun onFailure(error: AppError) {
        if (error is AppError.Conflict && editing != null) {
            repository.mine()
            editing = repository.cached(reviewId) ?: editing
        }
        val commentError = error.fieldErrors()[FIELD_COMMENT]
        mutableState.update {
            it.copy(
                submitting = false,
                step = ReviewStep.FORM,
                commentError = commentError,
                error = when {
                    commentError != null -> null
                    error is AppError.Conflict && editing != null -> UiText.Res(R.string.reviews_edit_conflict)
                    else -> error.toUiText()
                }
            )
        }
    }
}
