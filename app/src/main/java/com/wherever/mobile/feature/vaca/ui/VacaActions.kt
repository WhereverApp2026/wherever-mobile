package com.wherever.mobile.feature.vaca.ui

import com.wherever.mobile.feature.vaca.domain.Debt
import com.wherever.mobile.feature.vaca.domain.SplitMode

/** Eventos de 28_Vaca. */
internal data class VacaActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onMode: (SplitMode) -> Unit = {},
    val onPay: (Debt) -> Unit = {},
    val onAdd: () -> Unit = {},
    val onMessageShown: () -> Unit = {}
)
