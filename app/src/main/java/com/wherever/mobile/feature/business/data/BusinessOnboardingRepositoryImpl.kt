package com.wherever.mobile.feature.business.data

import android.content.Context
import androidx.core.net.toUri
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.network.ApiJson
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.business.domain.Address
import com.wherever.mobile.feature.business.domain.BusinessDraft
import com.wherever.mobile.feature.business.domain.BusinessOnboardingRepository
import com.wherever.mobile.feature.business.domain.BusinessPhoto
import com.wherever.mobile.feature.business.domain.BusinessState
import com.wherever.mobile.feature.business.domain.Catalog
import com.wherever.mobile.feature.business.domain.DOCUMENT_MAX_BYTES
import com.wherever.mobile.feature.business.domain.DaySchedule
import com.wherever.mobile.feature.business.domain.DocumentType
import com.wherever.mobile.feature.business.domain.Option
import com.wherever.mobile.feature.business.domain.PhotoType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private const val STEP_LOCATION = 2
private const val STEP_SCHEDULES = 3
private const val STEP_PHOTOS = 4
private const val STEP_OPERATION = 5
private const val STEP_DOCUMENTS = 6

/** Tipos que acepta `POST /negocios/{id}/documentos` (la API los revisa por sus primeros bytes). */
private val DOCUMENT_TYPES = setOf("image/jpeg", "image/png", "application/pdf")

/** Alta del negocio contra la API real (specs/nv-34). El expediente no se guarda en el teléfono. */
class BusinessOnboardingRepositoryImpl @Inject constructor(
    private val api: BusinessOnboardingApi,
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider
) : BusinessOnboardingRepository {
    override suspend fun catalog(): Result<Catalog> = safeApiCall { api.catalog() }.map { dto ->
        Catalog(dto.categories.map { Option(it.id, it.name) }, dto.services.map { Option(it.id, it.name) })
    }

    override suspend fun mine(): Result<List<BusinessDraft>> = when (val result = safeApiCall { api.mine() }) {
        is Result.Success -> Result.Success(result.data.items.map { it.toDomain() })
        // Sin rol comerciante todavía no hay altas.
        is Result.Failure -> if (result.error is AppError.Forbidden) Result.Success(emptyList()) else result
    }

    override suspend fun create(name: String, category: String, description: String): Result<BusinessDraft> =
        safeApiCall { api.create(BusinessCreateDto(name.trim(), category, description.trim())).toDomain() }

    override suspend fun saveLocation(draft: BusinessDraft, address: Address, point: GeoPoint): Result<BusinessDraft> =
        step(
            draft,
            STEP_LOCATION,
            ApiJson.encodeToJsonElement(LocationStepDto(address.toDto(), point.toDto())).jsonObject
        )

    override suspend fun saveSchedules(draft: BusinessDraft, schedules: List<DaySchedule>): Result<BusinessDraft> =
        step(
            draft,
            STEP_SCHEDULES,
            ApiJson.encodeToJsonElement(
                ScheduleStepDto(
                    schedules.map {
                        it.toDto()
                    }
                )
            ).jsonObject
        )

    override suspend fun saveOperation(
        draft: BusinessDraft,
        priceLevel: Int,
        averagePrice: Int,
        services: List<String>
    ): Result<BusinessDraft> = step(
        draft,
        STEP_OPERATION,
        ApiJson.encodeToJsonElement(OperationStepDto(priceLevel, averagePrice, services)).jsonObject
    )

    override suspend fun addPhoto(draft: BusinessDraft, imageId: String, type: PhotoType): Result<BusinessDraft> =
        safeApiCall { api.addPhoto(draft.id, BizPhotoCreateDto(imageId, type.apiKey)).toDomain() }

    override suspend fun confirmPhotos(draft: BusinessDraft): Result<BusinessDraft> =
        step(draft, STEP_PHOTOS, JsonObject(emptyMap()))

    override suspend fun uploadDocument(draft: BusinessDraft, type: DocumentType, source: String): Result<Unit> {
        val file = readDocument(source) ?: return Result.Failure(AppError.Validation())
        val part = MultipartBody.Part.createFormData(
            "archivo",
            "${type.apiKey}.${extension(file.second)}",
            file.first.toRequestBody(file.second.toMediaType())
        )
        return safeApiCall {
            api.uploadDocument(draft.id, type.apiKey.toRequestBody("text/plain".toMediaType()), part)
        }
    }

    override suspend fun confirmDocuments(draft: BusinessDraft): Result<BusinessDraft> =
        step(draft, STEP_DOCUMENTS, JsonObject(emptyMap()))

    override suspend fun saveDepositAccount(draft: BusinessDraft, clabe: String, holder: String): Result<Unit> =
        safeApiCall { api.depositAccount(draft.id, DepositAccountDto(clabe, holder.trim())) }

    override suspend fun submit(draft: BusinessDraft): Result<BusinessDraft> = safeApiCall {
        api.submit(draft.id).toDomain()
    }

    override suspend fun suggestDescription(draft: BusinessDraft, words: List<String>): Result<String> =
        safeApiCall { api.describe(draft.id, DescriptionWordsDto(words.map { it.trim() })).description }

    private suspend fun step(draft: BusinessDraft, step: Int, body: JsonObject): Result<BusinessDraft> =
        safeApiCall { api.step(draft.id, step, "\"${draft.version}\"", body).toDomain() }

    /**
     * Lee el archivo elegido (bytes y tipo). Fuera de JPEG, PNG o PDF, o arriba de 5 MB, no se sube.
     * El nombre original no se manda ni se registra: solo `tipo.extensión` (spec §5).
     */
    private suspend fun readDocument(source: String): Pair<ByteArray, String>? = withContext(dispatchers.io) {
        try {
            val uri = source.toUri()
            val mime = context.contentResolver.getType(uri)?.takeIf { it in DOCUMENT_TYPES } ?: return@withContext null
            val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readNBytesSafe(DOCUMENT_MAX_BYTES + 1)
            } ?: return@withContext null
            if (bytes.size > DOCUMENT_MAX_BYTES) null else bytes to mime
        } catch (_: IOException) {
            null
        }
    }
}

