package com.wherever.mobile.feature.auth.ui

import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.BaseViewModel
import com.wherever.mobile.feature.auth.data.OnboardingRepository
import com.wherever.mobile.feature.auth.domain.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingStartViewModel @Inject constructor(private val repository: OnboardingRepository) :
    BaseViewModel<OnboardingStartUiState, OnboardingStartEvent>(OnboardingStartUiState.Loading) {

    init {
        scope.launch {
            val step = repository.connect()
            setState(OnboardingStartUiState.Ready)
            emitEvent(OnboardingStartEvent.Navigate(step))
        }
    }
}

@HiltViewModel
class PreferencesViewModel @Inject constructor(private val repository: OnboardingRepository) :
    BaseViewModel<PreferencesUiState, PreferencesEvent>(PreferencesUiState()) {

    init {
        scope.launch {
            val snapshot = repository.snapshot()
            setState(
                PreferencesUiState(
                    isLoading = false,
                    interests = snapshot.preferences.interests.toSet(),
                    vibe = snapshot.preferences.vibe,
                    budget = snapshot.preferences.budget,
                    company = snapshot.preferences.company
                )
            )
        }
    }

    fun toggleInterest(id: String) {
        updateState {
            val selected = if (id in it.interests) it.interests - id else it.interests + id
            it.copy(interests = selected, interestsMissing = false, error = null)
        }
    }

    fun selectVibe(id: String) = updateState { it.copy(vibe = id, error = null) }

    fun selectBudget(id: String) = updateState { it.copy(budget = id, error = null) }

    fun selectCompany(id: String) = updateState { it.copy(company = id, error = null) }

    fun save() {
        val current = state.value
        if (current.isSaving) return
        if (current.interests.isEmpty()) {
            setState(current.copy(interestsMissing = true))
            return
        }
        setState(current.copy(isSaving = true, interestsMissing = false, error = null))
        scope.launch {
            val preferences = OnboardingPreferences(
                interests = current.interests.toList(),
                vibe = current.vibe,
                budget = current.budget,
                company = current.company
            )
            when (val result = repository.savePreferences(preferences)) {
                is Result.Success -> emitEvent(PreferencesEvent.Continue)
                is Result.Failure -> setState(state.value.copy(isSaving = false, error = result.error))
            }
        }
    }

    fun skip() {
        if (state.value.isSaving) return
        setState(state.value.copy(isSaving = true, error = null))
        scope.launch {
            repository.skipPreferences()
            emitEvent(PreferencesEvent.Continue)
        }
    }
}

@HiltViewModel
class LocationViewModel @Inject constructor(private val repository: OnboardingRepository) :
    BaseViewModel<LocationUiState, LocationEvent>(LocationUiState()) {

    fun continueFlow() {
        if (state.value.isSaving) return
        setState(LocationUiState(isSaving = true))
        scope.launch {
            repository.locationHandled()
            emitEvent(LocationEvent.Continue)
        }
    }
}

@HiltViewModel
class FriendsViewModel @Inject constructor(private val repository: OnboardingRepository) :
    BaseViewModel<FriendsUiState, FriendsEvent>(FriendsUiState()) {

    init {
        load()
    }

    fun load() {
        setState(state.value.copy(isLoading = true, error = null))
        scope.launch {
            when (val result = repository.suggestions()) {
                is Result.Success ->
                    setState(state.value.copy(isLoading = false, suggestions = result.value))

                is Result.Failure ->
                    setState(state.value.copy(isLoading = false, error = result.error))
            }
        }
    }

    fun sendRequest(id: String) {
        val current = state.value
        if (id in current.sentIds || id in current.sendingIds) return
        setState(current.copy(sendingIds = current.sendingIds + id, error = null))
        scope.launch {
            when (val result = repository.sendFriendRequest(id)) {
                is Result.Success ->
                    setState(
                        state.value.copy(
                            sendingIds = state.value.sendingIds - id,
                            sentIds = state.value.sentIds + id
                        )
                    )

                is Result.Failure ->
                    setState(
                        state.value.copy(
                            sendingIds = state.value.sendingIds - id,
                            error = result.error
                        )
                    )
            }
        }
    }

    fun continueFlow() {
        if (state.value.isContinuing) return
        setState(state.value.copy(isContinuing = true, error = null))
        scope.launch {
            repository.friendsHandled()
            emitEvent(FriendsEvent.Continue)
        }
    }
}

@HiltViewModel
class PermissionsViewModel @Inject constructor(private val repository: OnboardingRepository) :
    BaseViewModel<PermissionsUiState, PermissionsEvent>(PermissionsUiState()) {

    fun complete() {
        val current = state.value
        if (current.isCompleting) return
        setState(current.copy(isCompleting = true, error = null))
        scope.launch {
            when (val result = repository.complete()) {
                is Result.Success -> emitEvent(PermissionsEvent.Complete)
                is Result.Failure -> setState(state.value.copy(isCompleting = false, error = result.error))
            }
        }
    }
}
