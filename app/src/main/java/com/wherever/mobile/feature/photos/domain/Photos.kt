package com.wherever.mobile.feature.photos.domain

import com.wherever.mobile.core.common.Result

/** Estado de la foto tras `POST /imagenes/{id}/confirmar` (backend nv-45). */
enum class ImageState { PUBLISHED, IN_REVIEW, REJECTED }

data class UploadedImage(val id: String, val state: ImageState, val url: String?, val thumbnailUrl: String?)

/**
 * Sube una foto: pide el formulario firmado, la manda a S3 y la confirma. `source` es el `content://`
 * de la galería o de la cámara. Sirve para el perfil y, después, para reseñas y postales.
 */
interface PhotoRepository {
    suspend fun upload(source: String): Result<UploadedImage>
}
