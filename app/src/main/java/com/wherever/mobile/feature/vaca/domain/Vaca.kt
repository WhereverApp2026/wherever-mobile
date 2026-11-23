package com.wherever.mobile.feature.vaca.domain

import com.wherever.mobile.core.common.Result

/** Monto máximo de un gasto: 100 000 pesos (`monto_centavos` de 1 a 10 000 000). */
const val EXPENSE_MAX_CENTS = 10_000_000L

/** Descripción del gasto: hasta 60 caracteres. */
const val EXPENSE_DESCRIPTION_MAX = 60

/** Entre cuántas personas se puede dividir un gasto (`dividir_entre`, 2 a 12). */
const val SPLIT_MIN = 2
const val SPLIT_MAX = 12

/** Centavos por peso. Todo el dinero va en centavos enteros, nunca en `Float` (spec nv-26). */
const val CENTS_PER_PESO = 100

/** En qué se gastó (`concepto`). */
enum class ExpenseConcept(val apiValue: String) {
    FOOD("comida"),
    DRINKS("bebidas"),
    TRANSPORT("transporte"),
    TICKET("entrada"),
    OTHER("otro");

    companion object {
        fun from(value: String?): ExpenseConcept = entries.firstOrNull { it.apiValue == value } ?: OTHER
    }
}

enum class SplitMode(val apiValue: String) { EQUAL("igual"), CUSTOM("personalizada") }

data class Expense(
    val id: String,
    val amountCents: Long,
    val concept: ExpenseConcept,
    val description: String?,
    val paidBy: String
)

/** Lo que puso y lo que le toca a cada integrante (`por_persona`). */
data class PersonBalance(
    val userId: String,
    val name: String,
    val putCents: Long,
    val shareCents: Long,
    val balanceCents: Long
)

enum class DebtStatus { PENDING, PAID }

/** Quién le debe a quién (`deudas`). Wherever no mueve dinero: marcar pagada solo lo registra. */
data class Debt(
    val id: String,
    val debtorId: String,
    val creditorId: String,
    val amountCents: Long,
    val status: DebtStatus
)

data class VacaSummary(
    val totalCents: Long,
    val people: List<PersonBalance>,
    val debts: List<Debt>,
    val expenses: List<Expense>
)

/** Gasto nuevo (`POST /grupos/{id}/gastos`). `clientId` evita duplicarlo si se reintenta. */
data class NewExpense(
    val amountCents: Long,
    val concept: ExpenseConcept,
    val description: String,
    val paidBy: String,
    val splitBetween: List<String>,
    val mode: SplitMode,
    val customCents: Map<String, Long>,
    val planId: String?,
    val clientId: String
)

/** La vaca virtual de un grupo (backend: gastos y deudas). */
interface VacaRepository {
    /** Corte (`GET /grupos/{id}/vaca`) y los gastos (`GET /grupos/{id}/gastos`). */
    suspend fun summary(groupId: String): Result<VacaSummary>

    suspend fun addExpense(groupId: String, expense: NewExpense): Result<Unit>

    /** `PATCH /vaca/deudas/{id}` con `estado: pagada`; ya pagada = `Conflict`. */
    suspend fun markPaid(debtId: String): Result<Unit>
}

/**
 * Reparte [totalCents] entre [people] sin perder centavos: los primeros reciben un centavo más
 * cuando no es exacto (100 entre 3 = 34, 33 y 33).
 */
fun splitEqually(totalCents: Long, people: Int): List<Long> {
    if (people <= 0) return emptyList()
    val base = totalCents / people
    val extra = (totalCents % people).toInt()
    return List(people) { index -> if (index < extra) base + 1 else base }
}

/** Pesos escritos («480», «480.5», «1,200.50») a centavos; nulo si no es un monto válido. */
fun parsePesos(text: String): Long? {
    val clean = text.replace(",", "").trim()
    if (!Regex("^\\d{1,6}(\\.\\d{0,2})?$").matches(clean)) return null
    val parts = clean.split('.')
    val pesos = parts[0].toLong()
    val cents = parts.getOrNull(1).orEmpty().padEnd(2, '0').take(2).ifEmpty { "0" }.toLong()
    return pesos * CENTS_PER_PESO + cents
}
