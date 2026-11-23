package com.wherever.mobile.testing

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.groups.domain.GroupMember
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.vaca.domain.NewExpense
import com.wherever.mobile.feature.vaca.domain.VacaRepository
import com.wherever.mobile.feature.vaca.domain.VacaSummary

/** Grupo de la maqueta de 28_Vaca: Ana (yo), Luis, Mariana, Diego y alguien que no confirmó. */
fun vacaGroup() = sampleGroup().copy(
    members = listOf(
        GroupMember("u2", "Luis Pérez", null, false, MemberStatus.CONFIRMED),
        GroupMember("u1", "Ana López", null, true, MemberStatus.CONFIRMED),
        GroupMember("u3", "Mariana", null, false, MemberStatus.CONFIRMED),
        GroupMember("u4", "Diego", null, false, MemberStatus.CONFIRMED),
        GroupMember("u5", "Pendiente", null, false, MemberStatus.INVITED)
    )
)

/** Vaca en memoria: guarda cada gasto y cada deuda marcada. */
class FakeVacaRepository(var summaryResult: Result<VacaSummary>) : VacaRepository {
    val added = mutableListOf<NewExpense>()
    val paid = mutableListOf<String>()
    var addResult: Result<Unit> = Result.Success(Unit)
    var payResult: Result<Unit> = Result.Success(Unit)
    var summaryCalls = 0

    override suspend fun summary(groupId: String): Result<VacaSummary> = summaryResult.also { summaryCalls++ }

    override suspend fun addExpense(groupId: String, expense: NewExpense): Result<Unit> =
        addResult.also { added += expense }

    override suspend fun markPaid(debtId: String): Result<Unit> = payResult.also { paid += debtId }
}
