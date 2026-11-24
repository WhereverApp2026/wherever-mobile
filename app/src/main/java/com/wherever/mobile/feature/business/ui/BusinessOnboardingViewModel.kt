package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.location.GeoPoint
import com.wherever.mobile.core.location.LocationProvider
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.Address
import com.wherever.mobile.feature.business.domain.BusinessDraft
import com.wherever.mobile.feature.business.domain.BusinessOnboardingRepository
import com.wherever.mobile.feature.business.domain.BusinessState
import com.wherever.mobile.feature.business.domain.Catalog
import com.wherever.mobile.feature.business.domain.DAYS_IN_WEEK
import com.wherever.mobile.feature.business.domain.DESCRIPTION_MAX
import com.wherever.mobile.feature.business.domain.DaySchedule
import com.wherever.mobile.feature.business.domain.DocumentType
import com.wherever.mobile.feature.business.domain.MAX_PHOTOS
import com.wherever.mobile.feature.business.domain.MIN_PHOTOS
import com.wherever.mobile.feature.business.domain.NAME_MAX
import com.wherever.mobile.feature.business.domain.PRICE_LEVELS
import com.wherever.mobile.feature.business.domain.PhotoType
import com.wherever.mobile.feature.business.domain.isValidBusinessName
import com.wherever.mobile.feature.business.domain.isValidClabe
import com.wherever.mobile.feature.business.domain.isValidRange
import com.wherever.mobile.feature.photos.domain.ImageState
import com.wherever.mobile.feature.photos.domain.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Pasos visibles: 43_NegRegistro, 44_NegUbic, 45_NegOper, 46_NegVerif y 47_NegListo. 42 se salta con sesión. */
enum class OnboardingStep { REGISTER, LOCATION, OPERATION, VERIFY, DONE }

private const val STEP_OPERATION_DONE = 5
private const val STEP_LOCATION_DONE = 2

/** Horario inicial: lunes a sábado de 9 a 20, domingo cerrado. Solo es el punto de partida del formulario. */
private fun defaultWeek() = (0 until DAYS_IN_WEEK).map { day ->
    if (day == 0) DaySchedule(day) else DaySchedule(day, "09:00", "20:00", closed = false)
}

data class OnboardingUiState(
    val loading: Boolean = true,
    val loadError: UiText? = null,
    val step: OnboardingStep = OnboardingStep.REGISTER,
    val catalog: Catalog? = null,
    val draft: BusinessDraft? = null,
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val aiWords: String = "",
    val address: Address = Address(),
    val point: GeoPoint? = null,
    val locating: Boolean = false,
    val schedules: List<DaySchedule> = defaultWeek(),
    val priceLevel: Int = 0,
    val averagePrice: String = "",
    val services: Set<String> = emptySet(),
    val uploadingPhoto: Boolean = false,
    /** Consentimiento expreso para INE, comprobante y CLABE; ya dado si hay documentos guardados. */
    val dataConsent: Boolean = false,
    val savingConsent: Boolean = false,
    val uploadedDocuments: Set<DocumentType> = emptySet(),
    val uploadingDocument: DocumentType? = null,
    val clabe: String = "",
    val holder: String = "",
    /** Lo que falta según el 422 de enviar-revision, tal cual lo manda la API. */
    val missing: List<String> = emptyList(),
    val saving: Boolean = false,
    val error: UiText? = null,
    val message: UiText? = null
) {
    val photoCount: Int get() = draft?.photos?.size ?: 0

    val documentsReady: Boolean
        get() = (
            draft?.hasIne == true ||
                (DocumentType.INE_FRONT in uploadedDocuments && DocumentType.INE_BACK in uploadedDocuments)
            ) &&
            (draft?.hasProofOfAddress == true || DocumentType.PROOF_OF_ADDRESS in uploadedDocuments)

    val hasDataConsent: Boolean
        get() = dataConsent ||
            uploadedDocuments.isNotEmpty() ||
            draft?.hasIne == true ||
            draft?.hasProofOfAddress == true

    val clabeValid: Boolean get() = clabe.isEmpty() || isValidClabe(clabe)

    /** El botón principal de cada paso se habilita solo con el paso válido. */
    val canContinue: Boolean
        get() = !saving &&
            when (step) {
                OnboardingStep.REGISTER ->
                    draft != null ||
                        (isValidBusinessName(name) && category.isNotBlank() && description.length <= DESCRIPTION_MAX)
                OnboardingStep.LOCATION -> address.complete && point != null
                OnboardingStep.OPERATION ->
                    priceLevel in 1..PRICE_LEVELS &&
                        (averagePrice.toIntOrNull() ?: 0) > 0 &&
                        schedules.any { !it.closed } &&
                        schedules.all { it.closed || it.isValidRange() }
                OnboardingStep.VERIFY ->
                    photoCount >= MIN_PHOTOS &&
                        documentsReady &&
                        clabeValid &&
                        (clabe.isEmpty() || holder.isNotBlank())
                OnboardingStep.DONE -> false
            }
}

