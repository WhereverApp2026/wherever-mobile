package com.wherever.mobile.feature.plans.ui

import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlanStop
import com.wherever.mobile.feature.plans.domain.RoutePoint
import com.wherever.mobile.feature.plans.domain.Transfer
import com.wherever.mobile.feature.plans.domain.routeTotals

// Datos solo para las vistas previas y las capturas: los textos de la maqueta (13_Mapa y 14_MapaFull).

private const val PREVIEW_LAT = 20.0517
private const val PREVIEW_LNG = -99.3417
private const val PREVIEW_STEP = 0.004
private const val PREVIEW_STAY = 90
private const val PREVIEW_PRICE = 150
private const val PREVIEW_WALK_MIN = 8
private const val PREVIEW_CAR_MIN = 12
private const val PREVIEW_WALK_KM = 0.6
private const val PREVIEW_CAR_KM = 4.3
private const val PREVIEW_BUDGET = 600
private const val PREVIEW_PER_PERSON = 300

private val PREVIEW_STOPS = listOf(
    PlanStop("s1", "p1", "Café Raíz", "cafeteria", "2026-10-11T09:00:00Z", PREVIEW_STAY, PREVIEW_PRICE),
    PlanStop("s2", "p2", "Jardín del Cerro", "parque", "2026-10-11T11:00:00Z", PREVIEW_STAY, 0),
    PlanStop("s3", "p3", "Fonda Doña Meche", "restaurante", "2026-10-11T14:00:00Z", PREVIEW_STAY, PREVIEW_PRICE)
)

private val PREVIEW_TRANSFERS = listOf(
    Transfer("s1", "s2", PREVIEW_WALK_MIN, PREVIEW_WALK_KM),
    Transfer("s2", "s3", PREVIEW_CAR_MIN, PREVIEW_CAR_KM)
)

internal fun previewMapState(full: Boolean) = PlanMapUiState(
    loading = false,
    full = full,
    plan = Plan(
        id = "plan1",
        name = "Plan en pareja · sáb 11 oct",
        date = "2026-10-11",
        people = 2,
        budget = PREVIEW_BUDGET,
        mobility = Mobility.WALK,
        stops = PREVIEW_STOPS,
        transfers = PREVIEW_TRANSFERS,
        totalCost = PREVIEW_BUDGET,
        costPerPerson = PREVIEW_PER_PERSON,
        isPublic = false,
        version = 1
    ),
    points = PREVIEW_STOPS.mapIndexed { i, stop ->
        RoutePoint(i + 1, stop, PREVIEW_LAT + i * PREVIEW_STEP, PREVIEW_LNG + i * PREVIEW_STEP)
    },
    totals = routeTotals(PREVIEW_STOPS, PREVIEW_TRANSFERS)
)
