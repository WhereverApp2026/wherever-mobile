package com.wherever.mobile.feature.places.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Contratos de wherever-backend docs/endpoints.md (nv-20, nv-21, nv-22, nv-54, nv-56, nv-62, nv-66, nv-67).

@Serializable
data class PointDto(@SerialName("lat") val lat: Double = 0.0, @SerialName("lng") val lng: Double = 0.0)

@Serializable
data class RatingSummaryDto(
    @SerialName("promedio") val average: Double = 0.0,
    @SerialName("total_resenas") val reviews: Int = 0
)

/** Tarjeta de lugar; `/recomendaciones` e `/inicio` le agregan `por_que` y `razones`. */
@Serializable
data class PlaceCardDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("categoria") val category: String = "",
    @SerialName("nivel_precio") val priceLevel: Int = 0,
    @SerialName("precio_promedio") val averagePrice: Int = 0,
    @SerialName("calificacion") val rating: RatingSummaryDto? = null,
    @SerialName("foto_portada") val cover: String? = null,
    @SerialName("distancia_m") val distanceMeters: Double? = null,
    @SerialName("abierto_ahora") val openNow: Boolean = false,
    @SerialName("por_que") val why: String? = null
)

@Serializable
data class PlacePageDto(
    @SerialName("items") val items: List<PlaceCardDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

@Serializable
data class SearchResultDto(@SerialName("items") val items: List<PlaceCardDto> = emptyList())

@Serializable
data class CategoryDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("conteo") val count: Int = 0
)

@Serializable
data class CategoriesDto(@SerialName("categorias") val categories: List<CategoryDto> = emptyList())

@Serializable
data class RecentSearchDto(@SerialName("texto") val text: String)

@Serializable
data class RecentSearchesDto(@SerialName("items") val items: List<RecentSearchDto> = emptyList())

@Serializable
data class AddressDto(
    @SerialName("calle") val street: String = "",
    @SerialName("numero") val number: String = "",
    @SerialName("colonia") val neighborhood: String = "",
    @SerialName("municipio") val city: String = "",
    @SerialName("estado") val state: String = ""
)

@Serializable
data class ScheduleDto(
    @SerialName("dia") val day: Int,
    @SerialName("abre") val opens: String = "",
    @SerialName("cierra") val closes: String = "",
    @SerialName("cerrado") val closed: Boolean = false
)

@Serializable
data class PhotoDto(@SerialName("url") val url: String = "", @SerialName("orden") val order: Int = 0)

@Serializable
data class AccessibilityDto(
    @SerialName("silla_ruedas") val wheelchair: Boolean = false,
    @SerialName("apto_ninos") val kids: Boolean = false,
    @SerialName("pet_friendly") val pets: Boolean = false
)

@Serializable
data class AspectsDto(
    @SerialName("sabor") val taste: Double = 0.0,
    @SerialName("atencion") val service: Double = 0.0,
    @SerialName("ambiente") val ambience: Double = 0.0,
    @SerialName("espera") val wait: Double = 0.0,
    @SerialName("precio_calidad") val value: Double = 0.0
)

@Serializable
data class RatingDto(
    @SerialName("promedio") val average: Double = 0.0,
    @SerialName("total_resenas") val reviews: Int = 0,
    @SerialName("aspectos") val aspects: AspectsDto? = null
)

@Serializable
data class PlaceDetailDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val name: String = "",
    @SerialName("categoria") val category: String = "",
    @SerialName("etiquetas") val tags: List<String>? = null,
    @SerialName("descripcion") val description: String? = null,
    @SerialName("nivel_precio") val priceLevel: Int = 0,
    @SerialName("precio_promedio") val averagePrice: Int = 0,
    @SerialName("ubicacion") val location: PointDto = PointDto(),
    @SerialName("direccion") val address: AddressDto? = null,
    @SerialName("telefono") val phone: String? = null,
    @SerialName("horarios") val schedules: List<ScheduleDto>? = null,
    @SerialName("fotos") val photos: List<PhotoDto>? = null,
    @SerialName("servicios") val services: List<String>? = null,
    @SerialName("accesibilidad") val accessibility: AccessibilityDto? = null,
    @SerialName("opciones_dieta") val dietOptions: List<String>? = null,
    @SerialName("acepta_reservas") val acceptsReservations: Boolean = false,
    @SerialName("calificacion") val rating: RatingDto? = null,
    @SerialName("verificado") val verified: Boolean = false,
    @SerialName("sello_excelencia") val excellenceSeal: Boolean = false,
    @SerialName("abierto_ahora") val openNow: Boolean = false
)

@Serializable
data class TrafficDto(
    @SerialName("dia") val day: Int = 0,
    @SerialName("horas") val hours: List<Int> = emptyList(),
    @SerialName("hora_actual") val currentHour: Int? = null,
    @SerialName("suficiente") val enough: Boolean = false
)

@Serializable
data class MenuItemDto(
    @SerialName("nombre") val name: String = "",
    @SerialName("descripcion") val description: String? = null,
    @SerialName("precio") val price: Double = 0.0
)

@Serializable
data class MenuSectionDto(
    @SerialName("nombre") val name: String = "",
    @SerialName("platillos") val items: List<MenuItemDto> = emptyList()
)

@Serializable
data class MenuDto(@SerialName("secciones") val sections: List<MenuSectionDto> = emptyList())

@Serializable
data class ReviewAuthorDto(@SerialName("nombre_usuario") val username: String = "")

@Serializable
data class BusinessReplyDto(@SerialName("texto") val text: String = "")

@Serializable
data class ReviewDto(
    @SerialName("id") val id: String,
    @SerialName("autor") val author: ReviewAuthorDto? = null,
    @SerialName("calificacion") val rating: Int = 0,
    @SerialName("chips") val chips: List<String>? = null,
    @SerialName("comentario") val comment: String? = null,
    @SerialName("util") val useful: Int = 0,
    @SerialName("respuesta_negocio") val businessReply: BusinessReplyDto? = null,
    @SerialName("creado_en") val createdAt: String = ""
)

@Serializable
data class ReviewPageDto(
    @SerialName("items") val items: List<ReviewDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

@Serializable
data class UsefulDto(@SerialName("util") val useful: Boolean)

@Serializable
data class ViewDto(@SerialName("origen") val origin: String)

@Serializable
data class ReportDto(@SerialName("motivo") val reason: String, @SerialName("detalle") val detail: String? = null)

@Serializable
data class FavoriteIdsDto(@SerialName("ids") val ids: List<String> = emptyList())

@Serializable
data class FavoriteSummaryDto(
    @SerialName("nombre") val name: String = "",
    @SerialName("foto_url") val photoUrl: String? = null,
    @SerialName("categoria") val category: String? = null
)

@Serializable
data class FavoriteDto(
    @SerialName("objetivo_id") val targetId: String,
    @SerialName("resumen") val summary: FavoriteSummaryDto? = null,
    @SerialName("abierto_ahora") val openNow: Boolean? = null
)

@Serializable
data class FavoritePageDto(@SerialName("items") val items: List<FavoriteDto> = emptyList())
