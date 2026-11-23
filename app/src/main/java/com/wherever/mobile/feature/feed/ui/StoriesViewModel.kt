package com.wherever.mobile.feature.feed.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.feed.domain.MomentSocialRepository
import com.wherever.mobile.feature.feed.domain.StoryMoment
import com.wherever.mobile.feature.feed.domain.StoryPerson
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoriesUiState(
    val loading: Boolean = true,
    val people: List<StoryPerson> = emptyList(),
    val error: UiText? = null
)

/** Fila de historias de Inicio (últimas 24 h de amigos, backend nv-84). */
@HiltViewModel
class StoriesViewModel @Inject constructor(private val social: MomentSocialRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(StoriesUiState())
    val state: StateFlow<StoriesUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val result = social.stories()
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(loading = false, people = result.data, error = null)
                    // Sin historias no se muestra la fila: el error no tapa el feed.
                    is Result.Failure -> it.copy(loading = false, error = result.error.toUiText())
                }
            }
        }
    }
}

data class StoryViewerUiState(
    val loading: Boolean = true,
    val people: List<StoryPerson> = emptyList(),
    val person: Int = 0,
    val moment: Int = 0,
    val finished: Boolean = false,
    val error: UiText? = null
) {
    val current: StoryPerson? get() = people.getOrNull(person)
    val currentMoment: StoryMoment? get() = current?.moments?.getOrNull(moment)
}

/** Visor de historias: empieza en la persona elegida y sigue con las demás. */
@HiltViewModel
class StoryViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val social: MomentSocialRepository
) : ViewModel() {
    private val authorId: String = savedStateHandle.get<String>("authorId").orEmpty()
    private val mutableState = MutableStateFlow(StoryViewerUiState())
    val state: StateFlow<StoryViewerUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = social.stories()) {
                is Result.Success -> {
                    val start = result.data.indexOfFirst { it.authorId == authorId }.coerceAtLeast(0)
                    mutableState.update {
                        it.copy(loading = false, people = result.data, person = start, finished = result.data.isEmpty())
                    }
                }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }

    fun next() = mutableState.update { s ->
        val person = s.current ?: return@update s.copy(finished = true)
        when {
            s.moment + 1 < person.moments.size -> s.copy(moment = s.moment + 1)
            s.person + 1 < s.people.size -> s.copy(person = s.person + 1, moment = 0)
            else -> s.copy(finished = true)
        }
    }

    fun previous() = mutableState.update { s ->
        when {
            s.moment > 0 -> s.copy(moment = s.moment - 1)
            s.person > 0 -> s.copy(
                person = s.person - 1,
                moment = (s.people[s.person - 1].moments.size - 1).coerceAtLeast(0)
            )
            else -> s
        }
    }
}
