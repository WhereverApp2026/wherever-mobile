package com.wherever.mobile.feature.reviews.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.reviews.domain.PlaceBrief
import com.wherever.mobile.feature.reviews.domain.PlaceReview
import com.wherever.mobile.feature.reviews.domain.PlaceReviewQuery
import com.wherever.mobile.feature.reviews.domain.ReportReason
import com.wherever.mobile.feature.reviews.domain.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Hoja «Reportar» de una reseña: motivo obligatorio. */
data class ReviewReportForm(
    val reviewId: String,
    val reason: ReportReason? = null,
    val sending: Boolean = false,
    val error: UiText? = null
) {
    val canSend: Boolean get() = reason != null && !sending
}

data class PlaceReviewsUiState(
    val placeName: String = "",
    val place: PlaceBrief? = null,
    val query: PlaceReviewQuery = PlaceReviewQuery(),
    val loading: Boolean = true,
    val reviews: List<PlaceReview> = emptyList(),
    val cursor: String? = null,
    val loadingMore: Boolean = false,
    val error: UiText? = null,
    /** Reseñas que marqué como útiles en esta visita (la lista pública no dice cuáles). */
    val usefulMarked: Set<String> = emptySet(),
    val report: ReviewReportForm? = null,
    val message: UiText? = null
) {
    val empty: Boolean get() = !loading && error == null && reviews.isEmpty()
}

/** 80_ResLugar: promedio, filtros, lista paginada, Útil y Reportar. */
@HiltViewModel
class PlaceReviewsViewModel @Inject constructor(
    private val repository: ReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val placeId: String = checkNotNull(savedStateHandle["placeId"])
    private val mutableState = MutableStateFlow(
        PlaceReviewsUiState(placeName = savedStateHandle.get<String>("placeName").orEmpty())
    )
    val state: StateFlow<PlaceReviewsUiState> = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadPlace()
        load()
    }

    private fun loadPlace() {
        viewModelScope.launch {
            val place = (repository.place(placeId) as? Result.Success)?.data ?: return@launch
            mutableState.update { it.copy(place = place, placeName = it.placeName.ifBlank { place.name }) }
        }
    }

    fun load() {
        loadJob?.cancel()
        mutableState.update { it.copy(loading = true, error = null) }
        loadJob = viewModelScope.launch {
            when (val page = repository.placeReviews(placeId, state.value.query, cursor = null)) {
                is Result.Success -> mutableState.update {
                    it.copy(loading = false, reviews = page.data.items, cursor = page.data.nextCursor)
                }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = page.error.toUiText()) }
            }
        }
    }

    fun loadMore() {
        val cursor = state.value.cursor ?: return
        if (state.value.loadingMore || state.value.loading) return
        mutableState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            when (val page = repository.placeReviews(placeId, state.value.query, cursor)) {
                is Result.Success -> mutableState.update {
                    it.copy(loadingMore = false, reviews = it.reviews + page.data.items, cursor = page.data.nextCursor)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loadingMore = false, message = page.error.toUiText())
                }
            }
        }
    }

    fun onQuery(query: PlaceReviewQuery) {
        if (query == state.value.query) return
        mutableState.update { it.copy(query = query) }
        load()
    }

    /** Útil se prende y se apaga; el contador sale de la respuesta de la API. */
    fun toggleUseful(review: PlaceReview) {
        val useful = review.id !in state.value.usefulMarked
        viewModelScope.launch {
            when (val result = repository.markUseful(review.id, useful)) {
                is Result.Success -> mutableState.update { state ->
                    state.copy(
                        usefulMarked = if (result.data.useful) {
                            state.usefulMarked + review.id
                        } else {
                            state.usefulMarked - review.id
                        },
                        reviews = state.reviews.map {
                            if (it.id == review.id) it.copy(useful = result.data.total) else it
                        }
                    )
                }
                is Result.Failure -> mutableState.update { it.copy(message = result.error.toUiText()) }
            }
        }
    }

    fun openReport(review: PlaceReview) = mutableState.update { it.copy(report = ReviewReportForm(review.id)) }

    fun onReportReason(reason: ReportReason) = mutableState.update {
        it.copy(report = it.report?.copy(reason = reason, error = null))
    }

    fun closeReport() = mutableState.update { if (it.report?.sending == true) it else it.copy(report = null) }

    fun sendReport() {
        val form = state.value.report ?: return
        val reason = form.reason ?: return
        if (!form.canSend) return
        mutableState.update { it.copy(report = form.copy(sending = true, error = null)) }
        viewModelScope.launch {
            val result = repository.report(form.reviewId, reason)
            mutableState.update {
                when {
                    result is Result.Success ->
                        it.copy(report = null, message = UiText.Res(R.string.reviews_report_sent))
                    // 409: ya la reporté antes. No es un error que arreglar.
                    (result as Result.Failure).error is AppError.Conflict ->
                        it.copy(report = null, message = UiText.Res(R.string.reviews_report_already))
                    else -> it.copy(report = form.copy(sending = false, error = result.error.toUiText()))
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
