package com.wherever.mobile.feature.reviews.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.di.ApplicationScope
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.reviews.domain.MyReview
import com.wherever.mobile.feature.reviews.domain.PendingReview
import com.wherever.mobile.feature.reviews.domain.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tiempo para Deshacer antes del `DELETE` (D-32a: la API no restaura). */
const val UNDO_WINDOW_MS = 5_000L

enum class MyReviewsTab { PUBLISHED, PENDING }

data class MyReviewsUiState(
    val tab: MyReviewsTab = MyReviewsTab.PUBLISHED,
    val loading: Boolean = true,
    val reviews: List<MyReview> = emptyList(),
    val pending: List<PendingReview> = emptyList(),
    val cursor: String? = null,
    val loadingMore: Boolean = false,
    val error: UiText? = null,
    /** Reseña que pide confirmar «Eliminar». */
    val confirmDelete: MyReview? = null,
    /** Reseña quitada de la lista que aún se puede recuperar con Deshacer. */
    val undoable: MyReview? = null,
    val message: UiText? = null
)

/**
 * 79_MisRes: Publicadas y Por reseñar. Eliminar quita la fila y espera [UNDO_WINDOW_MS] antes del `DELETE`.
 * La espera corre en el alcance de la app: si se sale de la pantalla, el borrado se manda igual al vencer.
 */
@HiltViewModel
class MyReviewsViewModel @Inject constructor(
    private val repository: ReviewRepository,
    @ApplicationScope private val appScope: CoroutineScope
) : ViewModel() {
    private val mutableState = MutableStateFlow(MyReviewsUiState())
    val state: StateFlow<MyReviewsUiState> = mutableState.asStateFlow()
    private var pendingDelete: Job? = null

    init {
        load()
    }

    fun load() {
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val pending = async { repository.pending() }
            when (val page = repository.mine()) {
                is Result.Success -> {
                    val visits = (pending.await() as? Result.Success)?.data.orEmpty()
                    mutableState.update { state ->
                        state.copy(
                            loading = false,
                            reviews = page.data.items.filterNot { it.id == state.undoable?.id },
                            cursor = page.data.nextCursor,
                            pending = visits
                        )
                    }
                }
                is Result.Failure -> {
                    pending.cancel()
                    mutableState.update { it.copy(loading = false, error = page.error.toUiText()) }
                }
            }
        }
    }

    fun loadMore() {
        val cursor = state.value.cursor ?: return
        if (state.value.loadingMore) return
        mutableState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            when (val page = repository.mine(cursor)) {
                is Result.Success -> mutableState.update {
                    it.copy(loadingMore = false, reviews = it.reviews + page.data.items, cursor = page.data.nextCursor)
                }
                is Result.Failure -> mutableState.update {
                    it.copy(loadingMore = false, message = page.error.toUiText())
                }
            }
        }
    }

    fun onTab(tab: MyReviewsTab) = mutableState.update { it.copy(tab = tab) }

    fun askDelete(review: MyReview) = mutableState.update { it.copy(confirmDelete = review) }

    fun cancelDelete() = mutableState.update { it.copy(confirmDelete = null) }

    /** Confirma: quita la fila, muestra «Reseña eliminada» con Deshacer y manda el `DELETE` al vencer el plazo. */
    fun confirmDelete() {
        val review = state.value.confirmDelete ?: return
        flushPendingDelete()
        mutableState.update {
            it.copy(confirmDelete = null, undoable = review, reviews = it.reviews.filterNot { r -> r.id == review.id })
        }
        pendingDelete = appScope.launch {
            delay(UNDO_WINDOW_MS)
            sendDelete(review)
        }
    }

    fun undo() {
        val review = state.value.undoable ?: return
        pendingDelete?.cancel()
        pendingDelete = null
        mutableState.update { state ->
            state.copy(undoable = null, reviews = (state.reviews + review).sortedByDescending { it.createdAt })
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }

    /** Un segundo borrado no espera: el anterior se manda ya. */
    private fun flushPendingDelete() {
        val previous = state.value.undoable ?: return
        pendingDelete?.cancel()
        appScope.launch { sendDelete(previous) }
    }

    private suspend fun sendDelete(review: MyReview) {
        val result = repository.delete(review.id)
        mutableState.update { state ->
            val cleared = if (state.undoable?.id == review.id) state.copy(undoable = null) else state
            if (result is Result.Failure) {
                // No se pudo borrar: la reseña vuelve a la lista y se explica por qué.
                cleared.copy(
                    reviews = (cleared.reviews + review).distinctBy { it.id }.sortedByDescending { it.createdAt },
                    message = result.error.toUiText()
                )
            } else {
                cleared
            }
        }
    }
}
