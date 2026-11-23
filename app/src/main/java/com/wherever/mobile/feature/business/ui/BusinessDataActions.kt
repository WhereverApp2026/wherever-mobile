package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.feature.business.domain.DaySchedule

/** Eventos de «Datos y horario». */
internal data class BusinessDataActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onDescription: (String) -> Unit = {},
    val onPhone: (String) -> Unit = {},
    val onSchedule: (DaySchedule) -> Unit = {},
    val onSave: () -> Unit = {}
)
