package com.wherever.mobile.feature.friends.domain

import com.wherever.mobile.core.common.Result

/**
 * Contenido del QR de 25_MiCodigo (specs/nv-20 §2, Validaciones): solo `codigo_amigo`, nunca tokens
 * ni datos personales. Lo leído con la cámara es texto no confiable: solo pasa un código válido.
 */
object FriendQr {
    private const val PREFIX = "wherever:amigo:"

    /** Un QR más largo que esto no es nuestro; se descarta sin procesarlo. */
    const val MAX_SCANNED_LENGTH = 64

    private val CODE = Regex("^[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{8}$")

    fun isValidCode(code: String): Boolean = CODE.matches(code.trim().uppercase())

    fun payload(code: String): String {
        require(isValidCode(code)) { "codigo_amigo inválido" }
        return PREFIX + code.trim().uppercase()
    }

    /** Código de amigo del texto escaneado, o `null` si no es un QR de Wherever. */
    fun parse(raw: String?): String? {
        val text = raw?.trim()?.takeIf { it.isNotEmpty() && it.length <= MAX_SCANNED_LENGTH } ?: return null
        val candidate = if (text.startsWith(PREFIX, ignoreCase = true)) text.substring(PREFIX.length) else text
        return candidate.uppercase().takeIf { CODE.matches(it) }
    }
}

/** Imagen PNG del QR para compartir o guardar en la galería (25_MiCodigo). */
interface QrImageRepository {
    /** Escribe el PNG en la caché compartible y devuelve su `content://`. */
    suspend fun shareable(payload: String): Result<String>

    /** Guarda el PNG en Imágenes/Wherever. */
    suspend fun saveToGallery(payload: String): Result<Unit>
}
