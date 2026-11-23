package com.wherever.mobile.feature.checkin.ui

/** A dónde va la llegada al terminar. */
data class ArrivalNavigation(val onBack: () -> Unit, val onPostcard: () -> Unit, val onNext: () -> Unit)
