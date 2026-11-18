package com.wherever.mobile.feature.plans.domain

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint

/** Cómo se mueve la persona (US-049). `apiValue` es el de la API. */
enum class Mobility(val apiValue: String) {
    WALK("a_pie"),
    BIKE("bicicleta"),
    CAR("auto"),
    TRANSIT("transporte_publico");

    companion object {
        fun from(value: String?): Mobility = entries.firstOrNull { it.apiValue == value } ?: WALK
    }
}

/** Una parada del plan. `arrives` es ISO 8601 en UTC, como lo manda la API. */
data class PlanStop(
    val id: String,
    val placeId: String,
    val name: String,
    val category: String,
    val arrives: String,
    val minutes: Int,
    val price: Int,
    val why: String? = null
)

/** Traslado entre dos paradas, ya calculado por la API según la movilidad (US-055). */
data class Transfer(val fromStopId: String, val toStopId: String, val minutes: Int, val km: Double)

/** Lo que entendió el asistente de la frase (US-041). */
data class PlanParameters(
    val company: String,
    val withKids: Boolean,
    val withPet: Boolean,
    val maxBudget: Int,
    val time: String,
    val keywords: List<String>
)

/** Plan sin guardar que propone `POST /planes/generar`. */
data class PlanDraft(
    val name: String,
    val date: String,
    val people: Int,
    val budget: Int,
    val mobility: Mobility,
    val stops: List<PlanStop>,
    val transfers: List<Transfer>,
    val totalCost: Int,
    val costPerPerson: Int,
    /** `ia` o `reglas`. Con `reglas`, la IA no respondió a tiempo (US-039). */
    val source: String,
    val notice: String?,
    val parameters: PlanParameters?
)

data class Plan(
    val id: String,
    val name: String,
    val date: String,
    val people: Int,
    val budget: Int,
    val mobility: Mobility,
    val stops: List<PlanStop>,
    val transfers: List<Transfer>,
    val totalCost: Int,
    val costPerPerson: Int,
    val isPublic: Boolean,
    val version: Int
)

enum class PlansTab(val apiValue: String) { UPCOMING("proximos"), SAVED("guardados"), PAST("pasados") }

data class PlanSummary(val id: String, val name: String, val date: String, val stops: Int, val costPerPerson: Int)

data class PopularPlan(
    val id: String,
    val name: String,
    val people: Int,
    val costPerPerson: Int,
    val copies: Int,
    val stops: List<String>
)

/** Lugar que puede reemplazar una parada (US-056). */
data class Alternative(
    val placeId: String,
    val name: String,
    val category: String,
    val distanceMeters: Double,
    val extraMinutes: Int
)

data class ShareLink(val link: String, val expiresAt: String)

/** Petición al asistente: la frase y lo que la persona ajustó en la hoja rápida (US-050). */
data class PlanRequest(
    val phrase: String,
    val near: GeoPoint,
    val dateTime: String? = null,
    val people: Int? = null,
    val mobility: Mobility? = null
)

/** El asistente de IA (backend nv-31 a nv-33) y los planes populares (nv-68). */
interface PlanAssistantRepository {
    suspend fun generate(request: PlanRequest): Result<PlanDraft>

    /** "Otra idea" (US-044): otro plan sin los lugares ya propuestos. */
    suspend fun otherIdea(request: PlanRequest, exclude: List<String>): Result<PlanDraft>

    suspend fun popular(near: GeoPoint): Result<List<PopularPlan>>
}

/** Planes guardados y su edición (backend nv-30, nv-38, nv-68). Cada cambio manda `If-Match` con la versión. */
interface PlansRepository {
    suspend fun save(draft: PlanDraft): Result<Plan>

    suspend fun list(tab: PlansTab): Result<List<PlanSummary>>

    suspend fun get(id: String): Result<Plan>

    suspend fun rename(plan: Plan, name: String): Result<Plan>

    suspend fun delete(plan: Plan): Result<Unit>

    /** Repetir (US-058): copia el plan en otra fecha. También copia planes públicos ajenos (US-033). */
    suspend fun clone(planId: String, date: String): Result<Plan>

    suspend fun addStop(plan: Plan, placeId: String, arrives: String, minutes: Int): Result<Plan>

    suspend fun reorder(plan: Plan, order: List<String>): Result<Plan>

    suspend fun moveStop(plan: Plan, stopId: String, arrives: String): Result<Plan>

    suspend fun removeStop(plan: Plan, stopId: String): Result<Plan>

    suspend fun alternatives(plan: Plan, stopId: String): Result<List<Alternative>>

    suspend fun replaceStop(plan: Plan, stopId: String, placeId: String): Result<Plan>

    suspend fun share(plan: Plan): Result<ShareLink>
}
