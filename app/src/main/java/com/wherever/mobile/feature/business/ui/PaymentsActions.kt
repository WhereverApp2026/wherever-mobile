package com.wherever.mobile.feature.business.ui

/** Eventos de 57_NegCobros. */
internal data class PaymentsActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onMore: () -> Unit = {},
    val onConnect: () -> Unit = {},
    val onBank: () -> Unit = {},
    val onMessageShown: () -> Unit = {}
)