/** Alta del negocio, pasos 2 a 6 de la API y el envío a revisión (specs/nv-34). */
@Suppress("TooManyFunctions") // Un evento por control de los cinco pasos del alta.
@HiltViewModel
class BusinessOnboardingViewModel @Inject constructor(
    private val repository: BusinessOnboardingRepository,
    private val photos: PhotoRepository,
    private val location: LocationProvider
) : ViewModel() {
    private val mutableState = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = mutableState.asStateFlow()
    private val exitChannel = Channel<Unit>(Channel.BUFFERED)

    /** Guardar y salir: lo guardado vive en la API (`paso_alta`) y se retoma al volver. */
    val exit: Flow<Unit> = exitChannel.receiveAsFlow()

    init {
        load()
    }

    /** Retoma el alta abierta en el paso que sigue a `paso_alta`. */
    fun load() {
        mutableState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            val catalog = async { repository.catalog() }
            val mine = repository.mine()
            val categories = catalog.await()
            when {
                categories is Result.Failure -> mutableState.update {
                    it.copy(loading = false, loadError = categories.error.toUiText())
                }
                mine is Result.Failure -> mutableState.update {
                    it.copy(loading = false, loadError = mine.error.toUiText())
                }
                else -> {
                    val open = (mine as Result.Success).data.firstOrNull { it.state != BusinessState.PUBLISHED }
                    mutableState.update {
                        val base = it.copy(loading = false, catalog = (categories as Result.Success).data)
                        if (open == null) base else base.resume(open)
                    }
                }
            }
        }
    }

    // --- 43_NegRegistro ---

    fun onName(value: String) = mutableState.update { it.copy(name = value.take(NAME_MAX), error = null) }

    fun onCategory(id: String) = mutableState.update { it.copy(category = id, error = null) }

    fun onDescription(value: String) = mutableState.update { it.copy(description = value.take(DESCRIPTION_MAX)) }

    fun onAiWords(value: String) = mutableState.update { it.copy(aiWords = value) }

    /** «Que la IA me ayude» (3 palabras); solo con el borrador ya creado. */
    fun suggestDescription() {
        val draft = state.value.draft ?: return
        val words = state.value.aiWords.split(',', ' ').map { it.trim() }.filter { it.isNotEmpty() }
        if (words.size != AI_WORDS) {
            mutableState.update { it.copy(error = UiText.Res(R.string.business_ai_words_hint)) }
            return
        }
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.suggestDescription(draft, words)) {
                is Result.Success -> mutableState.update {
                    it.copy(saving = false, description = result.data.take(DESCRIPTION_MAX))
                }
                is Result.Failure -> fail(result.error)
            }
        }
    }

    // --- 44_NegUbic ---

    fun onAddress(address: Address) = mutableState.update { it.copy(address = address, error = null) }

    /** «Estoy en mi negocio»: la ubicación del teléfono (permiso pedido en contexto por la pantalla). */
    fun locate() {
        mutableState.update { it.copy(locating = true, error = null) }
        viewModelScope.launch {
            val point = location.current()
            mutableState.update {
                if (point == null) {
                    it.copy(locating = false, error = UiText.Res(R.string.business_location_unavailable))
                } else {
                    it.copy(locating = false, point = point)
                }
            }
        }
    }

    // --- 45_NegOper ---

    fun onSchedule(schedule: DaySchedule) = mutableState.update { state ->
        state.copy(schedules = state.schedules.map { if (it.day == schedule.day) schedule else it })
    }

    fun onPriceLevel(level: Int) = mutableState.update { it.copy(priceLevel = level) }

    fun onAveragePrice(value: String) = mutableState.update {
        it.copy(averagePrice = value.filter(Char::isDigit).take(PRICE_DIGITS))
    }

    fun onToggleService(id: String) = mutableState.update {
        it.copy(services = if (id in it.services) it.services - id else it.services + id)
    }

    // --- 46_NegVerif ---

    fun onPhotoPicked(source: String) {
        val draft = state.value.draft ?: return
        if (state.value.uploadingPhoto || state.value.photoCount >= MAX_PHOTOS) return
        mutableState.update { it.copy(uploadingPhoto = true, error = null) }
        viewModelScope.launch {
            val uploaded = photos.upload(source)
            val result = when {
                uploaded is Result.Failure -> uploaded
                (uploaded as Result.Success).data.state != ImageState.PUBLISHED ->
                    Result.Failure(AppError.Validation(apiMessage = null))
                else -> repository.addPhoto(
                    draft,
                    uploaded.data.id,
                    if (state.value.photoCount ==
                        0
                    ) {
                        PhotoType.FACADE
                    } else {
                        PhotoType.INTERIOR
                    }
                )
            }
            when (result) {
                is Result.Success -> mutableState.update { it.copy(uploadingPhoto = false, draft = result.data) }
                is Result.Failure -> mutableState.update {
                    it.copy(uploadingPhoto = false, error = UiText.Res(R.string.business_photo_failed))
                }
            }
        }
    }

    /** Marcar la casilla registra el consentimiento en la API; no se puede retirar desde aquí (va en Privacidad). */
    fun onDataConsent(checked: Boolean) {
        val current = state.value
        if (!checked || current.dataConsent || current.savingConsent) return
        mutableState.update { it.copy(savingConsent = true, error = null) }
        viewModelScope.launch {
            val result = repository.acceptBusinessDataConsent()
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(savingConsent = false, dataConsent = true)
                    is Result.Failure -> it.copy(savingConsent = false, error = result.error.toUiText())
                }
            }
        }
    }

    fun onDocumentPicked(type: DocumentType, source: String) {
        val draft = state.value.draft ?: return
        if (state.value.uploadingDocument != null || !state.value.hasDataConsent) return
        mutableState.update { it.copy(uploadingDocument = type, error = null) }
        viewModelScope.launch {
            when (val result = repository.uploadDocument(draft, type, source)) {
                is Result.Success -> mutableState.update {
                    it.copy(uploadingDocument = null, uploadedDocuments = it.uploadedDocuments + type)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(
                        uploadingDocument = null,
                        error = if (result.error is AppError.Validation && result.error.apiMessage == null) {
                            UiText.Res(R.string.business_document_invalid)
                        } else {
                            result.error.toUiText()
                        }
                    )
                }
            }
        }
    }

    fun onClabe(value: String) = mutableState.update { it.copy(clabe = value.filter(Char::isDigit).take(CLABE_DIGITS)) }

    fun onHolder(value: String) = mutableState.update { it.copy(holder = value) }

    // --- Avance ---

    /** Botón principal de cada paso. */
    fun next() {
        val current = state.value
        if (!current.canContinue) return
        mutableState.update { it.copy(saving = true, error = null, missing = emptyList()) }
        viewModelScope.launch {
            when (current.step) {
                OnboardingStep.REGISTER -> register(current)
                OnboardingStep.LOCATION -> save(OnboardingStep.OPERATION) {
                    repository.saveLocation(it, current.address, checkNotNull(current.point))
                }
                OnboardingStep.OPERATION -> save(OnboardingStep.VERIFY) { draft ->
                    when (val schedules = repository.saveSchedules(draft, current.schedules)) {
                        is Result.Failure -> schedules
                        is Result.Success -> repository.saveOperation(
                            schedules.data,
                            current.priceLevel,
                            current.averagePrice.toInt(),
                            current.services.toList()
                        )
                    }
                }
                OnboardingStep.VERIFY -> submit(current)
                OnboardingStep.DONE -> Unit
            }
        }
    }

    fun back() {
        val previous = when (state.value.step) {
            OnboardingStep.REGISTER, OnboardingStep.DONE -> null
            OnboardingStep.LOCATION -> OnboardingStep.REGISTER
            OnboardingStep.OPERATION -> OnboardingStep.LOCATION
            OnboardingStep.VERIFY -> OnboardingStep.OPERATION
        }
        if (previous == null) {
            exitChannel.trySend(Unit)
        } else {
            mutableState.update { it.copy(step = previous, error = null) }
        }
    }

    fun saveAndExit() {
        exitChannel.trySend(Unit)
    }

    /** «Editar mi solicitud» (rechazada): vuelve al primer paso que falta. */
    fun edit() = mutableState.update { state ->
        state.draft?.let { state.resume(it.copy(state = BusinessState.DRAFT)) }
            ?: state
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }

    private suspend fun register(current: OnboardingUiState) {
        if (current.draft != null) {
            mutableState.update { it.copy(saving = false, step = OnboardingStep.LOCATION) }
            return
        }
        when (val result = repository.create(current.name, current.category, current.description)) {
            is Result.Success -> mutableState.update {
                it.copy(saving = false, draft = result.data, step = OnboardingStep.LOCATION)
            }
            is Result.Failure -> fail(result.error)
        }
    }

    private suspend fun save(next: OnboardingStep, block: suspend (BusinessDraft) -> Result<BusinessDraft>) {
        val draft = state.value.draft ?: return
        when (val result = block(draft)) {
            is Result.Success -> mutableState.update { it.copy(saving = false, draft = result.data, step = next) }
            is Result.Failure -> fail(result.error)
        }
    }

    /** Paso 4 (fotos), paso 6 (documentos), CLABE si se escribió y el envío. */
    private suspend fun submit(current: OnboardingUiState) {
        var draft = current.draft ?: return
        for (step in listOf(repository::confirmPhotos, repository::confirmDocuments)) {
            when (val result = step(draft)) {
                is Result.Success -> draft = result.data
                is Result.Failure -> return fail(result.error)
            }
        }
        if (current.clabe.isNotEmpty()) {
            val saved = repository.saveDepositAccount(draft, current.clabe, current.holder)
            if (saved is Result.Failure) return fail(saved.error)
        }
        when (val result = repository.submit(draft)) {
            is Result.Success -> mutableState.update {
                it.copy(saving = false, draft = result.data, step = OnboardingStep.DONE)
            }
            is Result.Failure -> fail(result.error, draft)
        }
    }

    /** 422 de enviar-revision: la lista de faltantes tal cual. 409: la versión cambió, se recarga. */
    private fun fail(error: AppError, draft: BusinessDraft? = null) {
        val missing = (error as? AppError.Validation)?.fields.orEmpty().map { it.message }
        mutableState.update {
            it.copy(
                saving = false,
                draft = draft ?: it.draft,
                missing = missing,
                error = if (error is AppError.Conflict) UiText.Res(R.string.business_conflict) else error.toUiText()
            )
        }
        if (error is AppError.Conflict) load()
    }

    private companion object {
        const val AI_WORDS = 3
        const val PRICE_DIGITS = 6
        const val CLABE_DIGITS = 18
    }
}

/** Siguiente paso según `paso_alta` y el estado del alta. */
private fun OnboardingUiState.resume(draft: BusinessDraft): OnboardingUiState {
    val step = when {
        draft.state == BusinessState.IN_REVIEW -> OnboardingStep.DONE
        draft.step < STEP_LOCATION_DONE -> OnboardingStep.LOCATION
        draft.step < STEP_OPERATION_DONE -> OnboardingStep.OPERATION
        else -> OnboardingStep.VERIFY
    }
    return copy(
        draft = draft,
        step = step,
        name = draft.name,
        category = draft.category,
        description = draft.description,
        address = if (draft.address.complete) draft.address else address,
        schedules = if (draft.schedules.isNotEmpty()) draft.schedules else schedules,
        priceLevel = if (draft.priceLevel > 0) draft.priceLevel else priceLevel,
        averagePrice = if (draft.averagePrice > 0) draft.averagePrice.toString() else averagePrice,
        services = if (draft.services.isNotEmpty()) draft.services.toSet() else services
    )
}
