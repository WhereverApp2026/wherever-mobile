package com.wherever.mobile.feature.business.ui

/** Eventos de 52_NegResp. */
internal data class ReviewReplyActions(
    val onBack: () -> Unit = {},
    val onText: (String) -> Unit = {},
    val onQuick: (String) -> Unit = {},
    val onSuggest: () -> Unit = {},
    val onPublish: () -> Unit = {}
)
