package com.wherever.mobile.feature.checkin.domain

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint

/** Cómo se comprueba la llegada (`metodo`). */
enum class CheckinMethod(val apiValue: String) { LOCATION("ubicacion"), BUSINESS_QR("qr_negocio") }

/** Check-in válido (`POST /checkins` 201). `createdAt` en ISO 8601. */
data class Checkin(
    val id: String,
    val placeId: String,
    val valid: Boolean,
    val reviewId: String?,
    val createdAt: String
)

/** Llegada a una parada (specs/nv-30). La API nunca guarda la ubicación de la persona. */
interface CheckinRepository {
    /**
     * Por ubicación ([point]) o con el código del mostrador ([code]).
     * Fuera de radio = `Validation` con el mensaje neutro de la API; segundo del día = `Conflict`.
     */
    suspend fun checkIn(placeId: String, planId: String?, point: GeoPoint?, code: String?): Result<Checkin>
}

/** Largo máximo de un código escaneado; lo demás se descarta antes de llamar a la API. */
const val CHECKIN_CODE_MAX = 64

/** Código del mostrador válido: corto y solo letras, números, guiones o guion bajo. */
fun isValidCheckinCode(code: String): Boolean =
    code.length in 1..CHECKIN_CODE_MAX && code.all { it.isLetterOrDigit() || it == '-' || it == '_' }
