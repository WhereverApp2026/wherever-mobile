package com.wherever.mobile.testing

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
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

fun sampleBizProfile(photos: Int = 4, state: String = "aprobado", paused: Boolean = false, version: Long = 3) =
    BusinessProfile(
        id = "n1",
        name = "Café Raíz",
        category = "cafeteria",
        description = "Café de especialidad",
        phone = "5512345678",
        address = null,
        state = state,
        paused = paused,
        schedules = listOf(DaySchedule(1, "08:00", "20:00", closed = false)),
        photos = (1..photos).map { BusinessPhoto("f$it", "https://img/$it.jpg") },
        acceptsReservations = true,
        paymentsState = null,
        version = version,
        depositAccount = DepositAccount("1234", "BBVA", null, "registrada"),
        taxDataComplete = false
    )

/** Perfil, fotos y cobros del negocio en memoria (nv-38); cada llamada queda en [calls]. */
@Suppress("TooManyFunctions") // Un método por endpoint, como la interfaz.
class FakeBusinessProfileRepository : BusinessProfileRepository {
    val calls = mutableListOf<String>()
    var profileResult: Result<BusinessProfile> = Result.Success(sampleBizProfile())
    var updateResult: Result<BusinessProfile>? = null
    var acceptsResult: Result<BusinessProfile>? = null
    var pauseResult: Result<Unit> = Result.Success(Unit)
    var deleteResult: Result<Unit> = Result.Success(Unit)
    var addResult: Result<List<BusinessPhoto>>? = null
    var reorderResult: Result<List<BusinessPhoto>>? = null
    var deletePhotoResult: Result<Unit> = Result.Success(Unit)
    var connectResult: Result<String> = Result.Success("https://connect.stripe.com/setup/abc")
    var balanceResult: Result<Balance> = Result.Success(Balance(342_000, 124_000, LocalDate.of(2026, 11, 30)))
    var settlementPages: Map<String?, PageResult<Settlement>> = mapOf(
        null to PageResult(listOf(Settlement("s1", LocalDate.of(2026, 10, 12), 218_000, "pagada")), "c2"),
        "c2" to PageResult(listOf(Settlement("s2", LocalDate.of(2026, 10, 5), 194_000, "pagada")), null)
    )
    var bankResult: Result<DepositAccount> = Result.Success(
        DepositAccount("7899", "BBVA", "Café Raíz", "sin_verificar")
    )
    var taxResult: Result<Unit> = Result.Success(Unit)
    var exploreResult: Result<Unit> = Result.Success(Unit)
    val pauses = mutableListOf<Triple<Boolean, LocalDate?, String?>>()
    val versionsSent = mutableListOf<Long>()
    var orderSent: List<String>? = null
    var taxSent: TaxData? = null

    override suspend fun profile(businessId: String) = profileResult.also { calls += "perfil" }

    override suspend fun updateData(profile: BusinessProfile, change: BusinessDataChange): Result<BusinessProfile> {
        versionsSent += profile.version
        return updateResult ?: Result.Success(
            profile.copy(description = change.description, phone = change.phone, version = profile.version + 1)
        )
    }

    override suspend fun setAcceptsReservations(profile: BusinessProfile, accepts: Boolean): Result<BusinessProfile> {
        versionsSent += profile.version
        calls += "acepta $accepts"
        return acceptsResult
            ?: Result.Success(profile.copy(acceptsReservations = accepts, version = profile.version + 1))
    }

    override suspend fun pause(businessId: String, paused: Boolean, until: LocalDate?, reason: String?): Result<Unit> {
        pauses += Triple(paused, until, reason)
        return pauseResult
    }

    override suspend fun delete(businessId: String, confirmation: String) = deleteResult.also {
        calls +=
            "borrar $confirmation"
    }

    override suspend fun addPhoto(businessId: String, imageId: String, type: PhotoType): Result<List<BusinessPhoto>> {
        calls += "foto $imageId"
        val current = (profileResult as Result.Success).data.photos
        return addResult ?: Result.Success(current + BusinessPhoto("f-$imageId", "https://img/$imageId.jpg"))
    }

    override suspend fun reorderPhotos(businessId: String, ids: List<String>): Result<List<BusinessPhoto>> {
        orderSent = ids
        val byId = (profileResult as Result.Success).data.photos.associateBy { it.id }
        return reorderResult ?: Result.Success(ids.mapNotNull { byId[it] })
    }

    override suspend fun deletePhoto(businessId: String, photoId: String) = deletePhotoResult.also {
        calls +=
            "borrar foto $photoId"
    }

    override suspend fun connectPayments(businessId: String) = connectResult.also { calls += "conectar" }

    override suspend fun balance(businessId: String) = balanceResult

    override suspend fun settlements(businessId: String, cursor: String?): Result<PageResult<Settlement>> {
        calls += "liquidaciones $cursor"
        return Result.Success(settlementPages.getValue(cursor))
    }

    override suspend fun saveDepositAccount(businessId: String, clabe: String, holder: String) =
        bankResult.also { calls += "cuenta ${clabe.takeLast(4)}" }

    override suspend fun saveTaxData(businessId: String, data: TaxData): Result<Unit> {
        taxSent = data
        return taxResult
    }

    override suspend fun exploreMode() = exploreResult.also { calls += "explorar" }
}
