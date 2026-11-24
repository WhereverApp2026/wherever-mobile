package com.wherever.mobile.feature.business.ui

/** Eventos de 90_NegFiscal. */
internal data class TaxDataActions(
    val onBack: () -> Unit = {},
    val onRfc: (String) -> Unit = {},
    val onLegalName: (String) -> Unit = {},
    val onRegime: (String) -> Unit = {},
    val onCfdiUse: (String) -> Unit = {},
    val onPostalCode: (String) -> Unit = {},
    val onSave: () -> Unit = {}
)
