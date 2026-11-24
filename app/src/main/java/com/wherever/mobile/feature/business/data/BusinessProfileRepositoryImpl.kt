package com.wherever.mobile.feature.business.data

import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.business.domain.Balance
import com.wherever.mobile.feature.business.domain.BusinessDataChange
import com.wherever.mobile.feature.business.domain.BusinessPhoto
import com.wherever.mobile.feature.business.domain.BusinessProfile
import com.wherever.mobile.feature.business.domain.BusinessProfileRepository
import com.wherever.mobile.feature.business.domain.DaySchedule
import com.wherever.mobile.feature.business.domain.DepositAccount
import com.wherever.mobile.feature.business.domain.PhotoType
import com.wherever.mobile.feature.business.domain.Settlement
import com.wherever.mobile.feature.business.domain.TaxData
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/** Liquidaciones por página. */
private const val SETTLEMENTS_LIMIT = 20

/** Perfil, fotos y cobros del negocio contra la API real (specs/nv-38). CLABE y RFC no se guardan. */
@Suppress("TooManyFunctions") // Un método por endpoint de la spec.
class BusinessProfileRepositoryImpl @Inject constructor(private val api: BusinessProfileApi) :
    BusinessProfileRepository {
    /** `reservas_config` tal cual llegó: el `PUT` devuelve los demás campos sin cambios (D-37e). */
    private val configs = ConcurrentHashMap<String, JsonObject>()

    override suspend fun profile(businessId: String): Result<BusinessProfile> {
        val result = safeApiCall {
            coroutineScope {
                // El panel solo completa la cuenta y los fiscales: si falla, el perfil se muestra igual.
                val accounts = async { runCatching { api.panelAccounts(businessId) }.getOrNull() }
                val dto = api.mine().items.firstOrNull { it.id == businessId }
                val extra = accounts.await()
                dto?.toDomain()?.copy(
                    depositAccount = extra?.depositAccount?.toDomain(),
                    taxDataComplete = extra?.taxDataComplete
                )
            }
        }
        return when (result) {
            is Result.Failure -> result
            is Result.Success -> result.data?.let { Result.Success(it) } ?: Result.Failure(AppError.NotFound())
        }
    }

    override suspend fun updateData(profile: BusinessProfile, change: BusinessDataChange): Result<BusinessProfile> =
        safeApiCall {
            val body = BusinessDataPatchDto(
                description = change.description.trim(),
                phone = change.phone.takeIf { it.isNotBlank() },
                schedules = change.schedules.map { BizScheduleDto(it.day, it.opens, it.closes, it.closed) }
            )
            api.update(profile.id, "\"${profile.version}\"", body).toDomain().keepAccounts(profile)
        }

    override suspend fun setAcceptsReservations(profile: BusinessProfile, accepts: Boolean): Result<BusinessProfile> =
        safeApiCall {
            val base = configs[profile.id] ?: JsonObject(emptyMap())
            val body = JsonObject(base + ("acepta_reservas" to JsonPrimitive(accepts)))
            api.saveConfig(profile.id, "\"${profile.version}\"", body).toDomain().keepAccounts(profile)
        }

    override suspend fun pause(businessId: String, paused: Boolean, until: LocalDate?, reason: String?): Result<Unit> =
        safeApiCall {
            api.pause(
                businessId,
                PauseDto(paused, until?.toString(), reason?.trim()?.takeIf { it.isNotEmpty() })
            )
            Unit
        }

    override suspend fun delete(businessId: String, confirmation: String): Result<Unit> =
        safeApiCall { api.delete(businessId, DeleteBusinessDto(confirmation)) }

    override suspend fun addPhoto(businessId: String, imageId: String, type: PhotoType): Result<List<BusinessPhoto>> =
        safeApiCall { api.addPhoto(businessId, BizPhotoCreateDto(imageId, type.apiKey)).photos.toPhotos() }

    override suspend fun reorderPhotos(businessId: String, ids: List<String>): Result<List<BusinessPhoto>> =
        safeApiCall { api.reorderPhotos(businessId, PhotoOrderDto(ids.distinct())).photos.toPhotos() }

    override suspend fun deletePhoto(businessId: String, photoId: String): Result<Unit> =
        safeApiCall { api.deletePhoto(businessId, photoId) }

    override suspend fun connectPayments(businessId: String): Result<String> =
        safeApiCall { api.connectPayments(businessId).url }

    override suspend fun balance(businessId: String): Result<Balance> = safeApiCall {
        val dto = api.balance(businessId)
        Balance(dto.available, dto.pending, dto.nextDeposit?.let { runCatching { LocalDate.parse(it) }.getOrNull() })
    }

    override suspend fun settlements(businessId: String, cursor: String?): Result<PageResult<Settlement>> =
        safeApiCall {
            val page = api.settlements(businessId, cursor, SETTLEMENTS_LIMIT)
            PageResult(
                page.items.mapNotNull { dto ->
                    runCatching { LocalDate.parse(dto.weekStart.take(DATE_LENGTH)) }.getOrNull()
                        ?.let { Settlement(dto.id, it, dto.net, dto.state) }
                },
                page.nextCursor
            )
        }

    override suspend fun saveDepositAccount(businessId: String, clabe: String, holder: String): Result<DepositAccount> =
        safeApiCall { api.depositAccount(businessId, DepositAccountDto(clabe, holder.trim())).toDomain() }

    override suspend fun saveTaxData(businessId: String, data: TaxData): Result<Unit> = safeApiCall {
        api.taxData(
            businessId,
            TaxDataDto(
                rfc = data.rfc.trim().uppercase(),
                legalName = data.legalName.trim(),
                regime = data.regime,
                cfdiUse = data.cfdiUse,
                postalCode = data.postalCode
            )
        )
    }

    override suspend fun exploreMode(): Result<Unit> = safeApiCall { api.mode(ModeDto(EXPLORE_MODE)) }

    private fun BusinessProfileDto.toDomain(): BusinessProfile {
        reservationsConfig?.let { configs[id] = it }
        val pausedFlag = when (val value = paused) {
            null, is JsonNull -> false
            is JsonPrimitive -> value.booleanOrNull ?: false
            else -> true
        }
        return BusinessProfile(
            id = id,
            name = name,
            category = category,
            description = description.orEmpty(),
            phone = phone.orEmpty(),
            address = address?.let { listOf("${it.street} ${it.number}".trim(), it.neighborhood, it.city) }
                ?.filter { it.isNotBlank() }?.joinToString(", ")?.takeIf { it.isNotBlank() },
            state = state,
            paused = pausedFlag || state == PAUSED_STATE,
            schedules = schedules.orEmpty().map { DaySchedule(it.day, it.opens, it.closes, it.closed) },
            photos = photos.toPhotos(),
            acceptsReservations = reservationsConfig?.get("acepta_reservas")?.jsonPrimitive?.booleanOrNull,
            paymentsState = payments?.state,
            version = version
        )
    }
}

private const val EXPLORE_MODE = "explorar"
private const val PAUSED_STATE = "pausado"
private const val DATE_LENGTH = 10

private fun BusinessProfile.keepAccounts(old: BusinessProfile) =
    copy(depositAccount = old.depositAccount, taxDataComplete = old.taxDataComplete)

private fun List<BizPhotoDto>?.toPhotos() = orEmpty().map { BusinessPhoto(it.id, it.url) }

private fun DepositAccountInfoDto.toDomain() = DepositAccount(lastFour, bank, holder, state)
