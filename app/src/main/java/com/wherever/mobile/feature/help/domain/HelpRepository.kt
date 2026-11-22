package com.wherever.mobile.feature.help.domain

import com.wherever.mobile.core.common.Result
import java.text.Normalizer

/** Largo máximo del reporte que acepta `POST /errores` (specs/nv-33). */
const val REPORT_MAX_LENGTH = 500

/** Pregunta frecuente de 86_Ayuda. Los textos viven en `strings.xml` (specs/nv-33, decisión D-33). */
data class Faq(val id: Int, val question: String, val answer: String)

/**
 * Datos de contacto de soporte. Salen de `BuildConfig` (`SUPPORT_EMAIL`, `SUPPORT_WHATSAPP`); si faltan, la
 * fila no se dibuja (specs/nv-33 §2, decisiones).
 */
data class SupportContacts(val email: String?, val whatsapp: String?)

/** Ayuda (specs/nv-33): preguntas frecuentes, contacto y reporte de problemas. */
interface HelpRepository {
    fun faqs(): List<Faq>

    fun contacts(): SupportContacts

    /** `POST /errores` con `modulo = reporte_usuario`. Manda solo el texto recortado a [REPORT_MAX_LENGTH]. */
    suspend fun reportProblem(message: String): Result<Unit>
}

private val DIACRITICS = Regex("\\p{Mn}+")

/** Clave de búsqueda sin acentos ni mayúsculas: «Cancelación» y «cancelacion» coinciden. */
fun String.searchKey(): String = Normalizer.normalize(this, Normalizer.Form.NFD)
    .replace(DIACRITICS, "")
    .lowercase()
    .trim()

/** Preguntas cuyo texto o respuesta contiene la búsqueda; con la búsqueda vacía, todas. */
fun List<Faq>.matching(query: String): List<Faq> {
    val key = query.searchKey()
    if (key.isEmpty()) return this
    return filter { it.question.searchKey().contains(key) || it.answer.searchKey().contains(key) }
}
