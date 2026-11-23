package com.wherever.mobile.feature.plans.ui

import com.wherever.mobile.feature.plans.domain.Mobility
import java.time.LocalDate
import java.time.LocalTime

/** Eventos de IAPalabra, 17_AjustaPlan, Generando y el plan propuesto hacia [CreatePlanViewModel]. */
internal data class CreatePlanActions(
    val onBack: () -> Unit = {},
    val onPhrase: (String) -> Unit = {},
    val onQuickWord: (String) -> Unit = {},
    val onAdjust: () -> Unit = {},
    val onBackToWord: () -> Unit = {},
    /** «¿Prefieres hacerlo tú? Crear plan a mano» (CrearPlan, nv-24). */
    val onManual: () -> Unit = {},
    val onCompany: (Company) -> Unit = {},
    val onDate: (LocalDate) -> Unit = {},
    val onTime: (LocalTime) -> Unit = {},
    val onPeople: (Int) -> Unit = {},
    val onStops: (Int) -> Unit = {},
    val onPrice: (PriceLevel) -> Unit = {},
    val onMobility: (Mobility) -> Unit = {},
    val onMood: (Mood) -> Unit = {},
    val onGenerate: () -> Unit = {},
    val onCancel: () -> Unit = {},
    val onShowPlan: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onOtherIdea: () -> Unit = {},
    val onEdit: () -> Unit = {}
)
