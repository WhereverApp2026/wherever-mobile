package com.wherever.mobile.feature.photos.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.common.map
import com.wherever.mobile.core.di.UploadClient
import com.wherever.mobile.core.media.CompressedImage
import com.wherever.mobile.core.media.ImageCompressor
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.photos.domain.ImageState
import com.wherever.mobile.feature.photos.domain.PhotoRepository
import com.wherever.mobile.feature.photos.domain.UploadedImage
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

// Contrato de wherever-backend docs/endpoints.md (nv-45).

@Serializable
data class UploadRequestDto(@SerialName("tipo") val type: String)

@Serializable
data class UploadFormDto(
    @SerialName("id") val id: String,
    @SerialName("url") val url: String,
    @SerialName("campos") val fields: Map<String, String> = emptyMap(),
    @SerialName("max_bytes") val maxBytes: Long = 0
)

@Serializable
data class ConfirmedImageDto(
    @SerialName("id") val id: String,
    @SerialName("estado") val state: String = "",
    @SerialName("url_1080") val url1080: String? = null,
    @SerialName("url_320") val url320: String? = null
)

interface ImagesApi {
    @POST("imagenes/subida")
    suspend fun requestUpload(@Body body: UploadRequestDto): UploadFormDto

    @POST("imagenes/{id}/confirmar")
    suspend fun confirm(@Path("id") id: String): ConfirmedImageDto
}

/**
 * El formulario firmado va directo a S3 con un cliente sin `Authorization`: S3 rechaza cabeceras
 * de la API y el token no debe salir de nuestro dominio (constitución §6). El archivo va al final,
 * después de `campos`, como pide el formulario POST de S3.
 */
class PhotoRepositoryImpl @Inject constructor(
    private val api: ImagesApi,
    private val compressor: ImageCompressor,
    @UploadClient private val storageClient: OkHttpClient,
    private val dispatchers: DispatcherProvider
) : PhotoRepository {
    override suspend fun upload(source: String): Result<UploadedImage> {
        val image = withContext(dispatchers.io) { compressor.compress(source) }
            ?: return Result.Failure(AppError.Validation(apiMessage = null))
        return when (val requested = safeApiCall { api.requestUpload(UploadRequestDto(image.mimeType)) }) {
            is Result.Success -> sendAndConfirm(requested.data, image)
            is Result.Failure -> requested
        }
    }

    /** Pasos 2 y 3: subir a S3 sin token y confirmar con la API. */
    private suspend fun sendAndConfirm(form: UploadFormDto, image: CompressedImage): Result<UploadedImage> {
        if (form.maxBytes > 0 && image.bytes.size > form.maxBytes) {
            return Result.Failure(AppError.Validation(apiMessage = null))
        }
        val sent = withContext(dispatchers.io) { postToStorage(form, image.bytes, image.mimeType) }
        return when (sent) {
            is Result.Failure -> sent
            is Result.Success -> safeApiCall { api.confirm(form.id) }.map { it.toDomain() }
        }
    }

    private fun postToStorage(form: UploadFormDto, bytes: ByteArray, mimeType: String): Result<Unit> {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM).apply {
            form.fields.forEach { (name, value) -> addFormDataPart(name, value) }
            addFormDataPart("file", "foto.jpg", bytes.toRequestBody(mimeType.toMediaType()))
        }.build()
        val request = Request.Builder().url(form.url).post(body).build()
        return try {
            storageClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.Success(Unit) else Result.Failure(AppError.Server(traceId = null))
            }
        } catch (_: IOException) {
            Result.Failure(AppError.Network)
        }
    }
}

private fun ConfirmedImageDto.toDomain() = UploadedImage(
    id = id,
    state = when (state) {
        "publicada" -> ImageState.PUBLISHED
        "rechazada" -> ImageState.REJECTED
        else -> ImageState.IN_REVIEW
    },
    url = url1080?.takeIf { it.isNotBlank() },
    thumbnailUrl = url320?.takeIf { it.isNotBlank() }
)
