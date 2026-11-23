package com.wherever.mobile.feature.plans.ui

/** Parada a la que se llega («Ya llegué»), con la siguiente para 26_CheckinOk (specs/nv-30). */
data class ArrivalTarget(
    val placeId: String,
    val placeName: String,
    val planId: String,
    val nextName: String?,
    val nextMinutes: Int?
)
