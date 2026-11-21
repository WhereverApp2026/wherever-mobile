package com.wherever.mobile.feature.friends.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.friends.domain.FriendQr
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.QrImageRepository
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MyCodeTab { MY_CODE, SCAN }

data class MyCodeUiState(
    val tab: MyCodeTab = MyCodeTab.MY_CODE,
    val loading: Boolean = true,
    val name: String = "",
    val username: String? = null,
    val photoUrl: String? = null,
    val code: String? = null,
    /** Sin perfil en Room y la API falló (sin red, 5xx...). */
    val loadError: UiText? = null,
    val sharing: Boolean = false,
    val saving: Boolean = false,
    val looking: Boolean = false,
    val scanError: UiText? = null,
    val message: UiText? = null,
    /** Evento: abrir la hoja de compartir con este `content://`. */
    val shareUri: String? = null,
    /** Evento: abrir el perfil de la persona escaneada. */
    val foundPersonId: String? = null
) {
    /** Perfil cargado sin código: la API aún no lo generó (estado vacío). */
    val empty: Boolean
        get() = !loading && loadError == null && code == null

    val payload: String?
        get() = code?.let(FriendQr::payload)
}

/** 25_MiCodigo: mi QR para que me agreguen y escanear el de otra persona (US-234). */
@HiltViewModel
class MyCodeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profiles: ProfileRepository,
    private val friends: FriendsRepository,
    private val images: QrImageRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        MyCodeUiState(tab = if (savedStateHandle.get<Boolean>("scan") == true) MyCodeTab.SCAN else MyCodeTab.MY_CODE)
    )
    val state: StateFlow<MyCodeUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            profiles.observeProfile().collect { profile ->
                if (profile != null) {
                    mutableState.update {
                        it.copy(
                            name = profile.name,
                            username = profile.username,
                            photoUrl = profile.photoUrl,
                            code = profile.friendCode?.takeIf(FriendQr::isValidCode),
                            loading = it.loading && profile.friendCode == null
                        )
                    }
                }
            }
        }
        refresh()
    }

    /** `GET /usuarios/yo`: el código llega con el perfil; Room conserva el último para usarlo sin red. */
    fun refresh() {
        mutableState.update { it.copy(loading = it.code == null, loadError = null) }
        viewModelScope.launch {
            val result = profiles.refresh()
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(loading = false)
                    // Con código guardado se sigue mostrando: el QR no cambia y sirve sin red.
                    is Result.Failure -> it.copy(
                        loading = false,
                        loadError = if (it.code == null) result.error.toUiText() else null
                    )
                }
            }
        }
    }

    fun selectTab(tab: MyCodeTab) = mutableState.update { it.copy(tab = tab, scanError = null) }

    fun share() {
        val payload = state.value.payload ?: return
        if (state.value.sharing) return
        mutableState.update { it.copy(sharing = true, message = null) }
        viewModelScope.launch {
            val result = images.shareable(payload)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(sharing = false, shareUri = result.data)
                    is Result.Failure -> it.copy(sharing = false, message = UiText.Res(R.string.my_code_image_error))
                }
            }
        }
    }

    fun shareHandled() = mutableState.update { it.copy(shareUri = null) }

    fun save() {
        val payload = state.value.payload ?: return
        if (state.value.saving) return
        mutableState.update { it.copy(saving = true, message = null) }
        viewModelScope.launch {
            val result = images.saveToGallery(payload)
            mutableState.update {
                it.copy(
                    saving = false,
                    message = when {
                        result is Result.Success -> UiText.Res(R.string.my_code_saved)
                        // Android 8 y 9: guardar pediría permiso de almacenamiento; se comparte.
                        (result as Result.Failure).error is AppError.Forbidden -> UiText.Res(R.string.my_code_save_old)
                        else -> UiText.Res(R.string.my_code_image_error)
                    }
                )
            }
        }
    }

    /** Resultado de la cámara: `null` si se canceló. El contenido no es confiable (specs/nv-20 §5). */
    fun onScanned(raw: String?) {
        if (raw == null || state.value.looking) return
        val code = FriendQr.parse(raw)
        if (code == null) {
            mutableState.update { it.copy(scanError = UiText.Res(R.string.my_code_scan_invalid)) }
            return
        }
        if (code == state.value.code) {
            mutableState.update { it.copy(scanError = UiText.Res(R.string.my_code_scan_own)) }
            return
        }
        mutableState.update { it.copy(looking = true, scanError = null) }
        viewModelScope.launch {
            val result = friends.byCode(code)
            mutableState.update {
                when {
                    result is Result.Success -> it.copy(looking = false, foundPersonId = result.data.person.id)
                    (result as Result.Failure).error is AppError.NotFound ->
                        it.copy(looking = false, scanError = UiText.Res(R.string.friends_code_not_found))
                    else -> it.copy(looking = false, scanError = result.error.toUiText())
                }
            }
        }
    }

    /** Sin cámara se sigue pudiendo agregar escribiendo el código (constitución §6). */
    fun onCameraDenied() = mutableState.update { it.copy(scanError = UiText.Res(R.string.my_code_camera_denied)) }

    fun personOpened() = mutableState.update { it.copy(foundPersonId = null) }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
