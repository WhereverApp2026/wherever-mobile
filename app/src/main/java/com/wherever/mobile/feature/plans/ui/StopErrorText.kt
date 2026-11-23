package com.wherever.mobile.feature.plans.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText

/** «Parada 6b04…: cerrado» o «parada 2: no_cabe» (backend `planes/recalculo.go`). */
private val STOP_ERROR = Regex("""[Pp]arada\s+([0-9a-f]{24}|\d+)\s*:\s*([a-z_]+)""")

/**
 * El backend explica por qué una parada no sirve con su id (o su número) y un código. Se traduce al nombre
 * del lugar y a una frase; si no se reconoce, queda el mensaje normal. Encontrado al probar con staging:
 * la app mostraba «Parada 6b045db57d777f3ea0301d45: cerrado».
 */
internal fun AppError.stopErrorText(stopNames: List<Pair<String, String>>): UiText {
    val match = apiMessage?.let(STOP_ERROR::find) ?: return toUiText()
    val (ref, reason) = match.destructured
    val name = stopNames.firstOrNull { it.first == ref }?.second
        ?: ref.toIntOrNull()?.let { stopNames.getOrNull(it)?.second ?: stopNames.getOrNull(it - 1)?.second }
        ?: return toUiText()
    val res = when (reason) {
        "cerrado" -> R.string.plan_stop_closed
        "no_cabe" -> R.string.plan_stop_overlap
        "duracion" -> R.string.plan_stop_duration
        "precio" -> R.string.plan_stop_price
        "negocio_no_disponible" -> R.string.plan_stop_unavailable
        else -> return toUiText()
    }
    return UiText.Res(res, listOf(name))
}
