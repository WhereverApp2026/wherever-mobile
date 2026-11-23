package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.feature.business.domain.ReviewPeriod
import com.wherever.mobile.feature.reviews.domain.PlaceReview

/** Eventos de 51_NegResenas. */
internal data class BusinessReviewsActions(
    val onPeriod: (ReviewPeriod) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onReply: (PlaceReview) -> Unit = {},
    val onMoments: () -> Unit = {},
    val onMessageShown: () -> Unit = {}
)
