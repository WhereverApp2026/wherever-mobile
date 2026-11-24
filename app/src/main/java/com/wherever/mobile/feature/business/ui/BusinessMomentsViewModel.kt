package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BusinessMoment
import com.wherever.mobile.feature.business.domain.BusinessReviewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MomentsTab { NEW, FEATURED }

data class BusinessMomentsUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    val tab: MomentsTab = MomentsTab.NEW,
    val all: List<BusinessMoment> = emptyList(),
    val featured: List<BusinessMoment> = emptyList(),
    /** Momentos con un cambio en camino: el segundo toque se ignora. */
    val busy: Set<String> = emptySet(),
    val message: UiText? = null
) {
    val visible: List<BusinessMoment> get() = if (tab == MomentsTab.NEW) all else featured

    val newCount: Int get() = all.count { !it.thanked }
}

/** NegMomentos (specs/nv-36): Nuevos y Destacados; Agradecer y Destacar sin duplicar. */
@HiltViewModel
class BusinessMomentsViewModel @Inject constructor(private val repository: BusinessReviewsRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(BusinessMomentsUiState())
    val state: StateFlow<BusinessMomentsUiState> = mutableState.asStateFlow()

    private var businessId: String? = null

    fun start(id: String) {
        if (businessId == id) return
        businessId = id
        load()
    }

    fun load() {
        val id = businessId ?: return
        mutableState.update { it.copy(loading = it.all.isEmpty(), error = null) }
        viewModelScope.launch {
            val featured = async { repository.moments(id, featured = true) }
            when (val all = repository.moments(id, featured = false)) {
                is Result.Failure -> mutableState.update { it.copy(loading = false, error = all.error.toUiText()) }
                is Result.Success -> mutableState.update {
                    it.copy(
                        loading = false,
                        all = all.data,
                        featured = (featured.await() as? Result.Success)?.data ?: all.data.filter { m -> m.featured }
                    )
                }
            }
        }
    }

    fun selectTab(tab: MomentsTab) = mutableState.update { it.copy(tab = tab) }

    /** Agradecer solo enciende la marca (avisa al autor una vez). */
    fun thank(moment: BusinessMoment) {
        if (moment.thanked) return
        mark(moment, thanked = true, featured = null, R.string.moments_thanked)
    }

    /** Destacar o quitar de destacados. */
    fun toggleFeatured(moment: BusinessMoment) = mark(
        moment,
        thanked = null,
        featured = !moment.featured,
        if (moment.featured) R.string.moments_unfeatured else R.string.moments_featured
    )

    private fun mark(moment: BusinessMoment, thanked: Boolean?, featured: Boolean?, done: Int) {
        val id = businessId ?: return
        if (moment.id in state.value.busy) return
        mutableState.update { it.copy(busy = it.busy + moment.id) }
        viewModelScope.launch {
            when (val result = repository.markMoment(id, moment.id, thanked, featured)) {
                is Result.Success -> mutableState.update { current ->
                    val updated = result.data
                    val all = current.all.map { if (it.id == updated.id) updated else it }
                    current.copy(
                        busy = current.busy - moment.id,
                        all = all,
                        featured = all.filter { it.featured },
                        message = UiText.Res(done)
                    )
                }
                is Result.Failure -> mutableState.update {
                    it.copy(busy = it.busy - moment.id, message = result.error.toUiText())
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}
