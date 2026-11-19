package com.wherever.mobile.feature.search.ui

/** Destinos que abre Buscar. */
data class SearchActions(
    val onBack: () -> Unit,
    val onPlace: (String) -> Unit,
    val onPerson: (String) -> Unit,
    val onPlan: (String) -> Unit,
    val onAskAi: (String) -> Unit,
    val onExplore: () -> Unit
)
