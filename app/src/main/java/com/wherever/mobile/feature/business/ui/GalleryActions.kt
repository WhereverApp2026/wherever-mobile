package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.feature.business.domain.BusinessPhoto

/** Eventos de 87_NegGaleria y 88_NegAgregarFoto. */
internal data class GalleryActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onMove: (Int, Int) -> Unit = { _, _ -> },
    val onCover: (BusinessPhoto) -> Unit = {},
    val onDelete: (BusinessPhoto) -> Unit = {},
    val onOpenAdd: () -> Unit = {},
    val onCloseAdd: () -> Unit = {},
    val onCamera: () -> Unit = {},
    val onGallery: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onMessageShown: () -> Unit = {}
)
