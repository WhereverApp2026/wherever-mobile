package com.wherever.mobile.feature.business.ui

import java.time.LocalDate

/** Eventos de 55_NegHoras. */
internal data class CapacityActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onDay: (LocalDate) -> Unit = {},
    val onCapacity: (Int) -> Unit = {},
    val onTurn: (Int) -> Unit = {},
    val onSave: () -> Unit = {},
    val onMessageShown: () -> Unit = {}
)
