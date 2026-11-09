package com.wherever.mobile.core.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError

/** Texto visible sin que el ViewModel dependa de `Context`. */
sealed interface UiText {
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

    data class Plain(val value: String) : UiText

    // stringResource recibe vararg: el spread es obligatorio y la lista es corta.
    @Suppress("SpreadOperator")
    @Composable
    fun asString(): String = when (this) {
        is Res -> stringResource(id, *args.toTypedArray())
        is Plain -> value
    }
}

/** La API manda sus mensajes en minúsculas. */
private fun String.sentenceCase(): String = replaceFirstChar { it.uppercase() }

/** Muestra el mensaje de la API si lo hay; si no, un texto propio. */
fun AppError.toUiText(): UiText {
    apiMessage?.takeIf { it.isNotBlank() }?.let { return UiText.Plain(it.sentenceCase()) }
    return UiText.Res(
        when (this) {
            AppError.Network -> R.string.error_network
            AppError.Timeout -> R.string.error_timeout
            is AppError.Server -> R.string.error_server
            is AppError.RateLimited -> R.string.error_rate_limited
            else -> R.string.error_unknown
        }
    )
}

/** Errores por campo (`campos[]`), con la clave de la API. */
fun AppError.fieldErrors(): Map<String, UiText> = (this as? AppError.Validation)?.fields.orEmpty()
    .associate { it.field to UiText.Plain(it.message.sentenceCase()) }
