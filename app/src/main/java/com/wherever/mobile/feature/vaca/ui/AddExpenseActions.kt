package com.wherever.mobile.feature.vaca.ui

import com.wherever.mobile.feature.vaca.domain.ExpenseConcept

/** Eventos de 45_AgregarGasto. */
internal data class AddExpenseActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onDescription: (String) -> Unit = {},
    val onConcept: (ExpenseConcept) -> Unit = {},
    val onAmount: (String) -> Unit = {},
    val onPaidBy: (String) -> Unit = {},
    val onToggle: (String) -> Unit = {},
    val onCustom: (String, String) -> Unit = { _, _ -> },
    val onFillEqually: () -> Unit = {},
    val onSave: () -> Unit = {}
)