/** Lee hasta `limit` bytes para no cargar archivos enormes en memoria. */
private fun java.io.InputStream.readNBytesSafe(limit: Long): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0L
    while (total < limit) {
        val read = read(buffer)
        if (read < 0) break
        out.write(buffer, 0, read)
        total += read
    }
    return out.toByteArray()
}

private fun extension(mime: String) = when (mime) {
    "image/png" -> "png"
    "application/pdf" -> "pdf"
    else -> "jpg"
}

private fun Address.toDto() = BizAddressDto(
    street = street.trim(),
    number = number.trim(),
    neighborhood = neighborhood.trim(),
    city = city.trim(),
    state = state.trim(),
    zip = zip.trim()
)

/** GeoJSON: longitud primero. */
private fun GeoPoint.toDto() = BizPointDto(type = "Point", coordinates = listOf(lng, lat))

private fun DaySchedule.toDto() = BizScheduleDto(day, if (closed) "" else opens, if (closed) "" else closes, closed)

internal fun BusinessDto.toDomain() = BusinessDraft(
    id = id,
    name = name,
    category = category,
    description = description.orEmpty(),
    state = BusinessState.fromApi(state),
    step = step,
    version = version,
    photos = photos.orEmpty().filter { it.url.isNotBlank() }.map { BusinessPhoto(it.id, it.url) },
    address = address?.let { Address(it.street, it.number, it.neighborhood, it.city, it.state, it.zip) } ?: Address(),
    schedules = schedules.orEmpty().map { DaySchedule(it.day, it.opens, it.closes, it.closed) },
    priceLevel = priceLevel ?: 0,
    averagePrice = averagePrice ?: 0,
    services = services.orEmpty(),
    hasIne = documents?.ine == true,
    hasProofOfAddress = documents?.proof == true,
    requiresRefresh = requiresRefresh
)
