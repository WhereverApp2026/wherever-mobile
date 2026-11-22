package com.wherever.mobile.feature.reviews.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.reviews.domain.PendingReview
import com.wherever.mobile.feature.reviews.domain.ReviewRepository
import com.wherever.mobile.feature.reviews.domain.ReviewedVisit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PendingReviewsUiState(
    val loading: Boolean = true,
    val pending: List<PendingReview> = emptyList(),
    val reviewed: List<ReviewedVisit> = emptyList(),
    val error: UiText? = null
) {
    val empty: Boolean get() = !loading && error == null && pending.isEmpty() && reviewed.isEmpty()
}

/** 77_ResPend: visitas por reseñar (`GET /resenas/pendientes`) y ya reseñadas (`GET /checkins`). */
@HiltViewModel
class PendingReviewsViewModel @Inject constructor(private val repository: ReviewRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(PendingReviewsUiState())
    val state: StateFlow<PendingReviewsUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val reviewed = async { repository.reviewedVisits() }
            when (val pending = repository.pending()) {
                is Result.Success -> {
                    // «Ya reseñadas» es complementaria: si falla, la lista principal se muestra igual.
                    val visits = (reviewed.await() as? Result.Success)?.data.orEmpty()
                    mutableState.update {
                        PendingReviewsUiState(loading = false, pending = pending.data, reviewed = visits)
                    }
                }
                is Result.Failure -> {
                    reviewed.cancel()
                    mutableState.update { it.copy(loading = false, error = pending.error.toUiText()) }
                }
            }
        }
    }
}
