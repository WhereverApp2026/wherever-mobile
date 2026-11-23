package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.feature.business.domain.CouponType
import java.time.LocalDate

/** Eventos de 54_NegCrear. */
internal data class CouponFormActions(
    val onBack: () -> Unit = {},
    val onType: (CouponType) -> Unit = {},
    val onValue: (String) -> Unit = {},
    val onTitle: (String) -> Unit = {},
    val onSuggest: () -> Unit = {},
    val onDay: (Int) -> Unit = {},
    val onHoursFrom: (String) -> Unit = {},
    val onHoursTo: (String) -> Unit = {},
    val onEnd: (LocalDate) -> Unit = {},
    val onLimit: (Int) -> Unit = {},
    val onPublish: () -> Unit = {}
)
