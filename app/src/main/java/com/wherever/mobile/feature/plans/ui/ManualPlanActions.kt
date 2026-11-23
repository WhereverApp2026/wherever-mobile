package com.wherever.mobile.feature.plans.ui

import com.wherever.mobile.feature.places.domain.PlaceCard
import java.time.LocalDate
import java.time.LocalTime

/** Eventos de CrearPlan hacia [ManualPlanViewModel]. */
internal data class ManualPlanActions(
    val onBack: () -> Unit = {},
    val onName: (String) -> Unit = {},
    val onCompany: (Company) -> Unit = {},
    val onDate: (LocalDate) -> Unit = {},
    val onTime: (LocalTime) -> Unit = {},
    val onPrice: (PriceLevel) -> Unit = {},
    val onReorder: () -> Unit = {},
    val onMoveUp: (Int) -> Unit = {},
    val onMoveDown: (Int) -> Unit = {},
    val onRemove: (Int) -> Unit = {},
    val onOpenSearch: () -> Unit = {},
    val onCloseSearch: () -> Unit = {},
    val onSearch: (String) -> Unit = {},
    val onPick: (PlaceCard) -> Unit = {},
    val onPublic: (Boolean) -> Unit = {},
    val onCreate: () -> Unit = {}
)
