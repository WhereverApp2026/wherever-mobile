package com.wherever.mobile.feature.memories.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.memories.domain.MAX_TAGGED
import com.wherever.mobile.feature.memories.domain.MOMENT_TEXT_MAX
import com.wherever.mobile.feature.memories.domain.MemoriesRepository
import com.wherever.mobile.feature.memories.domain.MomentDraft
import com.wherever.mobile.feature.memories.domain.Postcard
import com.wherever.mobile.feature.memories.domain.PostcardState
import com.wherever.mobile.feature.memories.domain.Visibility
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

/** Pestañas de 70_CrearPub. Plan queda fuera (sin endpoint, D-31a). */
enum class MomentTab { PHOTO, PLAN, REVIEW }

data class CreateMomentUiState(
    val tab: MomentTab = MomentTab.PHOTO,
    val loading: Boolean = true,
    /** Postales listas para publicar (`estado = lista`). */
    val postcards: List<Postcard> = emptyList(),
    val selected: Postcard? = null,
    val text: String = "",
    val friends: List<Person> = emptyList(),
    val tagged: Set<String> = emptySet(),
    val visibility: Visibility = Visibility.FRIENDS,
    val choosingPostcard: Boolean = false,
    val choosingFriends: Boolean = false,
    val publishing: Boolean = false,
    val error: UiText? = null,
    val loadError: UiText? = null
) {
    val canPublish: Boolean get() = selected != null && text.length <= MOMENT_TEXT_MAX && !publishing
}

/** 70_CrearPub, pestaña Foto: postal, texto ≤ 280, etiquetados ≤ 10 y visibilidad (specs/nv-31). */
@HiltViewModel
class CreateMomentViewModel @Inject constructor(
    private val repository: MemoriesRepository,
    private val friends: FriendsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val preselected: String = savedStateHandle.get<String>("postcardId").orEmpty()
    private val mutableState = MutableStateFlow(CreateMomentUiState())
    val state: StateFlow<CreateMomentUiState> = mutableState.asStateFlow()
    private val publishedChannel = Channel<Unit>(Channel.BUFFERED)
    val published: Flow<Unit> = publishedChannel.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            val people = async { friends.list(FriendshipList.FRIENDS) }
            when (val page = repository.postcards()) {
                is Result.Success -> {
                    val ready = page.data.items.filter { it.state == PostcardState.READY && it.imageUrl != null }
                    // Sin amigos (o si fallan) solo se oculta Etiquetar; la publicación sigue.
                    val accepted = (people.await() as? Result.Success)?.data?.items.orEmpty()
                        .filter { it.accepted }
                        .map { it.person }
                    mutableState.update { state ->
                        state.copy(
                            loading = false,
                            postcards = ready,
                            selected = ready.firstOrNull { it.id == preselected } ?: ready.firstOrNull(),
                            friends = accepted
                        )
                    }
                }
                is Result.Failure -> {
                    people.cancel()
                    mutableState.update { it.copy(loading = false, loadError = page.error.toUiText()) }
                }
            }
        }
    }

    fun onTab(tab: MomentTab) = mutableState.update { it.copy(tab = tab) }

    fun onText(value: String) = mutableState.update { it.copy(text = value.take(MOMENT_TEXT_MAX), error = null) }

    fun openPostcards() = mutableState.update { it.copy(choosingPostcard = true) }

    fun closePostcards() = mutableState.update { it.copy(choosingPostcard = false) }

    fun onPostcard(postcard: Postcard) = mutableState.update { it.copy(selected = postcard, choosingPostcard = false) }

    fun openFriends() = mutableState.update { it.copy(choosingFriends = true) }

    fun closeFriends() = mutableState.update { it.copy(choosingFriends = false) }

    /** Marca o desmarca a un amigo; no pasa de 10 (la API responde 422). */
    fun onToggleFriend(id: String) = mutableState.update {
        when {
            id in it.tagged -> it.copy(tagged = it.tagged - id, error = null)
            it.tagged.size >= MAX_TAGGED -> it.copy(error = UiText.Res(R.string.memories_tag_limit, listOf(MAX_TAGGED)))
            else -> it.copy(tagged = it.tagged + id, error = null)
        }
    }

    fun onVisibility(visibility: Visibility) = mutableState.update { it.copy(visibility = visibility) }

    /** Un solo envío aunque se toque dos veces. */
    fun publish() {
        val current = state.value
        val postcard = current.selected ?: return
        if (!current.canPublish) return
        mutableState.update { it.copy(publishing = true, error = null) }
        viewModelScope.launch {
            val draft = MomentDraft(postcard.id, current.text, current.visibility, current.tagged.toList())
            when (val result = repository.createMoment(draft)) {
                is Result.Success -> {
                    mutableState.update { it.copy(publishing = false) }
                    publishedChannel.send(Unit)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(publishing = false, error = result.error.toUiText())
                }
            }
        }
    }
}
