package com.wherever.mobile.testing

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.feature.plans.domain.Alternative
import com.wherever.mobile.feature.plans.domain.ManualPlan
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.plans.domain.PlanAssistantRepository
import com.wherever.mobile.feature.plans.domain.PlanDraft
import com.wherever.mobile.feature.plans.domain.PlanRequest
import com.wherever.mobile.feature.plans.domain.PlanStop
import com.wherever.mobile.feature.plans.domain.PlanSummary
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.plans.domain.PlansTab
import com.wherever.mobile.feature.plans.domain.PopularPlan
import com.wherever.mobile.feature.plans.domain.ShareLink

fun sampleStop(id: String, placeId: String, arrives: String) =
    PlanStop(id, placeId, "Lugar $placeId", "cafeteria", arrives, minutes = 60, price = 150)

fun sampleDraft(source: String = "ia") = PlanDraft(
    name = "Tarde de café",
    date = "2026-11-18",
    people = 2,
    budget = 600,
    mobility = Mobility.WALK,
    stops = listOf(sampleStop("s1", "n1", "2026-11-18T22:00:00Z"), sampleStop("s2", "n2", "2026-11-18T23:15:00Z")),
    transfers = emptyList(),
    totalCost = 600,
    costPerPerson = 300,
    source = source,
    notice = null,
    parameters = null
)

fun samplePlan(version: Int = 1) = Plan(
    id = "plan1",
    name = "Tarde de café",
    date = "2026-11-18",
    people = 2,
    budget = 600,
    mobility = Mobility.WALK,
    stops = listOf(
        sampleStop("s1", "n1", "2026-11-18T22:00:00Z"),
        sampleStop("s2", "n2", "2026-11-18T23:15:00Z"),
        sampleStop("s3", "n3", "2026-11-19T00:30:00Z")
    ),
    transfers = emptyList(),
    totalCost = 900,
    costPerPerson = 450,
    isPublic = false,
    version = version
)

class FakePlanAssistantRepository : PlanAssistantRepository {
    var generateResult: Result<PlanDraft> = Result.Success(sampleDraft())
    val requests = mutableListOf<PlanRequest>()
    val excluded = mutableListOf<List<String>>()

    /** Se espera antes de responder: sirve para ver los pasos de Generando y cancelar (nv-23). */
    var beforeAnswer: suspend () -> Unit = {}

    override suspend fun generate(request: PlanRequest): Result<PlanDraft> {
        requests += request
        beforeAnswer()
        return generateResult
    }

    override suspend fun otherIdea(request: PlanRequest, exclude: List<String>): Result<PlanDraft> {
        requests += request
        excluded += exclude
        return Result.Success(
            sampleDraft().copy(stops = listOf(sampleStop("s9", "n9", "2026-11-18T22:00:00Z")))
        )
    }

    override suspend fun popular(near: GeoPoint): Result<List<PopularPlan>> =
        Result.Success(listOf(PopularPlan("pop1", "Ruta de tacos", 3, 200, 12, listOf("Tacos Don Gus"))))
}

@Suppress("TooManyFunctions")
class FakePlansRepository : PlansRepository {
    var plan: Plan = samplePlan()
    var editResult: Result<Plan>? = null

    /** Si no es null, `get` lo devuelve en lugar del plan (p. ej. un error). */
    var getResult: Result<Plan>? = null
    var saveResult: Result<Plan> = Result.Success(samplePlan())
    val calls = mutableListOf<String>()
    var lastOrder: List<String> = emptyList()
    var lastArrives: String? = null
    var clonedTo: String? = null

    private fun edited(name: String): Result<Plan> {
        calls += name
        return editResult ?: Result.Success(plan.copy(version = plan.version + 1).also { plan = it })
    }

    override suspend fun save(draft: PlanDraft): Result<Plan> {
        calls += "save"
        return saveResult
    }

    override suspend fun list(tab: PlansTab): Result<List<PlanSummary>> {
        calls += "list:${tab.apiValue}"
        return Result.Success(listOf(PlanSummary("plan1", "Tarde de café", "2026-11-18", 3, 450)))
    }

    override suspend fun get(id: String): Result<Plan> {
        calls += "get"
        return getResult ?: Result.Success(plan)
    }

    override suspend fun rename(plan: Plan, name: String): Result<Plan> = edited("rename:$name")

    override suspend fun delete(plan: Plan): Result<Unit> {
        calls += "delete"
        return Result.Success(Unit)
    }

    override suspend fun clone(planId: String, date: String): Result<Plan> {
        clonedTo = date
        return Result.Success(plan.copy(id = "plan2"))
    }

    override suspend fun addStop(plan: Plan, placeId: String, arrives: String, minutes: Int): Result<Plan> {
        lastArrives = arrives
        return edited("add:$placeId")
    }

    override suspend fun reorder(plan: Plan, order: List<String>): Result<Plan> {
        lastOrder = order
        return edited("reorder")
    }

    override suspend fun moveStop(plan: Plan, stopId: String, arrives: String): Result<Plan> {
        lastArrives = arrives
        return edited("move:$stopId")
    }

    override suspend fun removeStop(plan: Plan, stopId: String): Result<Plan> = edited("remove:$stopId")

    val alternativeQueries = mutableListOf<Pair<String, String?>>()
    var created: ManualPlan? = null
    var createResult: Result<Plan>? = null

    override suspend fun alternatives(plan: Plan, stopId: String, query: String?): Result<List<Alternative>> {
        alternativeQueries += stopId to query
        return Result.Success(listOf(Alternative("n7", "Café Nuevo", "cafeteria", 300.0, 4)))
    }

    override suspend fun create(plan: ManualPlan): Result<Plan> {
        calls += "create"
        created = plan
        return createResult ?: Result.Success(samplePlan().copy(id = "manual1"))
    }

    override suspend fun replaceStop(plan: Plan, stopId: String, placeId: String): Result<Plan> =
        edited("replace:$stopId:$placeId")

    override suspend fun share(plan: Plan): Result<ShareLink> =
        Result.Success(ShareLink("enlace-de-prueba/plan#codigo=abc", "2026-11-25T00:00:00Z"))
}
