package com.wherever.mobile.feature.places.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.places.domain.FavoritesRepository
import com.wherever.mobile.feature.places.domain.MenuSection
import com.wherever.mobile.feature.places.domain.PlaceDetail
import com.wherever.mobile.feature.places.domain.PlacesRepository
import com.wherever.mobile.feature.places.domain.ReportReason
import com.wherever.mobile.feature.places.domain.Review
import com.wherever.mobile.feature.places.domain.ReviewQuery
import com.wherever.mobile.feature.places.domain.ReviewsRepository
import com.wherever.mobile.feature.places.domain.Traffic
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DAYS_IN_WEEK = 7
internal const val REPORT_DETAIL_MAX = 300

data class ReportForm(
    val reason: ReportReason? = null,
    val detail: String = "",
    val sending: Boolean = false,
    val error: UiText? = null
) {
    val canSend: Boolean get() = reason != null && (reason != ReportReason.OTHER || detail.isNotBlank()) && !sending
}

data class PlaceDetailUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    val place: PlaceDetail? = null,
    val today: Int = 0,
    val traffic: Traffic? = null,
    val menu: List<MenuSection> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val reviewQuery: ReviewQuery = ReviewQuery(),
    val reviewsCursor: String? = null,
    val usefulMarked: Set<String> = emptySet(),
    val favorite: Boolean = false,
    val report: ReportForm? = null,
    val message: UiText? = null
)

/** 20_FichaNegocio. `id` y `origin` los lee de SavedStateHandle (PlaceRoute). */
@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val repository: PlacesRepository,
    private val reviewsRepository: ReviewsRepository,
    private val favorites: FavoritesRepository,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val placeId: String = checkNotNull(savedStateHandle["id"])
    private val origin: String = savedStateHandle.get<String>("origin").orEmpty().ifBlank { "busqueda" }

    private val mutableState = MutableStateFlow(
        PlaceDetailUiState(today = LocalDate.now(clock).dayOfWeek.value % DAYS_IN_WEEK)
    )
    val state: StateFlow<PlaceDetailUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.detail(placeId)) {
                is Result.Success -> {
                    mutableState.update { it.copy(loading = false, place = result.data) }
                    loadExtras()
                }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }

    private fun loadExtras() {
        viewModelScope.launch { repository.registerView(placeId, origin) }
        viewModelScope.launch {
            (repository.traffic(placeId) as? Result.Success)?.let { r ->
                mutableState.update { it.copy(traffic = r.data) }
            }
        }
        viewModelScope.launch {
            (repository.menu(placeId) as? Result.Success)?.let { r -> mutableState.update { it.copy(menu = r.data) } }
        }
        viewModelScope.launch {
            (favorites.favoriteIds() as? Result.Success)?.let { r ->
                mutableState.update { it.copy(favorite = placeId in r.data) }
            }
        }
        loadReviews(ReviewQuery())
    }

    /** Filtro por estrellas (US-074). Tocar el mismo lo quita. */
    fun onStarsFilter(stars: Int?) {
        val current = state.value.reviewQuery
        loadReviews(current.copy(stars = stars.takeIf { it != current.stars }, cursor = null))
    }

    fun onRecentOrder(recent: Boolean) = loadReviews(state.value.reviewQuery.copy(recent = recent, cursor = null))

    fun loadMoreReviews() {
        val cursor = state.value.reviewsCursor ?: return
        loadReviews(state.value.reviewQuery.copy(cursor = cursor), append = true)
    }

    private fun loadReviews(query: ReviewQuery, append: Boolean = false) {
        mutableState.update { it.copy(reviewQuery = query) }
        viewModelScope.launch {
            when (val result = reviewsRepository.reviews(placeId, query)) {
                is Result.Success -> mutableState.update {
                    it.copy(
                        reviews = if (append) it.reviews + result.data.items else result.data.items,
                        reviewsCursor = result.data.nextCursor
                    )
                }
                is Result.Failure -> mutableState.update { it.copy(message = result.error.toUiText()) }
            }
        }
    }

    /** US-121: "Útil" una vez por reseña; un error lo desmarca. */
    fun markUseful(reviewId: String) {
        if (reviewId in state.value.usefulMarked) return
        mutableState.update { it.copy(usefulMarked = it.usefulMarked + reviewId) }
        viewModelScope.launch {
            if (reviewsRepository.markReviewUseful(reviewId, true) is Result.Failure) {
                mutableState.update {
                    it.copy(
                        usefulMarked = it.usefulMarked - reviewId,
                        message = UiText.Res(R.string.place_useful_error)
                    )
                }
            }
        }
    }

    /** US-029: el corazón cambia al instante; si la API falla, vuelve a como estaba. */
    fun toggleFavorite() {
        val wanted = !state.value.favorite
        mutableState.update { it.copy(favorite = wanted) }
        viewModelScope.launch {
            when (val result = favorites.setFavorite(placeId, wanted)) {
                is Result.Success -> mutableState.update {
                    it.copy(
                        message = UiText.Res(
                            if (wanted) R.string.place_favorite_added else R.string.place_favorite_removed
                        )
                    )
                }
                is Result.Failure -> mutableState.update {
                    it.copy(favorite = !wanted, message = result.error.toUiText())
                }
            }
        }
    }

    fun openReport() = mutableState.update { it.copy(report = ReportForm()) }

    fun closeReport() = mutableState.update { it.copy(report = null) }

    fun onReportReason(reason: ReportReason) = mutableState.update {
        it.copy(report = it.report?.copy(reason = reason))
    }

    fun onReportDetail(detail: String) =
        mutableState.update { it.copy(report = it.report?.copy(detail = detail.take(REPORT_DETAIL_MAX))) }

    /** US-073: reportar información incorrecta. 409 = ya tiene uno abierto en este negocio. */
    fun sendReport() {
        val form = state.value.report ?: return
        val reason = form.reason ?: return
        if (!form.canSend) return
        mutableState.update { it.copy(report = form.copy(sending = true, error = null)) }
        viewModelScope.launch {
            when (val result = repository.report(placeId, reason, form.detail)) {
                is Result.Success -> mutableState.update {
                    it.copy(report = null, message = UiText.Res(R.string.place_report_sent))
                }
                is Result.Failure -> mutableState.update {
                    it.copy(report = it.report?.copy(sending = false, error = result.error.toUiText()))
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
