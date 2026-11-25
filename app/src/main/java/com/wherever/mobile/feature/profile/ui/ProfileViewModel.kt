package com.wherever.mobile.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.designsystem.CreateOption
import com.wherever.mobile.core.designsystem.MainTab
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.memories.domain.MemoriesRepository
import com.wherever.mobile.feature.memories.domain.PostcardState
import com.wherever.mobile.feature.places.domain.FavoritesRepository
import com.wherever.mobile.feature.plans.domain.PlansRepository
import com.wherever.mobile.feature.plans.domain.PlansTab
import com.wherever.mobile.feature.profile.domain.Badge
import com.wherever.mobile.feature.profile.domain.BadgesRepository
import com.wherever.mobile.feature.profile.domain.ProfileExtras
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import com.wherever.mobile.feature.profile.domain.UserProfile
import com.wherever.mobile.feature.reviews.domain.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Pestañas de 81_Perfil. */
enum class ProfileTab { POSTS, PLANS, SAVED }

/** Una celda de la rejilla: postal, plan o lugar guardado. */
data class ProfileTile(val id: String, val title: String, val imageUrl: String?, val subtitle: String? = null)

/** Insignias que se muestran como chips: las dos primeras ganadas y cuántas más hay. */
data class BadgeChips(val shown: List<Badge>, val more: Int)

data class ProfileUiState(
    val profile: UserProfile? = null,
    val extras: ProfileExtras? = null,
    val loading: Boolean = true,
    val error: UiText? = null,
    val badges: BadgeChips? = null,
    /** Visitas con check-in que aún no tienen reseña: «N nuevas» en Mis reseñas. */
    val pendingReviews: Int = 0,
    val tab: ProfileTab = ProfileTab.POSTS,
    val tiles: Map<ProfileTab, List<ProfileTile>> = emptyMap(),
    val tabLoading: Boolean = false,
    val tabError: UiText? = null
) {
    val currentTiles: List<ProfileTile>?
        get() = tiles[tab]
}

/** 81_Perfil (specs/nv-14): datos de Room, contadores y bio de la API, insignias, reseñas por hacer y pestañas. */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val badges: BadgesRepository,
    private val reviews: ReviewRepository,
    private val memories: MemoriesRepository,
    private val plans: PlansRepository,
    private val favorites: FavoritesRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProfile().collect { profile -> mutableState.update { it.copy(profile = profile) } }
        }
        viewModelScope.launch {
            repository.observeExtras().collect { extras -> mutableState.update { it.copy(extras = extras) } }
        }
        refresh()
    }

    /** Al volver a la pantalla: perfil, insignias, reseñas por hacer y la pestaña abierta. */
    fun refresh() {
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = repository.refresh()
            mutableState.update {
                it.copy(loading = false, error = (result as? Result.Failure)?.error?.toUiText())
            }
        }
        viewModelScope.launch {
            (badges.badges() as? Result.Success)?.data?.let { list ->
                val earned = list.filter(Badge::earned)
                mutableState.update {
                    it.copy(
                        badges = BadgeChips(earned.take(SHOWN_BADGES), (earned.size - SHOWN_BADGES).coerceAtLeast(0))
                    )
                }
            }
        }
        viewModelScope.launch {
            (reviews.pending() as? Result.Success)?.data?.let { list ->
                mutableState.update { it.copy(pendingReviews = list.size) }
            }
        }
        loadTab(state.value.tab)
    }

    fun selectTab(tab: ProfileTab) {
        mutableState.update { it.copy(tab = tab, tabError = null) }
        if (tab !in state.value.tiles) loadTab(tab)
    }

    private fun loadTab(tab: ProfileTab) {
        mutableState.update { it.copy(tabLoading = it.tiles[tab] == null, tabError = null) }
        viewModelScope.launch {
            val result: Result<List<ProfileTile>> = when (tab) {
                ProfileTab.POSTS -> memories.postcards().mapTiles { page ->
                    page.items.filter { it.state == PostcardState.READY }
                        .map { ProfileTile(it.id, it.text, it.imageUrl) }
                }
                ProfileTab.PLANS -> plans.list(PlansTab.UPCOMING).mapTiles { list ->
                    list.map { ProfileTile(it.id, it.name, null, it.date) }
                }
                ProfileTab.SAVED -> favorites.favorites().mapTiles { list ->
                    list.map { ProfileTile(it.id, it.name, it.photoUrl, it.category) }
                }
            }
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(tabLoading = false, tiles = it.tiles + (tab to result.data))
                    is Result.Failure -> it.copy(tabLoading = false, tabError = result.error.toUiText())
                }
            }
        }
    }

    private inline fun <T> Result<T>.mapTiles(transform: (T) -> List<ProfileTile>): Result<List<ProfileTile>> =
        when (this) {
            is Result.Success -> Result.Success(transform(data))
            is Result.Failure -> this
        }

    private companion object {
        const val SHOWN_BADGES = 2
    }
}

/** A dónde lleva cada parte de 81_Perfil. */
data class ProfileNavigation(
    val onTab: (MainTab) -> Unit = {},
    val onCreate: (CreateOption) -> Unit = {},
    val onEdit: () -> Unit = {},
    val onSettings: () -> Unit = {},
    val onNotifications: () -> Unit = {},
    val onMyCode: () -> Unit = {},
    val onFriends: () -> Unit = {},
    val onBadges: () -> Unit = {},
    val onReservations: () -> Unit = {},
    val onFavorites: () -> Unit = {},
    val onMemories: () -> Unit = {},
    val onMyReviews: () -> Unit = {},
    val onCoupons: () -> Unit = {},
    val onPendingReviews: () -> Unit = {},
    val onVerifyEmail: (String) -> Unit = {},
    val onPostcard: (String) -> Unit = {},
    val onPlan: (String) -> Unit = {},
    val onPlace: (String) -> Unit = {}
)
