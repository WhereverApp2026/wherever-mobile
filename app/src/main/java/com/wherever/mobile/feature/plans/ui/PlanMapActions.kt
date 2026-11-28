package com.wherever.mobile.feature.plans.ui

/** Lo que pueden pedir 13_Mapa y 14_MapaFull; la navegación y la app de mapas las resuelve la pantalla. */
data class PlanMapActions(
    val onBack: () -> Unit = {},
    val onFull: () -> Unit = {},
    val onDirections: () -> Unit = {},
    val onStartRoute: () -> Unit = {},
    val onRetry: () -> Unit = {}
)
