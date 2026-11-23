package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.feature.business.domain.Reservation
import com.wherever.mobile.feature.business.domain.ScheduleBlock

/** Eventos de 49_NegReservas hacia su ViewModel (y a 50_NegCheckin). */
internal data class ReservationsActions(
    val onTab: (ReservationsTab) -> Unit = {},
    val onNextDay: () -> Unit = {},
    val onPreviousDay: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onOpen: (Reservation) -> Unit = {},
    val onDismiss: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onArrived: () -> Unit = {},
    val onNoShow: () -> Unit = {},
    val onCheckin: (String?) -> Unit = {},
    val onOpenContact: () -> Unit = {},
    val onContactText: (String) -> Unit = {},
    val onSendContact: () -> Unit = {},
    val onOpenBlock: () -> Unit = {},
    val onCloseBlock: () -> Unit = {},
    val onBlockForm: (BlockForm) -> Unit = {},
    val onSaveBlock: () -> Unit = {},
    val onUnblock: (ScheduleBlock) -> Unit = {},
    val onMessageShown: () -> Unit = {}
)
