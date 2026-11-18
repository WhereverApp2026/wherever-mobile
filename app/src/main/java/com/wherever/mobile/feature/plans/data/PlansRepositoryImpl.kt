package com.wherever.mobile.feature.plans.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.places.data.PlacesApi
import com.wherever.mobile.feature.plans.domain.Alternative
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlanAssistantRepository
import com.wherever.mobile.feature.plans.domain.PlanDraft
import com.wherever.mobile.feature.plans.domain.PlanParameters
import com.wherever.mobile.feature.plans.domain.PlanRequest
import com.wherever.mobile.feature.plans.domain.PlanStop
import com.wherever.mobile.feature.plans.domain.PlanSummary
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.plans.domain.PlansTab
import com.wherever.mobile.feature.plans.domain.PopularPlan
import com.wherever.mobile.feature.plans.domain.ShareLink
import com.wherever.mobile.feature.plans.domain.Transfer
import javax.inject.Inject

private const val LIST_LIMIT = 30
private const val DATE_LENGTH = 10

class PlanAssistantRepositoryImpl @Inject constructor(private val api: PlansApi) : PlanAssistantRepository {
    override suspend fun generate(request: PlanRequest): Result<PlanDraft> =
        safeApiCall { api.generate(request.toDto(exclude = null)).toDraft() }

    override suspend fun otherIdea(request: PlanRequest, exclude: List<String>): Result<PlanDraft> =
        safeApiCall { api.otherIdea(request.toDto(exclude)).toDraft() }

    override suspend fun popular(near: GeoPoint): Result<List<PopularPlan>> = safeApiCall {
        api.popular(near.lat, near.lng).items.map { dto ->
            PopularPlan(
                dto.id,
                dto.name,
                dto.people,
                dto.costPerPerson,
                dto.copies,
                dto.stops.mapNotNull {
                    it.place?.name
                }
            )
        }
    }
}

private fun PlanRequest.toDto(exclude: List<String>?) = GenerateRequestDto(
    phrase = phrase.trim(),
    lat = near.lat,
    lng = near.lng,
    dateTime = dateTime,
    people = people,
    mobility = mobility?.apiValue,
    exclude = exclude
)

private fun GenerateResponseDto.toDraft(): PlanDraft = PlanDraft(
    name = plan.name,
    date = plan.date.take(DATE_LENGTH),
    people = plan.people,
    budget = plan.budget,
    mobility = Mobility.from(plan.mobility),
    stops = plan.stops.map { it.toDomain(it.name.orEmpty(), it.category.orEmpty()) },
    transfers = plan.transfers.map { it.toDomain() },
    totalCost = plan.totalCost,
    costPerPerson = plan.costPerPerson,
    source = source,
    notice = notice?.takeIf { it.isNotBlank() },
    parameters = parameters?.let {
        PlanParameters(it.company.orEmpty(), it.withKids, it.withPet, it.maxBudget, it.time.orEmpty(), it.keywords)
    }
)

private fun StopDto.toDomain(name: String, category: String) = PlanStop(
    id = id,
    placeId = placeId,
    name = name,
    category = category,
    arrives = arrives,
    minutes = minutes,
    price = price,
    why = why?.takeIf { it.isNotBlank() }
)

private fun TransferDto.toDomain() = Transfer(from, to, minutes, km)

private fun ifMatch(plan: Plan) = "\"${plan.version}\""

/**
 * Los planes guardados traen solo `negocio_id` en cada parada: el nombre y el giro salen de
 * `GET /lugares/{id}`, con un caché en memoria para no repetir la ficha al editar.
 */
