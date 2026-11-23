package com.wherever.mobile.feature.feed.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.feed.domain.FeedRepository
import com.wherever.mobile.feature.friends.domain.FriendsRepository
import com.wherever.mobile.feature.places.domain.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MomentDetailUiState(
    val loading: Boolean = true,
    val item: FeedItem? = null,
    val error: UiText? = null,
    /** Borrado, en moderación o sin permiso: la API responde 404 igual en todos los casos. */
    val gone: Boolean = false
)

/** 17_PostDetalle: un momento con su foto, texto, lugar y plan (sin comentarios ni reacciones: no hay API). */
@HiltViewModel
class MomentDetailViewModel @Inject constructor(
    private val feed: FeedRepository,
    private val friends: FriendsRepository,
    private val places: PlacesRepository,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val momentId: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(MomentDetailUiState())
    val state: StateFlow<MomentDetailUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = feed.moment(momentId)) {
                is Result.Success -> {
                    val moment = result.data
                    val person = async { (friends.person(moment.author.id) as? Result.Success)?.data?.person }
                    val place = async {
                        moment.placeId?.let { (places.detail(it) as? Result.Success)?.data?.name }
                    }
                    val author = person.await()
                    val item = FeedItem(
                        moment = moment,
                        authorName = if (moment.own) null else author?.name,
                        authorPhoto = moment.author.photoUrl ?: author?.photoUrl,
                        placeName = place.await(),
                        time = relativeTime(moment.createdAt, clock.instant())
                    )
                    mutableState.update { it.copy(loading = false, item = item) }
                }
                is Result.Failure -> mutableState.update {
                    if (result.error is AppError.NotFound) {
                        it.copy(loading = false, gone = true, error = UiText.Res(R.string.feed_moment_gone))
                    } else {
                        it.copy(loading = false, error = result.error.toUiText())
                    }
                }
            }
        }
    }
}

data class MomentNavigation(
    val onBack: () -> Unit,
    val onPlace: (String) -> Unit,
    val onPlan: (String) -> Unit,
    /** Perfil de quien reaccionó (13_Reacciones). */
    val onPerson: (String) -> Unit = {}
)
