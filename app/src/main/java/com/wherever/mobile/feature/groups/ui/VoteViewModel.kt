package com.wherever.mobile.feature.groups.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.groups.domain.GroupsRepository
import com.wherever.mobile.feature.groups.domain.Vote
import com.wherever.mobile.feature.groups.domain.VotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VoteUiState(
    val loading: Boolean = true,
    val loadError: UiText? = null,
    val votes: List<Vote> = emptyList(),
    val iAmOrganizer: Boolean = false,
    /** Opción elegida y aún sin enviar, por votación. */
    val selected: Map<String, String> = emptyMap(),
    val sending: Set<String> = emptySet(),
    val error: UiText? = null,
    val message: UiText? = null,
    val now: Instant = Instant.EPOCH
) {
    val empty: Boolean
        get() = !loading && loadError == null && votes.isEmpty()

    /** Lo que se marca en pantalla: lo elegido sin enviar o, si no, mi voto. */
    fun choice(vote: Vote): String? = selected[vote.id] ?: vote.myVote

    fun canSend(vote: Vote): Boolean {
        val choice = selected[vote.id] ?: return false
        return isOpen(vote) && choice != vote.myVote && vote.id !in sending
    }

    /** Abierta y sin vencer según el reloj del teléfono (la API la cierra al vencer). */
    fun isOpen(vote: Vote): Boolean = vote.open && remaining(vote)?.isNegative != true

    fun remaining(vote: Vote): Duration? = try {
        Duration.between(now, Instant.parse(vote.closesAt))
    } catch (_: DateTimeParseException) {
        null
    }
}

/** 48_Votar (specs/nv-25): un voto por persona, se cambia hasta el cierre; el organizador puede cerrar. */
@HiltViewModel
class VoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groups: GroupsRepository,
    private val votesRepository: VotesRepository,
    private val clock: Clock
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["id"])
    private val mutableState = MutableStateFlow(VoteUiState(now = Instant.now(clock)))
    val state: StateFlow<VoteUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() = reload(clearError = true)

    private fun reload(clearError: Boolean) {
        mutableState.update {
            it.copy(
                loading = it.votes.isEmpty(),
                loadError = null,
                error = if (clearError) null else it.error,
                now = Instant.now(clock)
            )
        }
        viewModelScope.launch {
            val group = groups.group(groupId)
            val votes = votesRepository.votes(groupId)
            mutableState.update {
                val organizer = (group as? Result.Success)?.data?.iAmOrganizer ?: it.iAmOrganizer
                when (votes) {
                    is Result.Success -> it.copy(
                        loading = false,
                        votes = votes.data.sortedByDescending(Vote::open),
                        iAmOrganizer = organizer
                    )
                    is Result.Failure -> if (it.votes.isEmpty()) {
                        it.copy(loading = false, loadError = votes.error.toUiText())
                    } else {
                        it.copy(loading = false, error = votes.error.toUiText())
                    }
                }
            }
        }
    }

    fun select(vote: Vote, placeId: String) {
        if (!state.value.isOpen(vote)) return
        mutableState.update { it.copy(selected = it.selected + (vote.id to placeId), error = null) }
    }

    fun send(vote: Vote) {
        val current = state.value
        if (!current.canSend(vote)) return
        val placeId = current.selected.getValue(vote.id)
        mutableState.update { it.copy(sending = it.sending + vote.id, error = null) }
        viewModelScope.launch {
            val result = votesRepository.vote(vote.id, placeId)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(
                        sending = it.sending - vote.id,
                        selected = it.selected - vote.id,
                        votes = it.votes.map { v -> if (v.id == vote.id) result.data else v },
                        message = UiText.Res(R.string.vote_sent)
                    )
                    is Result.Failure -> it.copy(
                        sending = it.sending - vote.id,
                        error = if (result.error is AppError.Conflict) {
                            UiText.Res(R.string.vote_closed_now)
                        } else {
                            result.error.toUiText()
                        }
                    )
                }
            }
            if (result is Result.Failure && result.error is AppError.Conflict) reload(clearError = false)
        }
    }

    fun close(vote: Vote) {
        if (!state.value.iAmOrganizer || vote.id in state.value.sending) return
        mutableState.update { it.copy(sending = it.sending + vote.id, error = null) }
        viewModelScope.launch {
            val result = votesRepository.closeVote(vote.id)
            mutableState.update {
                when (result) {
                    is Result.Success -> it.copy(
                        sending = it.sending - vote.id,
                        votes = it.votes.map { v -> if (v.id == vote.id) result.data else v },
                        message = UiText.Res(
                            if (result.data.applied == false) R.string.vote_closed_not_applied else R.string.vote_closed
                        )
                    )
                    is Result.Failure -> it.copy(sending = it.sending - vote.id, error = result.error.toUiText())
                }
            }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }
}

data class VoteActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onSelect: (Vote, String) -> Unit = { _, _ -> },
    val onSend: (Vote) -> Unit = {},
    val onClose: (Vote) -> Unit = {}
)
