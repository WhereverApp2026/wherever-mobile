package com.wherever.mobile.feature.tastes.domain

/** Tope de la app para lo escrito a mano (nv-44). La API no fija uno. */
const val BUDGET_AMOUNT_MAX = 50_000
private const val MAX_DIGITS = 6

enum class BudgetIssue { EMPTY, OUT_OF_RANGE, MIN_GREATER_THAN_MAX }

/**
 * Deja solo lo que puede ser un entero en pesos: sin espacios, letras, comas de miles ni decimales
 * (lo que va tras el primer punto se descarta) y sin ceros a la izquierda. Un "-" inicial se conserva
 * para avisar del negativo en vez de cambiar el signo sin decirlo. Nunca falla, aunque peguen texto.
 */
fun sanitizeAmount(input: String): String {
    val trimmed = input.trim()
    val whole = trimmed.substringBefore('.')
    val digits = whole.filter(Char::isDigit)
    val number = when {
        digits.isEmpty() -> ""
        else -> digits.trimStart('0').ifEmpty { "0" }.take(MAX_DIGITS)
    }
    return if (trimmed.startsWith("-")) "-$number" else number
}

fun validateAmount(text: String): BudgetIssue? = when {
    text.isEmpty() || text == "-" -> BudgetIssue.EMPTY
    text.toIntOrNull()?.let { it in 0..BUDGET_AMOUNT_MAX } == true -> null
    else -> BudgetIssue.OUT_OF_RANGE
}

/** Errores del mínimo y del máximo. "Mínimo mayor que máximo" va en el mínimo, y solo si los dos son válidos. */
fun validateBudget(minText: String, maxText: String): Pair<BudgetIssue?, BudgetIssue?> {
    val minIssue = validateAmount(minText)
    val maxIssue = validateAmount(maxText)
    if (minIssue == null && maxIssue == null && minText.toInt() > maxText.toInt()) {
        return BudgetIssue.MIN_GREATER_THAN_MAX to null
    }
    return minIssue to maxIssue
}
