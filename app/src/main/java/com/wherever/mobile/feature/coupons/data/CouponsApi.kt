package com.wherever.mobile.feature.coupons.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class WalletCouponDto(
    @SerialName("id") val id: String,
    @SerialName("cupon_id") val couponId: String = "",
    @SerialName("negocio_id") val placeId: String = "",
    @SerialName("negocio_nombre") val placeName: String = "",
    @SerialName("titulo") val title: String = "",
    @SerialName("tipo") val kind: String = "",
    @SerialName("valor") val value: Int? = null,
    @SerialName("estado") val state: String = "",
    @SerialName("vence_en") val expiresAt: String = ""
)

@Serializable
data class WalletPageDto(
    @SerialName("items") val items: List<WalletCouponDto> = emptyList(),
    @SerialName("siguiente_cursor") val nextCursor: String? = null
)

/** Cupón público: solo lo que usa el detalle. */
@Serializable
data class PublicCouponDto(
    @SerialName("id") val id: String = "",
    @SerialName("condiciones") val conditions: String? = null
)

@Serializable
data class RedeemCodeDto(@SerialName("codigo") val code: String, @SerialName("expira_en") val expiresAt: String = "")

/** wherever-backend `docs/endpoints.md`, grupo cupones (nv-61). Propia de nv-29; la del negocio es nv-37. */
interface CouponsApi {
    @GET("mis-cupones")
    suspend fun wallet(
        @Query("filtro") filter: String,
        @Query("cursor") cursor: String?,
        @Query("limite") limit: Int
    ): WalletPageDto

    @GET("cupones/{id}")
    suspend fun coupon(@Path("id") id: String): PublicCouponDto

    @POST("mis-cupones/{id}/canjear")
    suspend fun redeem(@Path("id") id: String): RedeemCodeDto
}
