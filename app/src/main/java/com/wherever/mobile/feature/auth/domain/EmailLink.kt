package com.wherever.mobile.feature.auth.domain

/**
 * Enlace de un correo de la API: `{APP_LINK_BASE}/restablecer#token=…` o `/verificar#token=…`
 * (backend `internal/platform/mailer/link.go`). También acepta `wherever://restablecer#token=…`
 * y el texto pegado del correo. El token va en el fragmento y nunca se registra en logs.
 */
sealed interface EmailLink {
    val token: String

    data class ResetPassword(override val token: String) : EmailLink

    data class VerifyEmail(override val token: String) : EmailLink

    companion object {
        private const val MAX_TOKEN_LENGTH = 512
        private val TOKEN_PATTERN = Regex("^[A-Za-z0-9._~%-]+$")
        private val LINK_PATTERN = Regex("""(restablecer|verificar)/?(?:\?[^#\s]*)?#token=([^&\s]+)""")

        /** `null` si el texto no trae un enlace de la API válido. */
        fun parse(text: String?): EmailLink? {
            val match = text?.trim()?.let { LINK_PATTERN.find(it) } ?: return null
            val token = match.groupValues[2].takeIf { it.length <= MAX_TOKEN_LENGTH && TOKEN_PATTERN.matches(it) }
                ?: return null
            val decoded = java.net.URLDecoder.decode(token, Charsets.UTF_8.name())
            return when (match.groupValues[1]) {
                "restablecer" -> ResetPassword(decoded)
                else -> VerifyEmail(decoded)
            }
        }
    }
}
