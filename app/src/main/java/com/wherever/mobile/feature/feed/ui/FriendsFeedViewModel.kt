package com.wherever.mobile.feature.feed.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.feed.domain.FeedRepository
import com.wherever.mobile.feature.feed.domain.Moment
import com.wherever.mobile.feature.friends.domain.FriendSuggestion
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.places.domain.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Un momento listo para pintar: con el nombre del amigo y del lugar ya resueltos. */
data class FeedItem(
    val moment: Moment,
    val authorName: String?,
    val authorPhoto: String?,
    val placeName: String?,
    val time: UiText?
)

data class FriendsFeedUiState(
    val loading: Boolean = true,
    val items: List<FeedItem> = emptyList(),
    val nextCursor: String? = null,
    val loadingMore: Boolean = false,
    val error: UiText? = null,
    val suggestions: List<FriendSuggestion> = emptyList(),
    val sent: Set<String> = emptySet(),
    val sending: Set<String> = emptySet()
) {
    val empty: Boolean get() = !loading && error == null && items.isEmpty()
}

/**
 * Pestaña Amigos de Inicio (11_InicioAmigos y 12_InicioMas, specs/nv-17). El feed no trae el nombre del autor
 * ni el del lugar: se cruzan con la lista de amigos y con la ficha de cada lugar (una vez por lugar).
 */
@HiltViewModel
class FriendsFeedViewModel @Inject constructor(
    private val feed: FeedRepository,
    private val friends: FriendsRepository,
    private val places: PlacesRepository,
    private val clock: Clock
) : ViewModel() {
    private val mutableState = MutableStateFlow(FriendsFeedUiState())
    val state: StateFlow<FriendsFeedUiState> = mutableState.asStateFlow()

    private var started = false
    private val people = mutableMapOf<String, Person>()
    private val placeNames = mutableMapOf<String, String>()

    /** Se llama al mostrar la pestaña: la primera vez carga; después no repite. */
    fun onShown() {
        if (started) return
        started = true
        refresh()
    }

    fun refresh() {
        mutableState.update { it.copy(loading = it.items.isEmpty(), error = null) }
        viewModelScope.launch {
            val (page, _) = coroutineScope {
                val page = async { feed.feed(null) }
                val people = async { loadFriends() }
                val suggestions = async { loadSuggestions() }
                Pair(page.await(), listOf(people, suggestions).awaitAll())
            }
            when (page) {
                is Result.Success -> {
                    val items = enrich(page.data.items)
                    mutableState.update {
                        it.copy(loading = false, items = items, nextCursor = page.data.nextCursor, error = null)
                    }
                }
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = page.error.toUiText()) }
            }
        }
    }

    fun loadMore() {
        val current = state.value
        val cursor = current.nextCursor ?: return
        if (current.loadingMore) return
        mutableState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            when (val page = feed.feed(cursor)) {
                is Result.Success -> {
                    val items = enrich(page.data.items)
                    mutableState.update {
                        it.copy(loadingMore = false, items = it.items + items, nextCursor = page.data.nextCursor)
                    }
                }
                is Result.Failure -> mutableState.update { it.copy(loadingMore = false, error = page.error.toUiText()) }
            }
        }
    }

    /** 12_InicioMas · solicitud enviada: Agregar pasa a Pendiente. */
    fun request(person: Person) {
        if (person.id in state.value.sending || person.id in state.value.sent) return
        mutableState.update { it.copy(sending = it.sending + person.id) }
        viewModelScope.launch {
            val result = friends.request(person.id)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(sending = it.sending - person.id, sent = it.sent + person.id)
                    is Result.Failure -> it.copy(sending = it.sending - person.id, error = result.error.toUiText())
                }
            }
        }
    }

    private suspend fun loadFriends() {
        (friends.list(FriendshipList.FRIENDS) as? Result.Success)?.data?.items?.forEach {
            people[it.person.id] =
                it.person
        }
    }

    private suspend fun loadSuggestions() {
        (friends.suggestions() as? Result.Success)?.let { result ->
            mutableState.update { it.copy(suggestions = result.data) }
        }
    }

    private suspend fun enrich(moments: List<Moment>): List<FeedItem> {
        val missing = moments.mapNotNull { it.placeId }.distinct().filterNot { it in placeNames }
        coroutineScope {
            missing.map { id ->
                async { (places.detail(id) as? Result.Success)?.data?.name?.let { placeNames[id] = it } }
            }.awaitAll()
        }
        val now = clock.instant()
        return moments.map { moment ->
            val person = people[moment.author.id]
            FeedItem(
                moment = moment,
                authorName = person?.name,
                authorPhoto = moment.author.photoUrl ?: person?.photoUrl,
                placeName = moment.placeId?.let(placeNames::get),
                time = relativeTime(moment.createdAt, now)
            )
        }
    }
}

/** Acciones de la pestaña Amigos; la navegación la resuelve quien la muestra. */
data class FriendsFeedActions(
    val onMoment: (String) -> Unit,
    val onPlace: (String) -> Unit,
    val onPlan: (String) -> Unit,
    val onPerson: (String) -> Unit,
    val onAddFriends: () -> Unit,
    val onRequest: (Person) -> Unit,
    val onRetry: () -> Unit,
    val onLoadMore: () -> Unit
)
