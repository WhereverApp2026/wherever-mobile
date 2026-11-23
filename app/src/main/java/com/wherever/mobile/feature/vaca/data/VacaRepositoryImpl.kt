package com.wherever.mobile.feature.vaca.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.vaca.domain.Debt
import com.wherever.mobile.feature.vaca.domain.DebtStatus
import com.wherever.mobile.feature.vaca.domain.Expense
import com.wherever.mobile.feature.vaca.domain.ExpenseConcept
import com.wherever.mobile.feature.vaca.domain.NewExpense
import com.wherever.mobile.feature.vaca.domain.PersonBalance
import com.wherever.mobile.feature.vaca.domain.SplitMode
import com.wherever.mobile.feature.vaca.domain.VacaRepository
import com.wherever.mobile.feature.vaca.domain.VacaSummary
import javax.inject.Inject
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/** Gastos que se muestran en «Quién pagó» (la API pagina de 50 en 50 como máximo). */
private const val EXPENSES_LIMIT = 50

/** La vaca contra la API real (specs/nv-26). No guarda nada en el teléfono: la API recalcula el corte. */
class VacaRepositoryImpl @Inject constructor(private val api: VacaApi) : VacaRepository {
    override suspend fun summary(groupId: String): Result<VacaSummary> = safeApiCall {
        val vaca = api.vaca(groupId)
        val expenses = api.expenses(groupId, EXPENSES_LIMIT)
        VacaSummary(
            totalCents = vaca.total,
            people = vaca.people.map { PersonBalance(it.userId, it.name, it.put, it.share, it.balance) },
            debts = vaca.debts.map {
                Debt(
                    id = it.id,
                    debtorId = it.debtor.userId(),
                    creditorId = it.creditor.userId(),
                    amountCents = it.amount,
                    status = if (it.status == "pagada") DebtStatus.PAID else DebtStatus.PENDING
                )
            },
            expenses = expenses.items.map {
                Expense(
                    id = it.id,
                    amountCents = it.amount,
                    concept = ExpenseConcept.from(it.concept),
                    description = it.description?.takeIf { d -> d.isNotBlank() },
                    paidBy = it.paidBy?.userId().orEmpty()
                )
            }
        )
    }

    override suspend fun addExpense(groupId: String, expense: NewExpense): Result<Unit> = safeApiCall {
        api.addExpense(
            groupId,
            NewExpenseDto(
                amount = expense.amountCents,
                concept = expense.concept.apiValue,
                description = expense.description.trim().takeIf { it.isNotEmpty() },
                paidBy = expense.paidBy,
                splitBetween = expense.splitBetween,
                mode = expense.mode.apiValue,
                custom = if (expense.mode == SplitMode.CUSTOM) {
                    expense.splitBetween.map { CustomAmountDto(it, expense.customCents[it] ?: 0) }
                } else {
                    null
                },
                planId = expense.planId,
                clientId = expense.clientId
            )
        )
    }

    override suspend fun markPaid(debtId: String): Result<Unit> =
        safeApiCall { api.payDebt(debtId, PayDebtDto("pagada")) }
}

/** Id de un usuario que llega como texto o como objeto `{id}` / `{usuario_id}`. */
private fun JsonElement.userId(): String = when (this) {
    is JsonPrimitive -> contentOrNull.orEmpty()
    is JsonObject -> (this["id"] ?: this["usuario_id"])?.jsonPrimitive?.contentOrNull.orEmpty()
    else -> ""
}
