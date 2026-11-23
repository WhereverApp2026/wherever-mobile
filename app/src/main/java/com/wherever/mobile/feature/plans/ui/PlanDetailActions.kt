package com.wherever.mobile.feature.plans.ui

import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.plans.domain.Alternative
import com.wherever.mobile.feature.plans.domain.PlanStop

/** Eventos de 39_Plan, 40_Cambiar y 41_AgregarParada hacia [PlanDetailViewModel]. */
internal data class PlanDetailActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onShare: () -> Unit = {},
    val onRepeat: () -> Unit = {},
    val onRename: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onChange: (PlanStop) -> Unit = {},
    val onRemove: (PlanStop) -> Unit = {},
    val onMoveUp: (PlanStop) -> Unit = {},
    val onMoveDown: (PlanStop) -> Unit = {},
    val onShift: (PlanStop, Boolean) -> Unit = { _, _ -> },
    val onCloseChange: () -> Unit = {},
    val onSelectAlternative: (Alternative) -> Unit = {},
    val onSearchAlternative: () -> Unit = {},
    val onAlternativeQuery: (String) -> Unit = {},
    val onReplace: () -> Unit = {},
    val onRemoveFromSheet: () -> Unit = {},
    val onAddStop: () -> Unit = {},
    val onCloseAdd: () -> Unit = {},
    val onAddTab: (AddStopTab) -> Unit = {},
    val onAddQuery: (String) -> Unit = {},
    val onAddSuggestion: (Alternative) -> Unit = {},
    val onAddPlace: (PlaceCard) -> Unit = {},
    /** «Cómo llegar»: abre la app de mapas del teléfono (D25, specs/nv-30). */
    val onDirections: (PlanStop) -> Unit = {},
    /** «Ya llegué»: check-in de la parada (specs/nv-30). */
    val onArrived: (PlanStop) -> Unit = {}
)