class PlansRepositoryImpl @Inject constructor(private val api: PlansApi, private val places: PlacesApi) :
    PlansRepository {
    private val names = mutableMapOf<String, Pair<String, String>>()

    override suspend fun save(draft: PlanDraft): Result<Plan> = safeApiCall {
        draft.stops.forEach { names[it.placeId] = it.name to it.category }
        api.create(
            CreatePlanDto(
                name = draft.name,
                date = draft.date,
                people = draft.people,
                budget = draft.budget,
                mobility = draft.mobility.apiValue,
                origin = draft.source.takeIf { it == "ia" || it == "reglas" } ?: "ia",
                stops = draft.stops.map { CreateStopDto(it.placeId, it.arrives, it.minutes, it.price) }
            )
        ).toPlan()
    }

    override suspend fun list(tab: PlansTab): Result<List<PlanSummary>> = safeApiCall {
        api.list(tab.apiValue, LIST_LIMIT).items.map {
            PlanSummary(it.id, it.name, it.date.take(DATE_LENGTH), it.stops.size, it.costPerPerson)
        }
    }

    override suspend fun get(id: String): Result<Plan> = safeApiCall { api.get(id).toPlan() }

    override suspend fun rename(plan: Plan, name: String): Result<Plan> =
        safeApiCall { api.patch(plan.id, ifMatch(plan), PlanPatchDto(name = name.trim())).toPlan() }

    override suspend fun delete(plan: Plan): Result<Unit> = safeApiCall { api.delete(plan.id, ifMatch(plan)) }

    override suspend fun clone(planId: String, date: String): Result<Plan> =
        safeApiCall { api.clone(planId, CloneDto(date)).toPlan() }

    override suspend fun addStop(plan: Plan, placeId: String, arrives: String, minutes: Int): Result<Plan> =
        safeApiCall {
            api.addStop(plan.id, ifMatch(plan), CreateStopDto(placeId, arrives, minutes, price = 0)).toPlan()
        }

    override suspend fun reorder(plan: Plan, order: List<String>): Result<Plan> =
        safeApiCall { api.reorder(plan.id, ifMatch(plan), OrderDto(order)).toPlan() }

    override suspend fun moveStop(plan: Plan, stopId: String, arrives: String): Result<Plan> =
        safeApiCall { api.patchStop(plan.id, stopId, ifMatch(plan), StopPatchDto(arrives)).toPlan() }

    override suspend fun removeStop(plan: Plan, stopId: String): Result<Plan> =
        safeApiCall { api.removeStop(plan.id, stopId, ifMatch(plan)).toPlan() }

    override suspend fun alternatives(plan: Plan, stopId: String): Result<List<Alternative>> = safeApiCall {
        api.alternatives(plan.id, stopId).items.map {
            names[it.place.id] = it.place.name to it.place.category
            Alternative(it.place.id, it.place.name, it.place.category, it.distanceMeters, it.extraMinutes)
        }
    }

    override suspend fun replaceStop(plan: Plan, stopId: String, placeId: String): Result<Plan> =
        safeApiCall { api.replaceStop(plan.id, stopId, ifMatch(plan), ReplaceStopDto(placeId)).toPlan() }

    override suspend fun share(plan: Plan): Result<ShareLink> =
        safeApiCall { api.share(plan.id).let { ShareLink(it.link, it.expiresAt) } }

    private suspend fun PlanDto.toPlan(): Plan {
        val stopsWithNames = stops.map { stop ->
            val (name, category) = names[stop.placeId] ?: lookup(stop.placeId)
            stop.toDomain(name, category)
        }
        return Plan(
            id = id,
            name = name,
            date = date.take(DATE_LENGTH),
            people = people,
            budget = budget,
            mobility = Mobility.from(mobility),
            stops = stopsWithNames,
            transfers = transfers.map { it.toDomain() },
            totalCost = totalCost,
            costPerPerson = costPerPerson,
            isPublic = visibility == "publico",
            version = version
        )
    }

    /** Si la ficha falla (negocio pausado o dado de baja) la parada queda sin nombre, no rompe el plan. */
    private suspend fun lookup(placeId: String): Pair<String, String> {
        val found = (safeApiCall { places.detail(placeId) } as? Result.Success)?.data
        val pair = (found?.name.orEmpty()) to (found?.category.orEmpty())
        if (found != null) names[placeId] = pair
        return pair
    }
}
