// MagicNumber: el ejemplo de la maqueta ($760 entre 4, $190; Jorge debe $70) para las vistas previas y capturas.
@file:Suppress("MagicNumber")

package com.wherever.mobile.feature.vaca.ui

import com.wherever.mobile.feature.vaca.domain.Debt
import com.wherever.mobile.feature.vaca.domain.DebtStatus
import com.wherever.mobile.feature.vaca.domain.Expense
import com.wherever.mobile.feature.vaca.domain.ExpenseConcept
import com.wherever.mobile.feature.vaca.domain.PersonBalance
import com.wherever.mobile.feature.vaca.domain.VacaSummary

internal fun sampleVacaSummary(paid: Set<String> = emptySet()) = VacaSummary(
    totalCents = 76_000,
    people = listOf(
        PersonBalance("u1", "Osmar", 64_000, 19_000, 45_000),
        PersonBalance("u2", "Jorge", 12_000, 19_000, -7_000),
        PersonBalance("u3", "Mariana", 0, 19_000, -19_000),
        PersonBalance("u4", "Diego", 0, 19_000, -19_000)
    ),
    debts = listOf(
        Debt("d1", "u3", "u1", 19_000, if ("d1" in paid) DebtStatus.PAID else DebtStatus.PENDING),
        Debt("d2", "u4", "u1", 19_000, if ("d2" in paid) DebtStatus.PAID else DebtStatus.PENDING),
        Debt("d3", "u2", "u1", 7_000, if ("d3" in paid) DebtStatus.PAID else DebtStatus.PENDING)
    ),
    expenses = listOf(
        Expense("e1", 64_000, ExpenseConcept.FOOD, "la comida", "u1"),
        Expense("e2", 12_000, ExpenseConcept.TRANSPORT, "el transporte", "u2")
    )
)

internal fun sampleVacaState(paid: Set<String> = emptySet()) = VacaUiState(
    loading = false,
    summary = sampleVacaSummary(paid),
    myId = "u1",
    title = "Viernes de tacos"
)

internal fun sampleExpenseState() = AddExpenseUiState(
    loading = false,
    title = "Viernes de tacos",
    members = listOf(
        ExpenseMember("u1", "Osmar"),
        ExpenseMember("u2", "Jorge"),
        ExpenseMember("u3", "Mariana"),
        ExpenseMember("u4", "Diego")
    ),
    myId = "u1",
    description = "Tacos y bebidas",
    amount = "480",
    paidBy = "u1",
    splitBetween = setOf("u1", "u2", "u3", "u4")
)
