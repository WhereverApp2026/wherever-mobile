package com.wherever.mobile.feature.plans.domain

import java.time.Duration
import java.time.Instant

/** Una parada del plan con su número (1, 2, 3…) y sus coordenadas de `GET /lugares/{id}` (specs/nv-30, D-30f). */
data class RoutePoint(val number: Int, val stop: PlanStop, val lat: Double, val lng: Double)

/** Totales de 13_Mapa y 14_MapaFull (D-30i): todo sale de la API, nada se estima. */
data class RouteTotals(val transferMinutes: Int, val km: Double, val spanMinutes: Long?)

/** El traslado que llega a la parada `stopId`, si la API lo dio. */
fun List<Transfer>.into(stopId: String): Transfer? = firstOrNull { it.toStopId == stopId }

/**
 * Suma los traslados y mide de la llegada a la primera parada al fin de la última (`llega` + `minutos`).
 * Sin fechas legibles, el lapso queda en null y la UI no lo muestra.
 */
fun routeTotals(stops: List<PlanStop>, transfers: List<Transfer>): RouteTotals {
    val start = stops.firstOrNull()?.let { parse(it.arrives) }
    val end = stops.lastOrNull()?.let { last -> parse(last.arrives)?.plus(Duration.ofMinutes(last.minutes.toLong())) }
    val span = if (start != null &&
        end != null &&
        end.isAfter(start)
    ) {
        Duration.between(start, end).toMinutes()
    } else {
        null
    }
    return RouteTotals(transfers.sumOf { it.minutes }, transfers.sumOf { it.km }, span)
}

private fun parse(iso: String): Instant? = runCatching { Instant.parse(iso) }.getOrNull()

/** `travelmode` de la URL de mapas según la movilidad del plan (D-30g). */
fun Mobility.travelMode(): String = when (this) {
    Mobility.WALK -> "walking"
    Mobility.BIKE -> "bicycling"
    Mobility.CAR -> "driving"
    Mobility.TRANSIT -> "transit"
}

/** «Iniciar ruta»: todas las paradas en orden en la app de mapas del teléfono (D-30g). */
fun routeUrl(points: List<RoutePoint>, mobility: Mobility): String? {
    if (points.isEmpty()) return null
    val coord = { p: RoutePoint -> "${p.lat},${p.lng}" }
    val destination = coord(points.last())
    val waypoints = points.dropLast(1).joinToString("|") { coord(it) }
    return buildString {
        append("https://www.google.com/maps/dir/?api=1&destination=").append(destination)
        if (waypoints.isNotEmpty()) append("&waypoints=").append(waypoints)
        append("&travelmode=").append(mobility.travelMode())
    }
}
