package com.wherever.mobile.feature.memories.ui

import com.wherever.mobile.feature.memories.domain.PostcardStyle

// Eventos que las pantallas de recuerdos envían a su ViewModel (sin lógica en los Composables).

/** Eventos de 72_Postal. */
internal data class PostcardEditorActions(
    val onBack: () -> Unit,
    val onStyle: (PostcardStyle) -> Unit,
    val onText: (String) -> Unit,
    val onSave: () -> Unit,
    val onShare: () -> Unit
)

/** Eventos de 75_PostalVer. */
internal data class PostcardDetailActions(
    val onBack: () -> Unit,
    val onShare: () -> Unit,
    val onDownload: () -> Unit,
    val onDelete: () -> Unit,
    val onRetry: () -> Unit
)
