package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.feature.business.domain.BusinessMoment

/** Eventos de NegMomentos. */
internal data class BusinessMomentsActions(
    val onBack: () -> Unit = {},
    val onTab: (MomentsTab) -> Unit = {},
    val onThank: (BusinessMoment) -> Unit = {},
    val onFeature: (BusinessMoment) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onMessageShown: () -> Unit = {}
)
