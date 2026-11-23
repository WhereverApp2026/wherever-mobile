// MagicNumber: datos de ejemplo de las vistas previas y capturas de 39, 40, 41 y CrearPlan, copiados de la maqueta.
// No se usan fuera de @Preview ni llegan a la app.
@file:Suppress("MagicNumber")

package com.wherever.mobile.feature.plans.ui

import com.wherever.mobile.feature.plans.domain.Alternative
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlanStop
import com.wherever.mobile.feature.plans.domain.Transfer
import java.time.ZoneId

/** Las capturas fijan la zona para que las horas no dependan del equipo (Windows en local, Linux en CI). */
internal val PREVIEW_ZONE: ZoneId = ZoneId.of("America/Mexico_City")

internal fun samplePlanDetail() = Plan(
    id = "plan1",
    name = "Plan en pareja",
    date = "2026-10-18",
    people = 2,
    budget = 500,
    mobility = Mobility.WALK,
    stops = listOf(
        PlanStop("s1", "n1", "Café Raíz", "cafeteria", "2026-10-18T15:00:00Z", 90, 180, priceLevel = 2),
        PlanStop("s2", "n2", "Jardín del Cerro", "entretenimiento", "2026-10-18T17:00:00Z", 120, 0),
        PlanStop("s3", "n3", "Fonda Doña Meche", "restaurante", "2026-10-18T20:00:00Z", 90, 240, priceLevel = 1)
    ),
    transfers = listOf(Transfer("s1", "s2", 8, 0.65), Transfer("s2", "s3", 12, 4.2)),
    totalCost = 840,
    costPerPerson = 420,
    isPublic = false,
    version = 3
)

internal fun sampleFamilyPlan() = samplePlanDetail().copy(
    name = "Paseo familiar",
    date = "2026-10-19",
    stops = listOf(
        PlanStop("s1", "n4", "Mercado de Tula", "comercio", "2026-10-19T16:00:00Z", 90, 120, priceLevel = 1),
        PlanStop("s2", "n5", "Parque Ecológico", "entretenimiento", "2026-10-19T18:00:00Z", 120, 0),
        PlanStop("s3", "n6", "Fonda Doña Lupe", "restaurante", "2026-10-19T21:00:00Z", 90, 190, priceLevel = 1)
    ),
    costPerPerson = 310
)

internal fun sampleAlternatives() = listOf(
    Alternative("n7", "Mirador", "entretenimiento", 650.0, 10),
    Alternative("n5", "Parque Ecológico", "entretenimiento", 1200.0, 6),
    Alternative("n4", "Mercado de Tula", "comercio", 900.0, 12, priceLevel = 1)
)
