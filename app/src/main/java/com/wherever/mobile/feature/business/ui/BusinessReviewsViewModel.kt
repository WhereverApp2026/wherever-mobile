package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BusinessHomeRepository
import com.wherever.mobile.feature.business.domain.BusinessReviewsRepository
import com.wherever.mobile.feature.business.domain.ReviewPeriod
import com.wherever.mobile.feature.business.domain.ReviewSummary
import com.wherever.mobile.feature.business.domain.SealProgress
import com.wherever.mobile.feature.reviews.domain.PlaceReview
import com.wherever.mobile.feature.reviews.domain.PlaceReviewQuery
import com.wherever.mobile.feature.reviews.domain.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BusinessReviewsUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    val period: ReviewPeriod = ReviewPeriod.MONTH,
    val summary: ReviewSummary? = null,
    val hasSeal: Boolean = false,
    val sealProgress: SealProgress? = null,
    val reviews: List<PlaceReview> = emptyList(),
    /** Momentos sin agradecer; nulo si no respondió la API. */
    val newMoments: Int? = null
) {
    /** Avance del criterio «responder reseñas» del Sello, para el aviso de 52_NegResp. */
    val replyProgress: Int?
        get() = sealProgress?.criteria
            ?.firstOrNull { it.name.lowercase(Locale.ROOT).contains("respond") }
            ?.current?.toInt()
}

/** 51_NegResenas (specs/nv-36): Sello, aspectos del periodo, reseñas y acceso a los momentos. */
@HiltViewModel
class BusinessReviewsViewModel @Inject constructor(
    private val reviews: BusinessReviewsRepository,
    private val placeReviews: ReviewRepository,
    private val home: BusinessHomeRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(BusinessReviewsUiState())
    val state: StateFlow<BusinessReviewsUiState> = mutableState.asStateFlow()

    private var businessId: String? = null

    /** Se pide al abrir y al volver de responder. */
    fun start(id: String) {
        businessId = id
        load()
    }

    fun onPeriod(period: ReviewPeriod) {
        if (period == state.value.period) return
        mutableState.update { it.copy(period = period) }
        load()
    }

    fun load() {
        val id = businessId ?: return
        val period = state.value.period
        mutableState.update { it.copy(loading = it.summary == null && it.reviews.isEmpty(), error = null) }
        viewModelScope.launch {
            val summary = async { reviews.summary(id, period) }
            val panel = async { home.panel(id) }
            val moments = async { reviews.moments(id, featured = false) }
            when (val list = placeReviews.placeReviews(id, PlaceReviewQuery(recent = true), cursor = null)) {
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = list.error.toUiText()) }
                is Result.Success -> {
                    val panelData = (panel.await() as? Result.Success)?.data
                    mutableState.update {
                        it.copy(
                            loading = false,
                            reviews = list.data.items,
                            // El resumen, el Sello y los momentos son complementos: si fallan, se ocultan.
                            summary = (summary.await() as? Result.Success)?.data,
                            hasSeal = panelData?.seal == true,
                            sealProgress = panelData?.sealProgress,
                            newMoments = (moments.await() as? Result.Success)?.data?.count { m -> !m.thanked }
                        )
                    }
                }
            }
        }
    }
}
