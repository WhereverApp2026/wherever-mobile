package com.wherever.mobile.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.profile.domain.Badge
import com.wherever.mobile.feature.profile.domain.BadgesRepository
import com.wherever.mobile.feature.profile.domain.LevelProgress
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BadgesUiState(
    val loading: Boolean = true,
    val progress: LevelProgress? = null,
    val earned: List<Badge> = emptyList(),
    val locked: List<Badge> = emptyList(),
    val error: UiText? = null,
    /** Número del nivel de la API (círculo dorado de 83_Insignias). */
    val levelNumber: Int? = null
)

/** 83_Insignias: nivel y puntos del perfil (Room, refrescado de `/usuarios/yo`) e insignias. */
@HiltViewModel
class BadgesViewModel @Inject constructor(
    private val profiles: ProfileRepository,
    private val badges: BadgesRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(BadgesUiState())
    val state: StateFlow<BadgesUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            profiles.observeProfile().collect { profile ->
                // Sin la API, la escalera local; en cuanto llega el nivel del backend, manda ese.
                if (profile != null && state.value.levelNumber == null) {
                    mutableState.update { it.copy(progress = LevelProgress.of(profile.points, profile.level)) }
                }
            }
        }
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val refreshed = profiles.refresh()
            when (val result = badges.badges()) {
                is Result.Success -> mutableState.update { s ->
                    s.copy(
                        loading = false,
                        earned = result.data.filter { it.earned },
                        locked = result.data.filterNot { it.earned },
                        error = (refreshed as? Result.Failure)?.error?.toUiText(),
                        progress = badges.lastLevel()?.toProgress() ?: s.progress,
                        levelNumber = badges.lastLevel()?.number ?: s.levelNumber
                    )
                }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }
}
