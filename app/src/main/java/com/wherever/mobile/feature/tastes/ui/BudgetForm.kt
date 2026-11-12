package com.wherever.mobile.feature.tastes.ui

import com.wherever.mobile.feature.tastes.domain.BudgetIssue
import com.wherever.mobile.feature.tastes.domain.sanitizeAmount
import com.wherever.mobile.feature.tastes.domain.validateBudget
import kotlin.math.roundToInt

/** Tope de la barra: arriba de esto, la barra se queda al final y vale lo escrito (nv-44). */
const val BUDGET_LIMIT = 1000
const val BUDGET_STEP = 50
private const val BUDGET_MIN_DEFAULT = 150
private const val BUDGET_MAX_DEFAULT = 300

/**
 * Presupuesto de Gustos: los textos de los campos son la fuente de verdad y la barra se deriva de ellos.
 * Kotlin puro (sin Compose) para probar la sincronía sin pantalla.
 */
data class BudgetForm(
    val minText: String = BUDGET_MIN_DEFAULT.toString(),
    val maxText: String = BUDGET_MAX_DEFAULT.toString(),
    val minDragged: Boolean = false,
    val maxDragged: Boolean = false
) {
    val min: Int? get() = minText.toIntOrNull()
    val max: Int? get() = maxText.toIntOrNull()

    private val issues: Pair<BudgetIssue?, BudgetIssue?> get() = validateBudget(minText, maxText)
    val minIssue: BudgetIssue? get() = issues.first
    val maxIssue: BudgetIssue? get() = issues.second
    val isValid: Boolean get() = minIssue == null && maxIssue == null

    /** Posición de cada extremo de la barra, siempre dentro de 0..BUDGET_LIMIT y con inicio ≤ fin. */
    val sliderStart: Float get() = (min ?: 0).coerceIn(0, BUDGET_LIMIT).toFloat()
    val sliderEnd: Float get() = (max ?: BUDGET_LIMIT).coerceIn(0, BUDGET_LIMIT).toFloat().coerceAtLeast(sliderStart)

    fun withMinText(raw: String) = copy(minText = sanitizeAmount(raw))

    fun withMaxText(raw: String) = copy(maxText = sanitizeAmount(raw))

    /**
     * La barra informa los dos extremos aunque se mueva uno. Si un extremo no cambió de posición, su texto
     * se respeta: así una cantidad escrita mayor de $1000, que la barra muestra al tope, no se pierde.
     */
    fun slide(start: Float, end: Float): BudgetForm {
        val newStart = start.roundToInt()
        val newEnd = end.roundToInt()
        val startMoved = newStart != sliderStart.roundToInt()
        val endMoved = newEnd != sliderEnd.roundToInt()
        return copy(
            minText = if (startMoved) newStart.toString() else minText,
            maxText = if (endMoved) newEnd.toString() else maxText,
            minDragged = minDragged || startMoved,
            maxDragged = maxDragged || endMoved
        )
    }

    /** Al soltar, solo los extremos arrastrados quedan en pasos de $50; lo escrito a mano no se redondea. */
    fun finishSlide(): BudgetForm = copy(
        minText = if (minDragged) snap(min).toString() else minText,
        maxText = if (maxDragged) snap(max).toString() else maxText,
        minDragged = false,
        maxDragged = false
    )

    private fun snap(value: Int?): Int =
        ((value ?: 0).toFloat() / BUDGET_STEP).roundToInt().times(BUDGET_STEP).coerceIn(0, BUDGET_LIMIT)

    companion object {
        fun of(min: Int, max: Int) = BudgetForm(minText = min.toString(), maxText = max.toString())
    }
}

/** Eventos de `BudgetSection` hacia el ViewModel. */
data class BudgetActions(
    val onMin: (String) -> Unit,
    val onMax: (String) -> Unit,
    val onSlide: (Float, Float) -> Unit,
    val onSlideFinished: () -> Unit
)
