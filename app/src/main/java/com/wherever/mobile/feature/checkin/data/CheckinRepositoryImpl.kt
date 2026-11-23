package com.wherever.mobile.feature.checkin.data

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.checkin.domain.Checkin
import com.wherever.mobile.feature.checkin.domain.CheckinMethod
import com.wherever.mobile.feature.checkin.domain.CheckinRepository
import javax.inject.Inject

/**
 * Check-in contra la API real (specs/nv-30). La ubicación solo viaja en esta petición: no se guarda en el
 * teléfono ni se registra.
 */
class CheckinRepositoryImpl @Inject constructor(private val api: CheckinApi) : CheckinRepository {
    override suspend fun checkIn(placeId: String, planId: String?, point: GeoPoint?, code: String?): Result<Checkin> =
        safeApiCall {
            val method = if (code != null) CheckinMethod.BUSINESS_QR else CheckinMethod.LOCATION
            val dto = api.checkIn(
                CheckinRequestDto(
                    placeId = placeId,
                    method = method.apiValue,
                    lat = point?.lat.takeIf { code == null },
                    lng = point?.lng.takeIf { code == null },
                    code = code,
                    planId = planId?.takeIf { it.isNotBlank() }
                )
            )
            Checkin(dto.id, dto.placeId.ifBlank { placeId }, dto.valid, dto.reviewId, dto.createdAt)
        }
}
