package com.wherever.mobile.feature.business.domain

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import java.time.LocalDate

/** Estados en los que la galería no baja de 3 fotos. */
private val LIVE_STATES = setOf("en_revision", "aprobado", "publicado", "pausado")

private val PAUSABLE_STATES = setOf("aprobado", "publicado")

/** Pausa: hasta 90 días y motivo de hasta 120 caracteres. */
const val PAUSE_MAX_DAYS = 90L
const val PAUSE_REASON_MAX = 120

/** Teléfono de 10 dígitos (México). */
const val PHONE_LENGTH = 10

/** RFC: 12 (moral) o 13 (física) caracteres; C.P. de 5 dígitos. */
const val RFC_MORAL_LENGTH = 12
const val RFC_PERSON_LENGTH = 13
const val POSTAL_CODE_LENGTH = 5
const val LEGAL_NAME_MAX = 254

/** Perfil del negocio (`GET /negocios/mios`): lo que edita 58_NegPerfil. */
data class BusinessProfile(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val phone: String,
    val address: String?,
    /** `estado` tal cual (`en_revision`, `aprobado`, `pausado`…). */
    val state: String,
    val paused: Boolean,
    val schedules: List<DaySchedule>,
    val photos: List<BusinessPhoto>,
    val acceptsReservations: Boolean?,
    /** Estado de los cobros de Stripe (`cobros.estado`); nulo si nunca se conectaron. */
    val paymentsState: String?,
    val version: Long,
    /** Del panel: cuenta de depósito (últimos 4) y si los datos fiscales están completos. */
    val depositAccount: DepositAccount? = null,
    val taxDataComplete: Boolean? = null
) {
    /** En revisión o aprobado no se puede bajar de 3 fotos. */
    val keepsMinPhotos: Boolean get() = paused || state in LIVE_STATES

    /** Pausar solo desde aprobado; reanudar solo desde pausado. */
    val canPause: Boolean get() = paused || state in PAUSABLE_STATES

    val paymentsConnected: Boolean get() = paymentsState == "activo" || paymentsState == "activa"
}

/** Cambios de «Datos y horario» (`PATCH /negocios/{id}`). */
data class BusinessDataChange(val description: String, val phone: String, val schedules: List<DaySchedule>)

/** `GET saldo`, en centavos. */
data class Balance(val available: Long, val pending: Long, val nextDeposit: LocalDate?)

/** Una liquidación semanal (`GET liquidaciones`). */
data class Settlement(val id: String, val weekStart: LocalDate, val net: Long, val state: String)

/** Cuenta para depósitos: la API solo devuelve los últimos 4 dígitos. */
data class DepositAccount(val lastFour: String, val bank: String?, val holder: String?, val state: String?)

/** Datos fiscales de 90_NegFiscal. Solo viajan en el `PUT`; nada se guarda en el teléfono. */
data class TaxData(
    val rfc: String,
    val legalName: String,
    val regime: String,
    val cfdiUse: String,
    val postalCode: String
)

/** Perfil, fotos, cobros y salida del modo negocio (specs/nv-38). */
@Suppress("TooManyFunctions") // Un método por endpoint de la tabla de la spec.
interface BusinessProfileRepository {
    suspend fun profile(businessId: String): Result<BusinessProfile>

    /** `PATCH` con `If-Match`; versión vieja = `Conflict`. */
    suspend fun updateData(profile: BusinessProfile, change: BusinessDataChange): Result<BusinessProfile>

    /** `acepta_reservas` en `PUT reservas-config`, sin perder los demás campos. */
    suspend fun setAcceptsReservations(profile: BusinessProfile, accepts: Boolean): Result<BusinessProfile>

    suspend fun pause(businessId: String, paused: Boolean, until: LocalDate?, reason: String?): Result<Unit>

    /** `DELETE` con el nombre exacto como confirmación. */
    suspend fun delete(businessId: String, confirmation: String): Result<Unit>

    suspend fun addPhoto(businessId: String, imageId: String, type: PhotoType): Result<List<BusinessPhoto>>

    /** Todas las fotos, sin repetir; la primera es la portada. */
    suspend fun reorderPhotos(businessId: String, ids: List<String>): Result<List<BusinessPhoto>>

    suspend fun deletePhoto(businessId: String, photoId: String): Result<Unit>

    /** URL del registro de Stripe. Sin llave de Stripe = 503. */
    suspend fun connectPayments(businessId: String): Result<String>

    suspend fun balance(businessId: String): Result<Balance>

    suspend fun settlements(businessId: String, cursor: String?): Result<PageResult<Settlement>>

    suspend fun saveDepositAccount(businessId: String, clabe: String, holder: String): Result<DepositAccount>

    suspend fun saveTaxData(businessId: String, data: TaxData): Result<Unit>

    /** «Volver a Wherever»: `POST /usuarios/yo/modo` con `explorar`. */
    suspend fun exploreMode(): Result<Unit>
}

/** Pausa con fecha de hoy (exclusiva) a 90 días. */
fun isValidPauseUntil(until: LocalDate?, today: LocalDate): Boolean =
    until == null || (until.isAfter(today) && !until.isAfter(today.plusDays(PAUSE_MAX_DAYS)))

fun isValidPhone(phone: String): Boolean =
    phone.isEmpty() || (phone.length == PHONE_LENGTH && phone.all { it.isDigit() })

/** RFC: 3 o 4 letras, fecha AAMMDD real y homoclave de 3. La API vuelve a revisarlo (422 `rfc_invalido`). */
fun isValidRfc(rfc: String): Boolean {
    val value = rfc.trim().uppercase()
    val date = RFC_PATTERN.matchEntire(value)?.groupValues?.get(2)
    return date != null && (value.length == RFC_MORAL_LENGTH || value.length == RFC_PERSON_LENGTH) && isRealDate(date)
}

private val RFC_PATTERN = Regex("^([A-ZÑ&]{3,4})(\\d{6})([A-Z\\d]{3})$")

private const val CENTURY = 2000
private const val YEAR_END = 2
private const val MONTH_END = 4
private const val DAY_END = 6

/** `AAMMDD` es una fecha que existe (el siglo no importa para validar el día). */
private fun isRealDate(date: String): Boolean = runCatching {
    LocalDate.of(
        CENTURY + date.substring(0, YEAR_END).toInt(),
        date.substring(YEAR_END, MONTH_END).toInt(),
        date.substring(MONTH_END, DAY_END).toInt()
    )
}.isSuccess

fun isValidTaxData(data: TaxData): Boolean = isValidRfc(data.rfc) &&
    data.legalName.trim().length in 1..LEGAL_NAME_MAX &&
    data.regime.isNotBlank() &&
    data.cfdiUse.isNotBlank() &&
    data.postalCode.length == POSTAL_CODE_LENGTH &&
    data.postalCode.all { it.isDigit() }
