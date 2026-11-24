package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.feature.business.domain.Address
import com.wherever.mobile.feature.business.domain.DaySchedule
import com.wherever.mobile.feature.business.domain.DocumentType

// Eventos que las pantallas de negocio envían a su ViewModel (sin lógica en los Composables).

/** Eventos del alta (43 a 47). Con valores vacíos para las vistas previas. */
internal data class OnboardingActions(
    val onBack: () -> Unit = {},
    val onNext: () -> Unit = {},
    val onSaveExit: () -> Unit = {},
    val onName: (String) -> Unit = {},
    val onCategory: (String) -> Unit = {},
    val onDescription: (String) -> Unit = {},
    val onAiWords: (String) -> Unit = {},
    val onSuggest: () -> Unit = {},
    val onAddress: (Address) -> Unit = {},
    val onLocate: () -> Unit = {},
    val onSchedule: (DaySchedule) -> Unit = {},
    val onPriceLevel: (Int) -> Unit = {},
    val onAveragePrice: (String) -> Unit = {},
    val onToggleService: (String) -> Unit = {},
    val onAddPhoto: () -> Unit = {},
    val onDataConsent: (Boolean) -> Unit = {},
    val onDocument: (DocumentType) -> Unit = {},
    val onClabe: (String) -> Unit = {},
    val onHolder: (String) -> Unit = {},
    val onPanel: () -> Unit = {},
    val onEdit: () -> Unit = {},
    val onRetry: () -> Unit = {}
)
