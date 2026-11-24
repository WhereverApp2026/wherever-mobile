package com.wherever.mobile.feature.business.ui

/** A dónde lleva cada fila de 58_NegPerfil. */
internal data class ProfileNavigation(
    val onData: () -> Unit = {},
    val onGallery: () -> Unit = {},
    val onPayments: () -> Unit = {},
    val onCapacity: () -> Unit = {},
    val onTax: (Boolean?) -> Unit = {},
    val onExit: (ProfileExit) -> Unit = {}
)
