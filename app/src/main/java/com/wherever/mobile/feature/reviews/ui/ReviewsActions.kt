package com.wherever.mobile.feature.reviews.ui

import com.wherever.mobile.feature.reviews.domain.MyReview
import com.wherever.mobile.feature.reviews.domain.PendingReview
import com.wherever.mobile.feature.reviews.domain.PlaceReview
import com.wherever.mobile.feature.reviews.domain.PlaceReviewQuery

// Eventos que las pantallas de reseñas envían a su ViewModel (sin lógica en los Composables).

/** Eventos de 79_MisRes. */
internal data class MyReviewsActions(
    val onBack: () -> Unit,
    val onTab: (MyReviewsTab) -> Unit,
    val onEdit: (MyReview) -> Unit,
    val onDelete: (MyReview) -> Unit,
    val onReview: (PendingReview) -> Unit,
    val onLoadMore: () -> Unit,
    val onRetry: () -> Unit
)

/** Eventos de 80_ResLugar. */
internal data class PlaceReviewsActions(
    val onBack: () -> Unit,
    val onQuery: (PlaceReviewQuery) -> Unit,
    val onUseful: (PlaceReview) -> Unit,
    val onReport: (PlaceReview) -> Unit,
    val onLoadMore: () -> Unit,
    val onRetry: () -> Unit
)
