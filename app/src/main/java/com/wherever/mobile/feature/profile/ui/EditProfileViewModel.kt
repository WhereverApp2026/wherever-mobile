package com.wherever.mobile.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.fieldErrors
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.auth.domain.isValidName
import com.wherever.mobile.feature.profile.domain.CitySuggestion
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import com.wherever.mobile.feature.profile.domain.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val loading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val emailVerified: Boolean = false,
    val nameError: UiText? = null,
    val city: CitySearchState = CitySearchState(),
    val originalName: String = "",
    val originalCityId: String? = null,
    val saving: Boolean = false,
    val error: UiText? = null
) {
    val canSave: Boolean
        get() = !loading && !saving && isValidName(name)
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(private val repository: ProfileRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<Unit>(Channel.BUFFERED)
    val saved = eventChannel.receiveAsFlow()

    private val citySearch = CitySearchController(viewModelScope, repository) { change ->
        mutableState.update { it.copy(city = change(it.city)) }
    }

    init {
        viewModelScope.launch {
            val cached = repository.observeProfile().firstOrNull()
            if (cached != null) {
                fill(cached)
            } else {
                when (val result = repository.refresh()) {
                    is Result.Success -> fill(result.data)
                    is Result.Failure -> mutableState.update {
                        it.copy(loading = false, error = result.error.toUiText())
                    }
                }
            }
        }
    }

    private fun fill(profile: UserProfile) = mutableState.update {
        it.copy(
            loading = false,
            name = profile.name,
            email = profile.email,
            emailVerified = profile.emailVerified,
            originalName = profile.name,
            originalCityId = profile.city?.id,
            city = CitySearchState(query = profile.city?.name.orEmpty(), selectedId = profile.city?.id),
            nameError = null
        )
    }

    fun onNameChange(value: String) = mutableState.update {
        it.copy(name = value, error = null, nameError = if (isValidName(value)) null else INVALID_NAME)
    }

    fun onCityQueryChange(query: String) = citySearch.onQueryChange(query)

    fun onCitySelect(city: CitySuggestion) = citySearch.onSelect(city)

    fun save() {
        val current = state.value
        if (!current.canSave) return
        val name = current.name.trim().takeIf { it != current.originalName }
        val cityId = current.city.selectedId?.takeIf { it != current.originalCityId }
        if (name == null && cityId == null) {
            eventChannel.trySend(Unit)
            return
        }
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.updateProfile(name, cityId)) {
                is Result.Success -> {
                    mutableState.update { it.copy(saving = false) }
                    eventChannel.send(Unit)
                }
                is Result.Failure -> onFailure(result.error)
            }
        }
    }

    private suspend fun onFailure(error: AppError) {
        if (error is AppError.Conflict) {
            reloadAfterConflict()
            return
        }
        val fields = error.fieldErrors()
        mutableState.update {
            it.copy(
                saving = false,
                nameError = fields["nombre"],
                error = error.toUiText().takeIf { fields.isEmpty() }
            )
        }
    }

    /** 409: la versión guardada es vieja. Se avisa y se muestra lo que hay en el servidor. */
    private suspend fun reloadAfterConflict() {
        (repository.refresh() as? Result.Success)?.let { fill(it.data) }
        mutableState.update { it.copy(saving = false, error = UiText.Res(R.string.edit_profile_conflict)) }
    }

    private companion object {
        val INVALID_NAME = UiText.Res(R.string.validation_name)
    }
}
