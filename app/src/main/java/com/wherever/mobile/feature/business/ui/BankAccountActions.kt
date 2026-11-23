package com.wherever.mobile.feature.business.ui

/** Eventos de 89_NegBanco. */
internal data class BankAccountActions(
    val onBack: () -> Unit = {},
    val onHolder: (String) -> Unit = {},
    val onClabe: (String) -> Unit = {},
    val onSave: () -> Unit = {}
)
