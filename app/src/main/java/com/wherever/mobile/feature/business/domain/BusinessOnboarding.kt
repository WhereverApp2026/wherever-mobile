package com.wherever.mobile.feature.business.domain

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint

/** Límites del alta (specs/nv-34). */
const val NAME_MIN = 3
const val NAME_MAX = 60
const val DESCRIPTION_MAX = 300
const val MIN_PHOTOS = 3
const val MAX_PHOTOS = 10
const val CLABE_LENGTH = 18
const val DOCUMENT_MAX_BYTES = 5L * 1024 * 1024
const val PRICE_LEVELS = 3

/** Días de la semana como los numera la API (0 = domingo). */
const val DAYS_IN_WEEK = 7

data class Catalog(val categories: List<Option>, val services: List<Option>)

data class Option(val id: String, val name: String)

data class Address(
    val street: String = "",
    val number: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val state: String = "",
    val zip: String = ""
) {
    val complete: Boolean get() = street.isNotBlank() && neighborhood.isNotBlank() && city.isNotBlank()
}

/** Horario de un día: abierto de `opens` a `closes` (HH:MM) o cerrado. */
data class DaySchedule(val day: Int, val opens: String = "", val closes: String = "", val closed: Boolean = true)

enum class BusinessState(val apiKey: String) {
    DRAFT("borrador"),
    IN_REVIEW("en_revision"),
    REJECTED("rechazado"),
    PUBLISHED("publicado");

    companion object {
        fun fromApi(key: String): BusinessState = entries.firstOrNull { it.apiKey == key } ?: DRAFT
    }
}

enum class PhotoType(val apiKey: String) { FACADE("fachada"), INTERIOR("interior"), PRODUCT("producto") }

enum class DocumentType(val apiKey: String) {
    INE_FRONT("ine_frente"),
    INE_BACK("ine_reverso"),
    PROOF_OF_ADDRESS("comprobante_domicilio")
}

data class BusinessPhoto(val id: String, val url: String)

/** Negocio en alta (`POST /negocios`, `GET /negocios/mios`, `PATCH alta/{paso}`). */
data class BusinessDraft(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val state: BusinessState,
    val step: Int,
    val version: Long,
    val photos: List<BusinessPhoto> = emptyList(),
    val address: Address = Address(),
    val schedules: List<DaySchedule> = emptyList(),
    val priceLevel: Int = 0,
    val averagePrice: Int = 0,
    val services: List<String> = emptyList(),
    val hasIne: Boolean = false,
    val hasProofOfAddress: Boolean = false,
    /** `POST /negocios` agregó el rol comerciante: el token actual aún no lo trae. */
    val requiresRefresh: Boolean = false
)

/** Alta del negocio (specs/nv-34). Cada paso guarda en la API con `If-Match`; la app no guarda el expediente. */
interface BusinessOnboardingRepository {
    suspend fun catalog(): Result<Catalog>

    /** Altas de quien llama, de la más nueva a la más vieja. Sin rol comerciante = lista vacía. */
    suspend fun mine(): Result<List<BusinessDraft>>

    suspend fun create(name: String, category: String, description: String): Result<BusinessDraft>

    /** Paso 2: dirección y ubicación (dentro de México). */
    suspend fun saveLocation(draft: BusinessDraft, address: Address, point: GeoPoint): Result<BusinessDraft>

    /** Paso 3: horarios. */
    suspend fun saveSchedules(draft: BusinessDraft, schedules: List<DaySchedule>): Result<BusinessDraft>

    /** Paso 5: precios y servicios. */
    suspend fun saveOperation(
        draft: BusinessDraft,
        priceLevel: Int,
        averagePrice: Int,
        services: List<String>
    ): Result<BusinessDraft>

    /** Registra una foto ya subida por `PhotoRepository` (`POST /negocios/{id}/fotos`). */
    suspend fun addPhoto(draft: BusinessDraft, imageId: String, type: PhotoType): Result<BusinessDraft>

    /** Paso 4: confirma que hay al menos 3 fotos. */
    suspend fun confirmPhotos(draft: BusinessDraft): Result<BusinessDraft>

    /** Consentimiento expreso `datos_negocio` con las versiones vigentes; sin él, documentos y CLABE dan 422. */
    suspend fun acceptBusinessDataConsent(): Result<Unit>

    /** Multipart directo a la API (`tipo` y `archivo`), JPEG, PNG o PDF de hasta 5 MB. */
    suspend fun uploadDocument(draft: BusinessDraft, type: DocumentType, source: String): Result<Unit>

    /** Paso 6: INE y comprobante vigentes. */
    suspend fun confirmDocuments(draft: BusinessDraft): Result<BusinessDraft>

    /** CLABE opcional (no frena el envío). */
    suspend fun saveDepositAccount(draft: BusinessDraft, clabe: String, holder: String): Result<Unit>

    /** `POST /negocios/{id}/enviar-revision`; 422 trae la lista de lo que falta en `campos`. */
    suspend fun submit(draft: BusinessDraft): Result<BusinessDraft>

    /** «Que la IA me ayude»: descripción a partir de 3 palabras. No se guarda. */
    suspend fun suggestDescription(draft: BusinessDraft, words: List<String>): Result<String>
}

private const val DECIMAL = 10

/** Pesos del dígito verificador de la CLABE (Banxico): 3, 7, 1 repetidos. */
private val CLABE_WEIGHTS = listOf(CLABE_WEIGHT_1, CLABE_WEIGHT_2, CLABE_WEIGHT_3)
private const val CLABE_WEIGHT_1 = 3
private const val CLABE_WEIGHT_2 = 7
private const val CLABE_WEIGHT_3 = 1

/** CLABE: 18 dígitos y dígito verificador (pesos 3, 7, 1), como la valida la API. */
fun isValidClabe(clabe: String): Boolean {
    if (clabe.length != CLABE_LENGTH || !clabe.all(Char::isDigit)) return false
    val sum = clabe.take(CLABE_LENGTH - 1).mapIndexed { index, c ->
        c.digitToInt() * CLABE_WEIGHTS[index % CLABE_WEIGHTS.size] % DECIMAL
    }.sum()
    return (DECIMAL - sum % DECIMAL) % DECIMAL == clabe.last().digitToInt()
}

fun isValidBusinessName(name: String): Boolean = name.trim().length in NAME_MIN..NAME_MAX

/** HH:MM de 00:00 a 23:59. */
fun isValidTime(value: String): Boolean = Regex("^([01]\\d|2[0-3]):[0-5]\\d$").matches(value)

/** Abre antes de cerrar, ambos en HH:MM. */
fun DaySchedule.isValidRange(): Boolean = isValidTime(opens) && isValidTime(closes) && opens < closes
