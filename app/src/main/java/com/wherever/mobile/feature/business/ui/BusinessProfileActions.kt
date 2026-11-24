package com.wherever.mobile.feature.business.ui

/** Eventos de 58_NegPerfil y de sus hojas (pausa y eliminar). */
internal data class BusinessProfileActions(
    val onRetry: () -> Unit = {},
    val onAccepts: (Boolean) -> Unit = {},
    val onOpenPause: () -> Unit = {},
    val onClosePause: () -> Unit = {},
    val onPauseLength: (PauseLength) -> Unit = {},
    val onPauseReason: (String) -> Unit = {},
    val onTogglePause: () -> Unit = {},
    val onExplore: () -> Unit = {},
    val onLogout: () -> Unit = {},
    val onOpenDelete: () -> Unit = {},
    val onCloseDelete: () -> Unit = {},
    val onDeleteText: (String) -> Unit = {},
    val onDelete: () -> Unit = {},
    val onMessageShown: () -> Unit = {}
)
