package com.wherever.mobile.feature.checkin.ui

/** Eventos de la llegada y de 26_CheckinOk. */
internal data class ArrivalActions(
    val onBack: () -> Unit = {},
    val onUseLocation: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onScan: () -> Unit = {},
    val onPostcard: () -> Unit = {},
    val onNext: () -> Unit = {}
)
