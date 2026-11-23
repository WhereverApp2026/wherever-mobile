package com.wherever.mobile.testing

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.feature.business.domain.Address
import com.wherever.mobile.feature.business.domain.BusinessDraft
import com.wherever.mobile.feature.business.domain.BusinessOnboardingRepository
import com.wherever.mobile.feature.business.domain.BusinessPhoto
import com.wherever.mobile.feature.business.domain.BusinessState
import com.wherever.mobile.feature.business.domain.Catalog
import com.wherever.mobile.feature.business.domain.DaySchedule
import com.wherever.mobile.feature.business.domain.DocumentType
import com.wherever.mobile.feature.business.domain.Option
import com.wherever.mobile.feature.business.domain.PhotoType
import com.wherever.mobile.feature.photos.domain.PhotoRepository
import com.wherever.mobile.feature.photos.domain.UploadedImage

fun sampleBusinessDraft(step: Int = 1, state: BusinessState = BusinessState.DRAFT, photos: Int = 0, version: Long = 1) =
    BusinessDraft(
        id = "n1",
        name = "Café Raíz",
        category = "cafeteria",
        description = "Café de olla",
        state = state,
        step = step,
        version = version,
        photos = (1..photos).map { BusinessPhoto("f$it", "https://img/$it.jpg") }
    )

/** Alta en memoria: avanza `paso_alta` y la versión como la API. */
class FakeBusinessOnboardingRepository : BusinessOnboardingRepository {
    val calls = mutableListOf<String>()
    var mineResult: Result<List<BusinessDraft>> = Result.Success(emptyList())
    var catalogResult: Result<Catalog> = Result.Success(
        Catalog(listOf(Option("cafeteria", "Cafetería")), listOf(Option("terraza", "Terraza")))
    )
    var submitResult: Result<BusinessDraft>? = null
    var documentResult: Result<Unit> = Result.Success(Unit)

    private fun advance(draft: BusinessDraft, step: Int) =
        Result.Success(draft.copy(step = maxOf(draft.step, step), version = draft.version + 1))

    override suspend fun catalog() = catalogResult

    override suspend fun mine() = mineResult.also { calls += "mine" }

    override suspend fun create(name: String, category: String, description: String): Result<BusinessDraft> {
        calls += "create:$name:$category"
        return Result.Success(sampleBusinessDraft().copy(name = name, category = category, description = description))
    }

    override suspend fun saveLocation(draft: BusinessDraft, address: Address, point: GeoPoint) =
        advance(draft, 2).also { calls += "alta2:v${draft.version}" }

    override suspend fun saveSchedules(draft: BusinessDraft, schedules: List<DaySchedule>) =
        advance(draft, 3).also { calls += "alta3:v${draft.version}" }

    override suspend fun saveOperation(
        draft: BusinessDraft,
        priceLevel: Int,
        averagePrice: Int,
        services: List<String>
    ) = advance(draft, 5).also { calls += "alta5:v${draft.version}:$priceLevel:$averagePrice" }

    override suspend fun addPhoto(draft: BusinessDraft, imageId: String, type: PhotoType): Result<BusinessDraft> {
        calls += "foto:$imageId:${type.apiKey}"
        return Result.Success(draft.copy(photos = draft.photos + BusinessPhoto(imageId, "https://img/$imageId.jpg")))
    }

    override suspend fun confirmPhotos(draft: BusinessDraft) = advance(draft, 4).also { calls += "alta4" }

    override suspend fun uploadDocument(draft: BusinessDraft, type: DocumentType, source: String) =
        documentResult.also { calls += "doc:${type.apiKey}" }

    override suspend fun confirmDocuments(draft: BusinessDraft) = advance(draft, 6).also { calls += "alta6" }

    override suspend fun saveDepositAccount(draft: BusinessDraft, clabe: String, holder: String) =
        Result.Success(Unit).also { calls += "clabe" }

    override suspend fun submit(draft: BusinessDraft): Result<BusinessDraft> {
        calls += "submit"
        return submitResult ?: Result.Success(draft.copy(state = BusinessState.IN_REVIEW))
    }

    override suspend fun suggestDescription(draft: BusinessDraft, words: List<String>) =
        Result.Success("Cafetería con ${words.joinToString()}").also { calls += "ia" }
}

/** Subida de fotos falsa para el alta: devuelve [result] y anota el origen. */
class FakeBusinessPhotoRepository(var result: Result<UploadedImage>) : PhotoRepository {
    val sources = mutableListOf<String>()

    override suspend fun upload(source: String): Result<UploadedImage> = result.also { sources += source }
}
